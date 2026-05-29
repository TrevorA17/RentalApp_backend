package com.rentalapp.module.profiles.repository;

import com.rentalapp.module.profiles.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, String>, IProfileRepository {
}
