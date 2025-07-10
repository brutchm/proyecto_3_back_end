package com.project.demo.logic.entity.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>  {

    @Query("SELECT u FROM User u WHERE u.userEmail = ?1")
    Optional<User> findByUserName(String userName);

    Optional<User> findByBusinessId(String businessId);

    Optional<User> findByUserEmail(String userEmail);
}