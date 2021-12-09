package com.example.antispamlpbot;

import lombok.Data;

@Data
public class SuspectUser {
    private Long id;
    private String userName;
    private Long joinDate;

    public SuspectUser(Long id, String userName, Long joinDate) {
        this.id = id;
        this.userName = userName;
        this.joinDate = joinDate;
    }
}
