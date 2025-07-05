package com.thanglv.sharedataapi.repository;

import com.thanglv.sharedataapi.entity.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleRepository extends MongoRepository<UserRole, String> {
    Optional<UserRole> findByRole(String role);
}
