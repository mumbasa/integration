package com.serenity.integration.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

import com.serenity.integration.models.AllergyIntolerance;
import com.serenity.integration.models.Doctors;
import com.serenity.integration.models.PatientData;
import com.serenity.integration.models.ServiceRequest;
import com.serenity.integration.repository.AllergyRepository;
import com.serenity.integration.repository.DoctorRepository;
import com.serenity.integration.repository.PatientRepository;
import com.serenity.integration.repository.ServiceRequestRepository;

@Service
public class AllergyService {

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
    AllergyRepository allergyRepository;

    @Autowired
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;

    Logger logger = LoggerFactory.getLogger(getClass());


    public void getLegacyAllergies(int batchSize) {
       // Map<String, PatientData> mps = patientRepository.findAll().stream()
         //       .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));

        String sql = "Select count(*) from allergy_intolerance ";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<AllergyIntolerance> serviceRequests = new ArrayList<AllergyIntolerance>();

            int startIndex = i * batchSize;
            String sqlQuery = """
SELECT a."uuid", a.created_at, a.is_deleted, a.modified_at, a.id, a."type", a.category, clinical_status, verification_status, code, last_occurrence, e.id as encounter_id,e.visit_id,p.uuid as  patient_id, e.chief_complaint_author_id,e.history_of_presenting_illness_author_id 
FROM allergy_intolerance a left join patient p  on p.id=a.patient_id left join encounter e  on e.id=a.encounter_id  
            order by a.id asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                AllergyIntolerance request = new AllergyIntolerance();
              //  request.setId(set.getLong("id"));
                request.setUuid(set.getString("id"));
                request.setCreatedAt(set.getString("created_at"));
                request.setRecordedDate(set.getString("last_occurrence"));
                request.setUpdatedAt(set.getString("modified_at"));
                request.setCategory(set.getString("type"));
                request.setAllergyIntoleranceType(set.getString("type"));
                request.setClinicalStatus(set.getString("clinical_status"));
                request.setVerificationStatus(set.getString("verification_status"));
                request.setCode(set.getString("code"));
                request.setServiceProviderId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                String encounterId = set.getString("encounter_id");
                if (encounterId != null) {
                    request.setEncounterId(encounterId);
                }
             
                 request.setPatientId(set.getString("patient_id"));            
                            
            String practitionerId =set.getString("chief_complaint_author_id")==null?set.getString("history_of_presenting_illness_author_id"):set.getString("chief_complaint_author_id");
               if(practitionerId !=null){
              //  System.err.println(practitionerId+"----------");
                request.setPractitionerId(practitionerId);
               }
                request.setVisitId(set.getString("visit_id"));
                serviceRequests.add(request);

            }
            allergyRepository.saveAll(serviceRequests);
            logger.info("Saved Allergy");
        }
        logger.info("Cleaning Requests");
       cleanAllergies();

    }

    public int migrateAllergy(List<AllergyIntolerance> allergies){
        String sql ="""
                INSERT INTO allergy_intolerances
(created_at,  "type", onset_period_start, onset_period_end, recorded_date, 
pk, encounter_id, service_provider_id, patient_id, visit_id, 
"uuid", clinical_status, verification_status, category, criticality,
 code, display, practitioner_id, practitioner_name,updated_at)
VALUES( ?::timestamp, ?, ?::timestamp, ?::timestamp, cast (? AS DATE), 
?, uuid(?), uuid(?), uuid(?), uuid(?), 
uuid(?), ?, ?, ?, ?, 
?, ?, uuid(?), ?,?::timestamp);
                """;

    serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            // TODO Auto-generated method stub
           AllergyIntolerance intolerance = allergies.get(i);
       
           ps.setString(1,intolerance.getCreatedAt());
           ps.setString(2,    intolerance.getAllergyIntoleranceType());
           ps.setString(3,     intolerance.getOnsetPeriodEnd());
           ps.setString(4,intolerance.getRecordedDate());
           ps.setString(5,intolerance.getRecordedDate());

           ps.setLong(6,intolerance.getId());
           ps.setString(7,    intolerance.getEncounterId());
           ps.setString(8,     intolerance.getServiceProviderId());
           ps.setString(9,intolerance.getPatientId());
           ps.setString(10,intolerance.getVisitId());

           ps.setString(11,intolerance.getUuid());
           ps.setString(12,    intolerance.getClinicalStatus());
           ps.setString(13,     intolerance.getVerificationStatus());
           ps.setString(14,intolerance.getCategory());
           ps.setString(15,intolerance.getCriticality());

           ps.setString(16,intolerance.getCode());
           ps.setString(17,    intolerance.getCode());
           ps.setString(18,     intolerance.getPractitionerId()==null?"4c4db061-1f31-4a7b-b5fc-04b6501fc3cf":intolerance.getPractitionerId());
           ps.setString(19,intolerance.getPractitionerName()==null?"":intolerance.getPractitionerName());
           ps.setString(20,intolerance.getUpdatedAt());


    




        }

        @Override
        public int getBatchSize() {
            // TODO Auto-generated method stub
           return allergies.size();
        }
        
    })     ;       
return allergies.size();
    }
    public void cleanAllergies(){

        String sql ="""
                update allergy_intolerance 
set practitioner_name =p.fullname 
from doctors p
where allergy_intolerance.practitioner_id =p.serenityuuid ;
                """;
                vectorJdbcTemplate.update(sql);

                sql="""
                        update allergy_intolerance 
set visit_id =v.visit_id 
from encounter v
where allergy_intolerance.encounterid =v.uuid ;
                        """;
                        vectorJdbcTemplate.update(sql);
    }
    


     public void migrateAllergyThread(int batchSize) {

        String clean ="""
                
        update allergy_intolerance 
set practitioner_id =v.assignedtoid ,practitioner_name =v.assignedtoname 
from visits v where v."uuid" =visit_id::uuid
                """;
                vectorJdbcTemplate.update(clean);
        long rows = allergyRepository.count();
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
                migrateAllergy(allergyRepository.findBatch(startIndex, batchSize));
                return 1;
            });
        }

        return callables;
    }
}
