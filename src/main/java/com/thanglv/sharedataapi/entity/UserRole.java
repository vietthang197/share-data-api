package com.thanglv.sharedataapi.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_role")
@Getter
@Setter
public class UserRole {

    @Id
    private String id;

    @Indexed(unique = true)
    private String role;
}
