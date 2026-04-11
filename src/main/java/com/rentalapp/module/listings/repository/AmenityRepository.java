package com.rentalapp.module.listings.repository;

import com.rentalapp.module.listings.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AmenityRepository extends JpaRepository<Amenity, String> {
    List<Amenity> findByIdIn(List<String> ids);
}
