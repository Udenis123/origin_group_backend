package com.org.group.repository;

import com.org.group.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {

    Optional<Users> findByEmail(String email);

    Optional<Users>findByVerificationCode(String verificationCode);
     Optional<Users>findByNationalId(String nationalId);
     Optional<Users>findByPhone(String phoneNumber);

}
