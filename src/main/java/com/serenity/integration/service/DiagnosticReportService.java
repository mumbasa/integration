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
                .collect(Collectors.toMap(e -> e.getUuid(), e -> e)); 
        Map<String, Doctors> doc = doctorRepository.findOPDPractitioners().stream()
                .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));
        String sql = "SELECT count(*) from diagnostic_report";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<DiagnosticReport> serviceRequests = new ArrayList<DiagnosticReport>();

            int startIndex = i * batchSize;
            String sqlQuery = """
                    SELECT dr.id, dr."uuid", dr.created_at, dr.is_deleted, dr.modified_at, dr.category, dr.code, dr.display, conclusion, dr.status, issued_date, effective_date_time, effective_period_start, effective_period_end, approved_date_time, approved_by_uuid, approved_by_name, rejected_by_uuid, rejected_by_name, rejected_date_time, review_request_by_uuid, review_request_by_name, review_request_date_time, based_on_id,sr."uuid" as service_request_id, sr.encounter_id, p.uuid as patient_id, dr.visit_id, service_request_category, billing_turnaround_time, intra_laboratory_turnaround_time, total_turnaround_time
from diagnostic_report dr left join patient p on p.id = dr.patient_id  left join service_request sr on dr.based_on_id =sr.id order by dr.id asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                DiagnosticReport request = new DiagnosticReport();
              
                request.setAcessionNumber(set.getString("based_on_id"));
                request.setUuid(set.getString("uuid"));
                request.setBasedOnId(set.getString("service_request_id"));
                request.setDisplay(set.getString("display"));
                request.setPatientId(set.getString("patient_id"));
                request.setConclusion(set.getString("conclusion"));
                request.setApprovedById(set.getString("approved_by_uuid"));
                request.setApprovedByName(set.getString("approved_by_name"));
                request.setReviewedById(set.getString("review_request_by_uuid"));
                request.setReviewedByName(set.getString("rejected_by_name"));
                request.setRejectedById(set.getString("rejected_by_uuid"));
                request.setRejectedDatetime(set.getString("rejected_date_time"));
                request.setPatientBirthDate(mps.get(set.getString("patient_id")).getBirthDate());
                request.setPatientFullName(mps.get(set.getString("patient_id")).getFirstName() +" "+mps.get(set.getString("patient_id")).getLastName());
                request.setPatientGender(mps.get(set.getString("patient_id")).getGender());
                request.setPatientMobile(mps.get(set.getString("patient_id")).getMobile());
                request.setPatientMrNumber(mps.get(set.getString("patient_id")).getMrNumber());
                request.setEncounterId(set.getString("encounter_id"));
                request.setIssuedDate(set.getString("issued_date")==null?request.getCreatedAt(): request.getIssuedDate());
                request.setServiceRequestCategory(set.getString("category"));
                
                request.setServiceProviderId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                if (set.getString("approved_by_uuid") != null) {
                   try{
                    request.setPerformerId(set.getString("approved_by_uuid"));
                    request.setPerformerName((set.getString("approved_by_name")));
                   }catch (Exception e){
                    e.printStackTrace();
                   }
               
                }
                request.setCreatedAt(set.getString(3));
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
    INSERT INTO diagnostic_reports (
        created_at, issued_date, sample_received_date_time, approved_date_time, reviewed_date_time,
        effective_datetime, id, uuid, display, category, code, conclusion, purpose,
        healthcare_service_id, service_provider_id, status, performer_name, performer_id,
        approved_by_name, approved_by_id, reviewed_by_name, reviewed_by_id, encounter_id,
        based_on_id, patient_id, visit_id, service_request_category, patient_mr_number,
        patient_full_name, patient_mobile, patient_birth_date, patient_gender,accession_number,updated_at
    ) VALUES (
        ?::timestamp, ?::timestamp, ?::timestamp, ?::timestamp, ?::timestamp,
        ?::timestamp, ?, uuid(?), ?, ?, ?, ?, ?, uuid(?), uuid(?), ?, ?, uuid(?), ?,
        uuid(?), ?, uuid(?), ?, ?, ?, ?, ?, ?, ?, ?, cast(? AS DATE), ?,?,now()
    );
""";
serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        DiagnosticReport report = reports.get(i);
        ps.setString(1, report.getCreatedAt()); // created_at
        ps.setString(2, report.getIssuedDate()); // issued_date
        ps.setString(3, report.getSampleReceivedDateTime()); // sample_received_date_time (fixed)
        ps.setString(4, report.getApprovedDateTime()); // approved_date_time
        ps.setString(5, report.getReviewedDateTime()); // reviewed_date_time
        ps.setString(6, report.getEffectiveDateTime()); // effective_datetime
        ps.setLong(7, report.getId()); // id
        ps.setString(8, report.getUuid()); // uuid
        ps.setString(9, report.getDisplay()); // display
        ps.setString(10, report.getCategory() == null ? "laboratory" : report.getCategory()); // category
        ps.setString(11, report.getCode()); // code
        ps.setString(12, report.getConclusion() == null ? "" : report.getConclusion()); // conclusion
        ps.setString(13, report.getPurpose()== null ? "" : report.getPurpose()); // purpose
        ps.setString(14, report.getHealthcareServiceId()== null ? "30f534f0-1210-4d9b-bd10-7871075534cb" : report.getHealthcareServiceId()); // healthcare_service_id
        ps.setString(15, report.getServiceProviderId()); // service_provider_id
        ps.setString(16, report.getStatus()== null ? "partial" : report.getStatus()); // status
        ps.setString(17, report.getPerformerName()==null?"":report.getPerformerName()); // performer_name
        ps.setString(18, report.getPerformerId()==null?report.getPatientId():report.getPerformerId()); // performer_id
        ps.setString(19, report.getApprovedByName()); // approved_by_name
        ps.setString(20, report.getApprovedById()); // approved_by_id
        ps.setString(21, report.getReviewedByName()); // reviewed_by_name
        ps.setString(22, report.getReviewedById()); // reviewed_by_id
        ps.setString(23, report.getEncounterId()); // encounter_id
        ps.setString(24, report.getBasedOnId()); // based_on_id
        ps.setString(25, report.getPatientId()); // patient_id
        ps.setString(26, report.getVisitId()); // visit_id
        ps.setString(27, report.getServiceRequestCategory()); // service_request_category
        ps.setString(28, report.getPatientMrNumber()); // patient_mr_number
        ps.setString(29, report.getPatientFullName()); // patient_full_name
        ps.setString(30, report.getPatientMobile()); // patient_mobile
        ps.setString(31, report.getPatientBirthDate()); // patient_birth_date
        ps.setString(32, report.getPatientGender()); // patient_gender (added)
        ps.setString(33, report.getAcessionNumber());
    }

    @Override
    public int getBatchSize() {
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
        String cleaner ="""
                update diagnostic_report 
set patientbirthdate =p.birthdate ,patientmobile =p.mobile ,patientgender =p.gender ,patientfullname=concat(p.firstname,' ',p.lastname) 
from patient_information p
where p."uuid" =patientid ;
                """;

        long rows = diagnosticReportRepository.findCleanCount();
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


    public void submitTasker(int batchSize) {
        long rows = diagnosticReportRepository.findCleanCount();
        long batches = (rows + batchSize - 1) / batchSize; // Safe ceiling division

        logger.info("Total batches: {}", batches);
        for (int i = 0; i < batches; i++) {
            final int batchNumber = i;

       
                int startIndex = batchNumber * batchSize;
                logger.info("Processing batch {}/{} indices [{}]", batchNumber + 1, batches, startIndex);
                migrateDiagnosticReports(diagnosticReportRepository.findBhy(startIndex, batchSize));
             
        
        }

   
    }
}



