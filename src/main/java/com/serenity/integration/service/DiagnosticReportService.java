package com.serenity.integration.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.serenity.integration.models.DiagnosticReport;
import com.serenity.integration.models.Doctors;
import com.serenity.integration.models.Observation;
import com.serenity.integration.models.PatientData;
import com.serenity.integration.models.ServiceRequest;
import com.serenity.integration.repository.DiagnosticReportRepository;
import com.serenity.integration.repository.DoctorRepository;
import com.serenity.integration.repository.PatientRepository;
@Service
public class DiagnosticReportService {
@Autowired
DiagnosticReportRepository diagnosticReportRepository;
 @Autowired
    @Qualifier(value = "hisJdbcTemplate")
    JdbcTemplate hisJdbcTemplate;

    @Autowired
    @Qualifier(value = "legJdbcTemplate")
    JdbcTemplate legJdbcTemplate;

    @Autowired
    @Qualifier(value = "vectorJdbcTemplate")
    JdbcTemplate vectorJdbcTemplate;

    @Autowired
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;


    @Autowired
    PatientRepository patientRepository;

    @Autowired
    DoctorRepository doctorRepository;
    Logger logger = LoggerFactory.getLogger(getClass());

    public void getLegacyDiagnosticReport(int batchSize) {
           Map<String, PatientData> mps = patientRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));
        Map<String, Doctors> doc = doctorRepository.findOPDPractitioners().stream()
                .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));
        String sql = "SELECT count(*) from diagnostic_report";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = 100;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<DiagnosticReport> serviceRequests = new ArrayList<DiagnosticReport>();

            int startIndex = i * batchSize;
            String sqlQuery = """
                    select * from diagnostic_report dr join patient p on p.id = dr.patient_id  join service_request sr on dr.based_on_id =sr.id order by dr.id asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                DiagnosticReport request = new DiagnosticReport();
                request.setUuid(set.getString(2));
                request.setBasedOnId(set.getString(75));
                request.setDisplay(set.getString(8));
                request.setPatientId(mps.get(set.getString("mr_number")).getUuid());
                request.setPatientBirthDate(mps.get(set.getString("mr_number")).getBirthDate());
                request.setPatientFullName(mps.get(set.getString("mr_number")).getFullName());
                request.setPatientGender(mps.get(set.getString("mr_number")).getGender());
                request.setPatientMobile(mps.get(set.getString("mr_number")).getMobile());
                request.setPatientMrNumber(mps.get(set.getString("mr_number")).getMrNumber());
                request.setEncounterId(set.getString(103));
                request.setServiceRequestCategory(set.getString("service_request_category"));
                request.setServiceProviderId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                if (set.getString("requesting_practitioner_role_id") != null) {
                   try{
                    request.setPerformerId(set.getString("requesting_practitioner_role_id"));
                    request.setPerformerName(doc.get(set.getString("requesting_practitioner_role_id")).getFullName());
                   }catch (Exception e){
                    e.printStackTrace();
                   }
               
                }
                
                request.setCreatedAt(set.getString(2));
                request.setVisitId(set.getString("visit_id"));
                serviceRequests.add(request);

            }
            diagnosticReportRepository.saveAll(serviceRequests);
            logger.info("Saved Diagnostic result");
            cleanDiagnositcs();
        }
    }


    public void migrateDiagnosticReports(List<DiagnosticReport> reports) {
        String sql = """
                        INSERT INTO diagnostic_reports
(created_at,  issued_date, sample_received_date_time, approved_date_time,reviewed_date_time,
  effective_datetime, billing_turnaround_time, total_turnaround_time, intra_laboratory_turnaround_time,id, 
  "uuid",  display, category,  code,  conclusion, 
  purpose,  healthcare_service_id, service_provider_id, status, performer_name, 
  performer_id, approved_by_name, approved_by_id, reviewed_by_name, reviewed_by_id, 
  encounter_id, based_on_id, patient_id, visit_id, ervice_request_category,
   patient_mr_number, patient_full_name, patient_mobile, patient_birth_date,patient_gender)
VALUES(?::timestamp,?::timestamp,?::timestamp,?::timestamp,?::timestamp,
?::timestamp,?,?,?,?,
uuid(?),?,?,?,?,
?,uuid(?),uuid(?),?,?,
uuid(?),?,uuid(?),?,uuid(?),
?,?,?,?,?,
?,?,?,?,?

);
                        """;
        serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // TODO Auto-generated method stub
                DiagnosticReport report = reports.get(i);
                ps.setString(1, report.getCreatedAt());
                ps.setString(2, report.getIssuedDate());
                ps.setString(3, report.getCreatedAt());
                ps.setString(4, report.getApprovedDateTime());
                ps.setString(5, report.getReviewedDateTime());

                ps.setString(6, report.getEffectiveDateTime());
                ps.setDouble(7, report.getBillingTurnaroundTime());
                ps.setDouble(8, report.getTotalTurnaroundTime());
                ps.setDouble(9, report.getIntraLaboratoryTurnaroundTime());
                ps.setLong(10, report.getId());

                ps.setString(11, report.getUuid());
                ps.setString(12, report.getDisplay());
                ps.setString(13, report.getCategory()==null?"laboratory":report.getCategory());
                ps.setString(14, report.getCode());
                ps.setString(15, report.getConclusion()==null?"":report.getConclusion());

                ps.setString(16, report.getPurpose());
                ps.setString(17, report.getHealthcareServiceId());
                ps.setString(18, report.getServiceProviderId());
                ps.setString(19, report.getStatus());
                ps.setString(20, report.getPerformerName());

                ps.setString(21, report.getPerformerId());
                ps.setString(22, report.getApprovedByName());
                ps.setString(23, report.getApprovedById());
                ps.setString(24, report.getReviewedByName());
                ps.setString(25, report.getReviewedById());

                ps.setString(26, report.getEncounterId());
                ps.setString(27, report.getBasedOnId());
                ps.setString(28, report.getPatientId());
              ps.setString(29, report.getVisitId());
              ps.setString(30, report.getServiceRequestCategory());

              ps.setString(31, report.getPatientMrNumber());
              ps.setString(32, report.getPatientFullName());
              ps.setString(33, report.getPatientMobile());
            ps.setString(34, report.getPatientBirthDate());
            ps.setString(35, report.getPatientGender());

            }

            @Override
            public int getBatchSize() {
                // TODO Auto-generated method stub
                return reports.size();
            }

        });

    }

    public void cleanDiagnositcs() {

        String sql = """
                          update diagnostic_report 
set visitid =e.visit_id
from encounter e
where diagnostic_report.encounterid =e."uuid"  ;
                            """;
        vectorJdbcTemplate.update(sql);
    }

    public void migrateDiagReportThread(int batchSize) {

        long rows = 50;
        logger.info("Rows size is: {}", rows);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            List<Future<Integer>> futures = executorService.invokeAll(submitTask2(batchSize, rows));
            for (Future<Integer> future : futures) {
                logger.info("Future result: {}", future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error processing batches", e);
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    public List<Callable<Integer>> submitTask2(int batchSize, long rows) {
        List<Callable<Integer>> callables = new ArrayList<>();
        long batches = (rows + batchSize - 1) / batchSize; // Safe ceiling division

        logger.info("Total batches: {}", batches);
        for (int i = 0; i < batches; i++) {
            final int batchNumber = i;

            callables.add(() -> {
                int startIndex = batchNumber * batchSize;
                logger.info("Processing batch {}/{} indices [{}]", batchNumber + 1, batches, startIndex);
                migrateDiagnosticReports(diagnosticReportRepository.findBhy(startIndex, batchSize));
                return 1;
            });
        }

        return callables;
    }
}



