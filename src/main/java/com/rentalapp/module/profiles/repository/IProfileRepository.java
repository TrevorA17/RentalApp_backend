package com.rentalapp.module.profiles.repository;

import com.rentalapp.module.profiles.entity.Profile;

import java.util.Optional;

public interface IProfileRepository {
    Profile save(Profile profile);
    Optional<Profile> findByUserId(String userId);
}
