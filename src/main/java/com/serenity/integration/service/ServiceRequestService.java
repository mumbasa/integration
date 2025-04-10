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

import com.serenity.integration.models.Diagnosis;
import com.serenity.integration.models.PatientData;
import com.serenity.integration.models.ServiceRequest;
import com.serenity.integration.repository.DoctorRepository;
import com.serenity.integration.repository.PatientRepository;
import com.serenity.integration.repository.ServiceRequestRepository;

@Service
public class ServiceRequestService {
    @Autowired
    ServiceRequestRepository serviceRequestRepository;

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
    PatientRepository patientRepository;

    @Autowired
    DoctorRepository doctorRepository;

    @Autowired
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;

    Logger logger = LoggerFactory.getLogger(getClass());

    public void getLegacyRequest(int batchSize) {
        Map<String, PatientData> mps = patientRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getUuid(), e -> e));
        String sql = "SELECT count(*) from service_request";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<ServiceRequest> serviceRequests = new ArrayList<ServiceRequest>();

            int startIndex = i * batchSize;
            String sqlQuery = """
          SELECT sr.id, sr."uuid", sr.created_at, sr.is_deleted, sr.modified_at, body_site, display, code, sr.category, diagnostic_service_section, due_date, purpose, p.passport_number, sample_received_date_time, priority, sr.status, group_identifier, status_reason, intent, do_not_perform, quantity_value, quantity_unit, occurence, as_needed, authored_on, note, patient_instruction, assigned_to, assigned_to_name, encounter_id, healthcare_service_id, sr.location_id, p.uuid as patient_id, sr.price_tier_id, replaces_id, requesting_patient_id, requesting_practitioner_role_id, requesting_related_contact_id, sr.visit_id, bill_paid_at, canceled_by_name, canceled_by_practitioner_id, canceled_at, encounter_diagnoses, is_mismatched, is_mismatched_comment, service_class,
ci."uuid" as charge_item_uuid FROM service_request sr left join patient p on p.id =sr.patient_id left join "ChargeItem" ci on ci.servicerequestid::uuid=sr."uuid" order by sr.id asc
 
                        offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                ServiceRequest request = new ServiceRequest();
                request.setId(set.getLong("id"));
                request.setUuid(set.getString("uuid"));
                request.setOccurence(set.getString("occurence"));
                request.setCategory(set.getString("category"));
                String encounterId = set.getString("encounter_id");
                if (encounterId != null) {
                    request.setEncounterId(encounterId);
                }
                String patientMrNumber = set.getString("patient_id");
                if (patientMrNumber != null) {
                    request.setPatientFullName(mps.get(patientMrNumber).getFullName());
                    request.setPatientMobile(mps.get(patientMrNumber).getMobile());
                    request.setPatientGender(mps.get(patientMrNumber).getGender());
                    request.setPatientMrNumber(mps.get(patientMrNumber).getMrNumber());
                    request.setPatientId(mps.get(patientMrNumber).getUuid());
                    request.setPatientBirthDate((mps.get(patientMrNumber).getBirthDate()));

                }
                request.setCreatedAt(set.getString("created_at"));
                request.setDisplay(set.getString("display"));
                request.setDoNotPerform(false);
                request.setIntent(set.getString("intent"));
                request.setCode(set.getString("code"));
                request.setPurpose(set.getString("purpose"));
                request.setStatus(set.getString("status"));
                request.setModifiedAt(set.getString("modified_at"));
                request.setDiagnosticServiceSection(set.getString("diagnostic_service_section"));
                request.setPaid(true);
                request.setNote(set.getString("note"));
                request.setGroupIdentifier(set.getString("group_identifier"));
                request.setChargeItemId(set.getString("charge_item_uuid"));
                request.setHealthcareServiceId(set.getString("healthcare_service_id"));
                request.setSampleReceivedDateTime(set.getString("sample_received_date_time"));
                serviceRequests.add(request);

            }
            serviceRequestRepository.saveAll(serviceRequests);
            logger.info("Saved Requests");
        }
        logger.info("Cleaning Requests");
        addVields();

    }

    public void addVields() {

        String sql = """
                update service_request sr
                set practitionerid =uuid(e.assigned_to_id),visitid=e.visit_id
                from encounter e
                where (e."uuid") =sr.encounterid ;
                                """;
        vectorJdbcTemplate.update(sql);
    }

    public void migrateServiceRequestToSerenity(List<ServiceRequest> requests) {
        String sql = """
                              INSERT INTO public.service_requests
    (created_at, due_date, sample_received_date_time, charge, occurence,
     id, body_site, encounter_id, patient_id, practitioner_id,
     visit_id, service_provider_id, uuid, display,
     category, code, diagnostic_service_section, purpose, priority,
     healthcare_service_id, healthcare_service_name, charge_item_id, status,
     status_reason, group_identifier, intent, practitioner_name, patient_mr_number,
     patient_mobile, patient_birth_date, patient_gender, patient_full_name,
     encounter_class, notes,is_paid,updated_at)
VALUES (
    ?::timestamp, ?::timestamp, ?::timestamp, ?, ?::timestamp,  
    ?, ARRAY[?], uuid(?), uuid(?), uuid(?),                    
    uuid(?), uuid(?), uuid(?), ?,                           
    ?, ?, ?, ?, ?,                                           
    uuid(?), ?, uuid(?), ?,                                
    ?, ?, ?, ?, ?,  
    ?,CAST(? AS DATE),?,? ,                                           
    ? ,? ,true ,now()                                               
);

                                """;

        serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // TODO Auto-generated method stub
                ServiceRequest request = requests.get(i);
                ps.setString(1, request.getCreatedAt());
ps.setString(2, request.getDueDate());
ps.setString(3, request.getSampleReceivedDateTime());
ps.setDouble(4, request.getCharge()); // Set charge (assuming it's a numeric type)
ps.setString(5, request.getOccurence());
ps.setLong(6, request.getId()); // ID (Generated)
ps.setString(7, request.getBodySite());
ps.setString(8, request.getEncounterId().toString());
ps.setString(9, request.getPatientId());
ps.setString(10, request.getPractitionerId().toString());
ps.setString(11, request.getVisitId());
ps.setString(12, "161380e9-22d3-4627-a97f-0f918ce3e4a9"); // service_provider_id
ps.setString(13, request.getUuid()); // UUID
ps.setString(14, request.getDisplay());
ps.setString(15, request.getCategory()==null?"Laboratory-procedure":request.getCategory() );
ps.setString(16, request.getCode());
ps.setString(17, request.getDiagnosticServiceSection());
ps.setString(18, request.getPurpose());
ps.setString(19, request.getPriority());
ps.setString(20, request.getHealthcareServiceId());
ps.setString(21, request.getHealthcareServiceName());
ps.setString(22, request.getChargeItemId());
ps.setString(23, request.getStatus());
ps.setString(24, request.getStatusReason());
ps.setString(25, request.getGroupIdentifier());
ps.setString(26, request.getIntent()); // Placeholder for intent
ps.setString(27, request.getPractitionerName());
ps.setString(28, request.getPatientMrNumber());
ps.setString(29, request.getPatientMobile());
ps.setString(30, request.getPatientBirthDate());
ps.setString(31, request.getPatientGender());
ps.setString(32, request.getPatientFullName());
ps.setString(33, "ambulatory"); // encounter_class
ps.setString(34, request.getNote());

                

            }

            @Override
            public int getBatchSize() {
                // TODO Auto-generated method stub
                return requests.size();
            }

        });
    

    }



 

    public void clean(){
        String sql="""
                update service_request 
set priority='routine'
where service_request.priority is null ;
                """;
        vectorJdbcTemplate.update(sql);

        sql ="""
                
                update service_request set visitid = e.visit_id
from encounter e 
where encounterid= e.uuid;
                """;
                vectorJdbcTemplate.update(sql);
    }



      public void migrateThread(int batchSize) {
  
    
    long rows = serviceRequestRepository.count();
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
           migrateServiceRequestToSerenity(serviceRequestRepository.findOffset(startIndex, batchSize));
            return 1;
        });
    }

    return callables;
}



public void migrate(int batchSize) {
    long rows = serviceRequestRepository.getParactionerIdCount();
    long batches = (rows + batchSize - 1) / batchSize; // Safe ceiling division
    
    logger.info("Total batches: {}", batches);
    for (int i = 0; i < batches; i++) {
        final int batchNumber = i;

        
            int startIndex = batchNumber * batchSize;
       migrateServiceRequestToSerenity(serviceRequestRepository.findOffset(startIndex, batchSize));
           
        }
    

    
}





}
