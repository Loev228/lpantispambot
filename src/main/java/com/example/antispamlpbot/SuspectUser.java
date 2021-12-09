package com.example.antispamlpbot;


import lombok.Data;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import java.time.Instant;
import java.util.Date;
@Data
public class SuspectUser {
    private Long  id;
    private long joinDate;

    public SuspectUser(Long id, long joinDate) {
        this.id = id;
        this.joinDate = joinDate;
    }
}
