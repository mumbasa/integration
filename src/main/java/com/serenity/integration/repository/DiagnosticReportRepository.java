package com.serenity.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.DiagnosticReport;
@Repository
public interface DiagnosticReportRepository extends JpaRepository<DiagnosticReport,Long>{

}
