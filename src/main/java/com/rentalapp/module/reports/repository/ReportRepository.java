package com.rentalapp.module.reports.repository;

import com.rentalapp.module.reports.entity.Report;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, String>, IReportRepository {
    @Override
    @EntityGraph(attributePaths = {"reporterUser", "reportedUser", "listing", "listing.ownerUser"})
    List<Report> findAllByOrderByCreatedAtDesc();
}
