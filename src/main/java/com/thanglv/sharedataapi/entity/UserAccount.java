package com.thanglv.sharedataapi.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_account")
@Getter
@Setter
public class UserAccount {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Instant createdAt;
    private Instant updatedAt;
    private String isLock;
}
