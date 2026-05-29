package com.rentalapp.module.reports.repository;

import com.rentalapp.module.reports.entity.Report;

import java.util.List;
import java.util.Optional;

public interface IReportRepository {
    Report save(Report report);
    Optional<Report> findById(String id);
    List<Report> findAllByOrderByCreatedAtDesc();
}
