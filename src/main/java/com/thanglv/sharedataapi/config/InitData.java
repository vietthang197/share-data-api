package com.thanglv.sharedataapi.config;

import com.thanglv.sharedataapi.entity.UserRole;
import com.thanglv.sharedataapi.repository.UserRoleRepository;
import com.thanglv.sharedataapi.util.Constant;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class InitData {

    private final UserRoleRepository userRoleRepository;

    @PostConstruct
    public void init() {
         var defaultRoleOptional = userRoleRepository.findByRole(Constant.DEFAULT_USER_ROLE);
         if (defaultRoleOptional.isEmpty()) {
             var userRole = new UserRole();
             userRole.setRole(Constant.DEFAULT_USER_ROLE);
             userRoleRepository.save(userRole);
         }
    }
}
