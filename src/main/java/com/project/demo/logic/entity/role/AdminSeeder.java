package com.project.demo.logic.entity.role;

import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Profile("!test")
@Order(2)
@Component
public class AdminSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    public AdminSeeder(
            RoleRepository roleRepository,
            UserRepository  userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.createSuperAdministrator();
    }

    private void createSuperAdministrator() {
        User superAdmin = new User();
        superAdmin.setName("Super");
        superAdmin.setUserFirstSurename("Admin");
        superAdmin.setUserEmail("super.admin@gmail.com");
        superAdmin.setUserPassword("superadmin123");

        Optional<Role> optionalRole = roleRepository.findByRoleName(RoleEnum.SUPER_ADMIN);
        Optional<User> optionalUser = userRepository.findByUserEmail(superAdmin.getUserEmail());

        if (optionalRole.isEmpty() || optionalUser.isPresent()) {
            return;
        }

        var user = new User();
        user.setName(superAdmin.getName());
        user.setUserFirstSurename(superAdmin.getUserFirstSurename());
        user.setUserEmail(superAdmin.getUserEmail());
        user.setUserPassword(passwordEncoder.encode(superAdmin.getUserPassword()));
        user.setRole(optionalRole.get());
        user.setIsActive(false);
        userRepository.save(user);
    }
}
