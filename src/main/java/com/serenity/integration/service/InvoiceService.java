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
import com.serenity.integration.models.PatientInvoice;
import com.serenity.integration.models.ServiceRequest;
import com.serenity.integration.repository.AllergyRepository;
import com.serenity.integration.repository.DoctorRepository;
import com.serenity.integration.repository.InvoiceRepository;
import com.serenity.integration.repository.PatientRepository;
import com.serenity.integration.repository.ServiceRequestRepository;

@Service
public class InvoiceService {

    @Autowired
    InvoiceRepository invoiceRepository;

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


    public void getLegacyInvoice(int batchSize) {
        Map<String, PatientData> mps = patientRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getUuid(), e -> e));

        String sql = "Select count(*) from invoice ";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<PatientInvoice> serviceRequests = new ArrayList<PatientInvoice>();

            int startIndex = i * batchSize;
            String sqlQuery = """
                   select *,(select currency from "ChargeItem" ci where uuid(invoiceid)=i."uuid" limit 1) from invoice i  ORDER by i.id asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                PatientInvoice request = new PatientInvoice();
                PatientData patientData=mps.get(set.getString("patientid"));
                request.setId(set.getLong(1));
                request.setUuid(set.getString(2));
                request.setCreatedAt(set.getString("created_at"));
                request.setPatientId(set.getString("patientid"));
                request.setVisitId(set.getString("visitid"));
                request.setPaymentMethod(set.getString("payment_method"));
                request.setExternalSystem("opd");
                request.setPatientBirthDate(patientData.getBirthDate());
                request.setPatientGender(patientData.getGender());
                request.setPatientMobile(patientData.getMobile());
                request.setPatientMrNumber(patientData.getMrNumber());
                request.setPatientName(patientData.getFullName());
                request.setExternalId(set.getString(2));
                request.setManagingOrganizationId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                request.setVisitId(set.getString("visitid"));
                request.setCurrency(set.getString(16));
                serviceRequests.add(request);

            }
            invoiceRepository.saveAll(serviceRequests);
            logger.info("Saved Invoice");
        }
        logger.info("Cleaning Requests");
     

    }

    public int migrateInvoice(List<AllergyIntolerance> allergies){
        String sql ="""
                INSERT INTO allergy_intolerances
(created_at,  "type", onset_period_start, onset_period_end, recorded_date, 
pk, encounter_id, service_provider_id, patient_id, visit_id, 
"uuid", clinical_status, verification_status, category, criticality,
 code, display, practitioner_id, practitioner_name)
VALUES( ?::timestamp, ?, ?::timestamp, ?::timestamp, cast (? AS DATE), 
?, uuid(?), uuid(?), uuid(?), uuid(?), 
uuid(?), ?, ?, ?, ?, 
?, ?, uuid(?), ?);
                """;

    serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            // TODO Auto-generated method stub
           AllergyIntolerance intolerance = allergies.get(i);
       
           ps.setString(1,intolerance.getRecordedDate());
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
           ps.setString(17,    intolerance.getDisplay());
           ps.setString(18,     intolerance.getPractitionerId()==null?"4c4db061-1f31-4a7b-b5fc-04b6501fc3cf":intolerance.getPractitionerId());
           ps.setString(19,intolerance.getPractitionerName()==null?"":intolerance.getPractitionerName());
    




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
    


     public void migrateinvoiceThread(int batchSize) {

        long rows = invoiceRepository.count();
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
                migrateInvoice(allergyRepository.findBatch(startIndex, batchSize));
                return 1;
            });
        }

        return callables;
    }
}
