package com.project.demo.logic.entity.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>  {

    Optional<User> findByUserEmail(String userEmail);

    Optional<User> findByUserName(String userName);

    Optional<User> findByBusinessId(String businessId);
}