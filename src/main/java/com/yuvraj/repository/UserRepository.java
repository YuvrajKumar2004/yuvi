package com.yuvraj.repository;

import com.yuvraj.model.User;
import com.yuvraj.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByLoginId(String loginId);
    Optional<User>findByLoginIdAndRole(String loginId, Role roel);
    List<User> findAllByLocked(boolean locked);
    boolean existsByLoginId(String loginId);
}
