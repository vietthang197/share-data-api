package com.thanglv.sharedataapi.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.Instant;
import java.util.List;

@Document(collection = "user_account")
@Getter
@Setter
public class UserAccount {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;
    private String password;
    private Instant createdAt;
    private Instant updatedAt;

    @DocumentReference
    private List<UserRole> roles;
    private String isLock;
}
