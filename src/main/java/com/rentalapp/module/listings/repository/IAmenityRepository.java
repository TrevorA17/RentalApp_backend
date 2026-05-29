package com.rentalapp.module.listings.repository;

import com.rentalapp.module.listings.entity.Amenity;

import java.util.List;

public interface IAmenityRepository {
    List<Amenity> findAll();
    List<Amenity> findByIdIn(List<String> ids);
}
