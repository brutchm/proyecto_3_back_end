package com.project.demo.logic.entity.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("select u from User u where u.role.id=?1 and u.isActive=true")
    List<User> findByRoleId(Long roleId);
    @Query("select u from User u where u.role.id=?1 and u.isActive=true")
    Page<User> findByRoleId(Long roleId, Pageable pageable);

}