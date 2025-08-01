package com.thanglv.sharedataapi.repository;

import com.thanglv.sharedataapi.entity.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends MongoRepository<UserAccount, String> {

    Optional<UserAccount> findByEmail(String email);

    List<UserAccount> findAllByEmailIn(Collection<String> emails);
}
