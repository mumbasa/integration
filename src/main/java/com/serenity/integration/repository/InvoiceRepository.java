package com.serenity.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.PatientInvoice;

@Repository
public interface InvoiceRepository extends JpaRepository<PatientInvoice,Long>{
@Query(value="SELECT * FROM patient_invoice offset ?1 limit ?2")
    List<PatientInvoice> findBatch(int startIndex, int batchSize);

}
