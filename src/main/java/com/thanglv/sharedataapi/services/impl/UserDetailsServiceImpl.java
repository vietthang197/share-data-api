package com.thanglv.sharedataapi.services.impl;

import com.thanglv.sharedataapi.entity.UserAccount;
import com.thanglv.sharedataapi.entity.UserRole;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var userAccountOptional = userAccountRepository.findByEmail(username);
        if (userAccountOptional.isEmpty()) {
            throw new UsernameNotFoundException("User does not exist");
        }
        var userAccount = userAccountOptional.get();
        return new User(userAccount.getEmail(), userAccount.getPassword(), userAccount.getRoles().stream().map(UserRole::getRole).map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
    }
}
