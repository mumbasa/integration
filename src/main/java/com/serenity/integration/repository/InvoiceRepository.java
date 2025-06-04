package com.serenity.integration.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.DiagnosticReport;
import com.serenity.integration.models.PatientInvoice;

@Repository
public interface InvoiceRepository extends JpaRepository<PatientInvoice,Long>{
@Query(value="SELECT * FROM patient_invoice where visitid is not null and patientmrnumber is not null and payerid is not null order by id offset ?1 limit ?2",nativeQuery = true)
    List<PatientInvoice> findBatch(int startIndex, int batchSize);

    @Query(value="SELECT * FROM patient_invoice where visitid is not null and patientmrnumber is not null and payerid is not null and createdat::date >?1 and createdat::date <=?2  order by id",nativeQuery = true)

List<PatientInvoice> findUpdates(LocalDate localDate, LocalDate localDate2);

Optional<PatientInvoice> findByUuid(String uuid);
}
