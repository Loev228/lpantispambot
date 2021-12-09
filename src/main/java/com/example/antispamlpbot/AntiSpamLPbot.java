package com.example.antispamlpbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.KickChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Component
public class AntiSpamLPbot extends TelegramLongPollingBot {

    @Value("${AntiSpamLPbot.username}")
    private String username;

    @Value("${AntiSpamLPbot.token}")
    private String token;

    private List<SuspectUser> suspectUsers = new ArrayList<>();


    @Override
    public void onUpdateReceived(Update update) {

        //String userName = update.getMessage().getFrom().getUserName();
        Integer messageTime = update.getMessage().getDate();
        Integer suspectMessageTime = 0;
        if (update.hasMessage()) {
            long suspectUserId = update.getMessage().getFrom().getId();
            for (SuspectUser suspectUser : suspectUsers) {
                if (suspectUser.getId() == suspectUserId)
                    suspectMessageTime = messageTime;
            }

            List<User> users = update.getMessage().getNewChatMembers();
            System.out.println("Хуета от телеги" + users);
            for (int i = 0; i < users.size(); ++i) {
                SuspectUser temp = new SuspectUser(users.get(i).getId(), Instant.now().getEpochSecond());

                suspectUsers.add(temp);
                System.out.println("наша хуета:" + temp);
            }
            if (update.getMessage().hasText() && (update.getMessage().getDate() - suspectMessageTime) < 10) {
                String chatId = update.getMessage().getChatId().toString();
                Integer messageId = update.getMessage().getMessageId();
                DeleteMessage deleteMessage = new DeleteMessage(chatId, messageId);
                BanChatMember banChatMember = new BanChatMember(chatId, suspectUserId);
                try {
                    execute(deleteMessage);
                    execute(banChatMember);
                } catch (TelegramApiException e) {
                    //todo add logging to the project.
                    e.printStackTrace();
                }
            }

            System.out.println(suspectUsers);
            String chatId = update.getMessage().getChatId().toString();
            SendMessage sm = new SendMessage();
            Long userId = update.getMessage().getFrom().getId();


            sm.setChatId(chatId);
            sm.setText("bukinpidor");


        }
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
