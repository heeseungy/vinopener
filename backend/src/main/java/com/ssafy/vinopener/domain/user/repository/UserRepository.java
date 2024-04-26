package com.ssafy.vinopener.domain.user.repository;

import com.ssafy.vinopener.domain.user.data.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

}
