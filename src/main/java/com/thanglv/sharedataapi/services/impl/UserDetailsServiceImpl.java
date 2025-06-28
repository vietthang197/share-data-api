package com.thanglv.sharedataapi.services.impl;

import com.thanglv.sharedataapi.entity.UserAccount;
import com.thanglv.sharedataapi.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserAccount> userAccountOptional = userAccountRepository.findByEmail(username);
        if (userAccountOptional.isEmpty()) {
            throw new UsernameNotFoundException("User does not exist");
        }
        UserAccount userAccount = userAccountOptional.get();
        return new User(userAccount.getEmail(), userAccount.getPassword(), List.of(new SimpleGrantedAuthority("USER")));
    }
}
