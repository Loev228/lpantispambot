package com.example.antispamlpbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Component
public class AntiSpamLPbot extends TelegramLongPollingBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(AntiSpamLPbot.class);
    private static final Integer SUSPECT_PERIOD = 30;
    private static final String ANTI_SPAM_LP_BOT = "AntiSpamLPbot";

    @Value("${AntiSpamLPbot.username}")
    private String username;

    @Value("${AntiSpamLPbot.token}")
    private String token;

    private final List<SuspectUser> suspectUsers = new ArrayList<>();

    private Update update;

    @Override
    public void onUpdateReceived(Update newUpdate) {
        update = newUpdate;
        update.getMessage().getNewChatMembers().forEach(this::addNewUserToSuspectUsers);
        if (!update.hasMessage()) {
            LOGGER.debug("No Message.");
            return;
        }
        Boolean isGuiltyContent = isGuiltyContent();
        LOGGER.debug("Is Guilty Content: {}", isGuiltyContent);
        if (!isGuiltyContent) {
            return;
        }
        long userId = update.getMessage().getFrom().getId();
        for (SuspectUser suspectUser : suspectUsers) {
            if (userId == suspectUser.getId()) {
                if (isGuilty(suspectUser)) {
                    banUser(userId);
                    suspectUsers.remove(suspectUser);
                    LOGGER.info("User With Username: {} - Successfully Banned.", suspectUser.getUserName());
                    break;
                }
            }
        }
    }

    private Boolean isGuiltyContent() {
        Boolean isPhoto = isPhoto();
        LOGGER.info("isPhoto: {}", isPhoto);
        Boolean isLink = isLink();
        LOGGER.info("isLink: {}", isLink);
        Boolean isNoGreetings = isNoGreetings();
        LOGGER.info("isNoGreetings: {}", isNoGreetings);
        return isPhoto || isLink || isNoGreetings;
    }

    private void banUser(Long userId) {
        Long chatId = update.getMessage().getChatId();
        Integer messageId = update.getMessage().getMessageId();
        LOGGER.info("Chat Id: {}, Message Id: {}", chatId, messageId);
        try {
            execute(new DeleteMessage(String.valueOf(chatId), messageId));
            execute(new BanChatMember(String.valueOf(chatId), userId));
        } catch (TelegramApiException exception) {
            LOGGER.error("Error During Ban Operation: ", exception);
        }
    }

    public void addNewUserToSuspectUsers(User user) {
        if (ANTI_SPAM_LP_BOT.equals(user.getUserName())) {
            return;
        }
        SuspectUser suspectUser = new SuspectUser(user.getId(), user.getUserName(), Instant.now().getEpochSecond());
        LOGGER.info("New Suspected User: {}", suspectUser);
        suspectUsers.add(suspectUser);
    }

    private Boolean isGuilty(SuspectUser suspectUser) {
        boolean isHasText = update.getMessage().hasText();
        Boolean isSuspectTimeEnded = (update.getMessage().getDate() - suspectUser.getJoinDate()) > SUSPECT_PERIOD;
        if (isSuspectTimeEnded) {
            LOGGER.info("Suspected User: {} - Confirmed", suspectUser);
            suspectUsers.remove(suspectUser);
        }
        LOGGER.info("Is Has Text: {}, Is Suspect Time Ended: {}", isHasText, isSuspectTimeEnded);
        return (isHasText || isLink() || isPhoto()) && !isSuspectTimeEnded;
    }

    private boolean isPhoto() {
        return update.getMessage().hasPhoto();
    }

    private boolean isLink() {
        return update.getMessage().getText().contains("http");
    }

    private boolean isNoGreetings() {
        String firstName = update.getMessage().getFrom().getFirstName();
        String lastName = update.getMessage().getFrom().getLastName();
        return !(update.getMessage().getText().contains(firstName) || update.getMessage().getText().contains(lastName));
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
