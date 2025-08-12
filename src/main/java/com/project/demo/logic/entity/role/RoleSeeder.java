package com.project.demo.logic.entity.role;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Profile("!test")
@Component
@Order(1)
public class RoleSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final RoleRepository roleRepository;

    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.loadRoles();
    }

    private void loadRoles() {
        final RoleEnum[] roleEnums = RoleEnum.values();
        final Map<RoleEnum, String> roleDescriptions = Map.of(
                RoleEnum.USER, "Usuario Administrador de Fincas",
                RoleEnum.CORPORATION, "Usuario Corporativo",
                RoleEnum.SUPER_ADMIN, "Administrador global del sistema"
        );

        Arrays.stream(roleEnums).forEach(roleName -> {
            Optional<Role> optionalRole = roleRepository.findByRoleName(roleName);

            if (optionalRole.isEmpty()) {
                Role roleToCreate = new Role();
                roleToCreate.setRoleName(roleName);
                roleToCreate.setRoleDescription(roleDescriptions.get(roleName));
                roleToCreate.setActive(true);
                roleRepository.save(roleToCreate);
            }
        });
    }
}