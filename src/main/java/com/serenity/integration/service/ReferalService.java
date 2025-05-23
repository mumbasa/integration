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

import com.serenity.integration.models.AllergyIntolerance;
import com.serenity.integration.models.Doctors;
import com.serenity.integration.models.PatientData;
import com.serenity.integration.models.Referal;
import com.serenity.integration.repository.DoctorRepository;
import com.serenity.integration.repository.EncounterNoteRepository;
import com.serenity.integration.repository.PatientRepository;
import com.serenity.integration.repository.ReferralRepository;
import com.serenity.integration.repository.VisitRepository;

@Service
public class ReferalService {
    @Autowired
    @Qualifier(value = "hisJdbcTemplate")
    JdbcTemplate hisJdbcTemplate;

    @Autowired
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;

    @Autowired
    @Qualifier("vectorJdbcTemplate")
    JdbcTemplate vectorJdbcTemplate;

    @Autowired
    EncounterNoteRepository encounterNoteRepository;
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    VisitRepository visitRepository;

    @Autowired
    @Qualifier(value = "legJdbcTemplate")
    JdbcTemplate legJdbcTemplate;

    @Autowired
    ReferralRepository referralRepository;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    DoctorRepository doctorRepository;


     public void getLegacyReferral(int batchSize) {
     //   Map<String, PatientData> mps = patientRepository.findAll().stream()
       //         .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));

         //       Map<String, Doctors> doctorMap = doctorRepository.findHisPractitioners().stream()
          //      .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));
        
        String sql = "SELECT count(*) from referral_request";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<Referal> serviceRequests = new ArrayList<Referal>();

            int startIndex = i * batchSize;
            String sqlQuery = """
                    SELECT rr.id, rr."uuid", rr.created_at, rr.is_deleted, rr.modified_at, rr.priority, recipient_extra_detail, rr.specialty, reason, description, referral_type, rr.status, encounter_id, p.uuid as patient_id, recipient_id,concat(pd.first_name,' ',pd.last_name) as recipient_name,replaces_id, requester_id, visit_id,concat(pr.title ,' ',pr.first_name,' ',pr.last_name) as full_name,requesting_organization_id
FROM public.referral_request rr left join patient p on p.id=rr.patient_id left join encounter e on e.id=rr.encounter_id left join practitioner_role pr on pr.id=requester_id left join practitioner_role pd on pd.id=recipient_id   order by rr.id  asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                Referal request = new Referal();
                request.setId(set.getLong("id"));
                request.setUuid(set.getString("uuid"));
                request.setCreatedAt(set.getString("created_at"));
                request.setUpdatedAt(set.getString("modified_at"));
                request.setDescription(set.getString("description"));
                request.setPriority(set.getString("priority"));
                request.setReason(set.getString("reason"));
                request.setRecipientExtraDetail(set.getString("recipient_extra_detail"));
                request.setReferralType(set.getString("referral_type"));
                request.setStatus(set.getString("status"));
                request.setSpecialty(set.getString("specialty"));
                request.setRecipientId(set.getString("recipient_id"));
                request.setRecipientName(set.getString("recipient_name"));
                request.setRequesterId(set.getString("requester_id"));
                request.setRequesterName(set.getString("full_name"));
                request.setRequestingOrganizationId(set.getString("requesting_organization_id"));
                request.setReplacesId(set.getString("replaces_id"));
                String encounterId = set.getString("encounter_id");
                if (encounterId != null) {
                    request.setEncounterId(encounterId);
                }
               
                request.setPatientId(set.getString("patient_id"));            
                
                
            
                              serviceRequests.add(request);

            }
            referralRepository.saveAll(serviceRequests);
            logger.info("Saved Allergy");
        }
        logger.info("Cleaning Requests");
        setRequesterName();

    }
    

    private void migrateReferal(List<Referal> referals){
        String sql ="""
                INSERT INTO referral_requests
(created_at, updated_at, pk, encounter_id, patient_id, 
requester_id, requesting_organization_id, recipient_id, "uuid", priority, 
specialty, reason, description, referral_type, status, 
recipient_extra_detail, recipient_name, replaces_id,  requester_name)
VALUES(?::timestamp,?::timestamp,?,uuid(?),uuid(?),
uuid(?),uuid(?),uuid(?),uuid(?),?,
?,?,?,?,?,
?,?,uuid(?),?
);
                """;
serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
     
        Referal referal = referals.get(i);
        ps.setString(1, referal.getCreatedAt());
        ps.setString(2, referal.getUpdatedAt());
        ps.setLong(3, referal.getId());
        ps.setString(4, referal.getEncounterId());
        ps.setString(5, referal.getPatientId());
        ps.setString(6, referal.getRequesterId());
        ps.setString(7, "161380e9-22d3-4627-a97f-0f918ce3e4a9");
        ps.setString(8, referal.getRecipientId());
        ps.setString(9, referal.getUuid());
        ps.setString(10, referal.getPriority());
        ps.setString(11, referal.getSpecialty());
        ps.setString(12, referal.getReason());
        ps.setString(13, referal.getDescription());
        ps.setString(14, referal.getReferralType().replaceAll("_"," "));
        ps.setString(15, referal.getStatus());

        ps.setString(16, referal.getRecipientExtraDetail());
        ps.setString(17, referal.getRecipientExtraDetail()==null?referal.getRecipientName():referal.getRecipientExtraDetail());
        ps.setString(18, referal.getReplacesId());
        ps.setString(19, referal.getRequesterName());

        


    }

    @Override
    public int getBatchSize() {
        // TODO Auto-generated method stub
return referals.size();
    }
    
});
    }
    
    public void setRequesterName(){
        String sql ="""
                update referrals
set requester_name =p.fullname 
from doctors p
where referrals.requester_id =p.externalid ;
                """;
vectorJdbcTemplate.execute(sql);

    }
    
    public void migrateReferalThread(int batchSize) {

        long rows = referralRepository.count();
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
                migrateReferal(referralRepository.findBhy(startIndex, batchSize));
                return 1;
            });
        }

        return callables;
    }



}
