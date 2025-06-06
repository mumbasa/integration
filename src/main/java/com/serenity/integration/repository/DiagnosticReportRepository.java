package com.serenity.integration.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.DiagnosticReport;
@Repository
public interface DiagnosticReportRepository extends JpaRepository<DiagnosticReport,Long>{
    @Query(value="SELECT * FROM diagnostic_report  order By id OFFSET ?1 LIMIT ?2",nativeQuery=true)
List<DiagnosticReport> findBhy(int offset,int limit);

@Query(value="SELECT * FROM diagnostic_report  where createdat::date > ?1 and createdat::date <=?2 order By id",nativeQuery=true)
List<DiagnosticReport> findUpdates(LocalDate current,LocalDate now);

@Query(value="SELECT count(*) FROM diagnostic_report ",nativeQuery=true)
long findCleanCount();
}
