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
@Component
@Order(3)
public class UserSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.createFarmAdminUser();
        this.createCorporationAdminUser();
    }

    private void createFarmAdminUser() {
        String userEmail = "farm.admin@agrisync.com";
        Optional<Role> optionalRole = roleRepository.findByRoleName(RoleEnum.USER);
        Optional<User> optionalUser = userRepository.findByUserEmail(userEmail);

        if (optionalRole.isEmpty() || optionalUser.isPresent()) {
            return;
        }

        User farmUser = new User();
        farmUser.setUserEmail(userEmail);
        farmUser.setUserPassword(passwordEncoder.encode("user123"));
        farmUser.setName("John");
        farmUser.setUserFirstSurename("Doe");
        farmUser.setRole(optionalRole.get());
        farmUser.setIsActive(true);

        userRepository.save(farmUser);
    }

    private void createCorporationAdminUser() {
        String corpEmail = "corp@agrisync.com";
        Optional<Role> optionalRole = roleRepository.findByRoleName(RoleEnum.CORPORATION);
        Optional<User> optionalUser = userRepository.findByUserEmail(corpEmail);

        if (optionalRole.isEmpty() || optionalUser.isPresent()) {
            return;
        }

        User corpUser = new User();

        // Datos Admin de la Corporación
        corpUser.setUserEmail(corpEmail);
        corpUser.setUserPassword(passwordEncoder.encode("corp123"));
        corpUser.setName("Corp");
        corpUser.setUserFirstSurename("Admin");
        corpUser.setRole(optionalRole.get());
        corpUser.setIsActive(true);

        // Datos de la corporacion
        corpUser.setBusinessName("AgroCorp S.A.");
        corpUser.setBusinessId("J-31015548-3");
        corpUser.setBusinessMission("Liderar el mercado agrícola con tecnología y sostenibilidad.");
        corpUser.setBusinessVision("Ser el principal aliado tecnológico para los agricultores del futuro.");
        corpUser.setBusinessCountry("Costa Rica");
        corpUser.setBusinessStateProvince("Alajuela");
        corpUser.setBusinessLocation("San Carlos, Alajuela");
        corpUser.setBusinessOtherDirections("Oficinas centrales, 200 metros norte del parque.");

        userRepository.save(corpUser);
    }
}