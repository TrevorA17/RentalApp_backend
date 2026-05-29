package com.rentalapp.module.listings.repository;

import com.rentalapp.module.listings.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityRepository extends JpaRepository<Amenity, String>, IAmenityRepository {
}
