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
logger.info("row are :---"+rows);
        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<PatientInvoice> serviceRequests = new ArrayList<PatientInvoice>();

            int startIndex = i * batchSize;
            String sqlQuery = """
                   select * from invoice i  ORDER by i.id asc offset ? LIMIT ?
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
              //  request.setCurrency(set.getString(16));
                serviceRequests.add(request);

            }
            invoiceRepository.saveAll(serviceRequests);
            logger.info("Saved Invoice");
        }
        logger.info("Cleaning Requests");
     

    }

    public int migrateInvoice(List<PatientInvoice> invoices){
        String sql ="""
               INSERT INTO invoices
(created_at,  pk, "uuid", patient_id, patient_name, 
patient_mr_number, patient_birth_date, patient_gender, patient_mobile, payer_name, 
managing_organization_id, payer_id, payer_type, currency, visit_id, 
payment_method, invoice_date, amount_paid, due_date, external_id,
 external_system)
VALUES( 
?::timestamp,?,uuid(?),?,?,
?,?,?,?,?,
uuid(?),uuid(?),?,?,uuid(?),
?,?,?,?,?,
?
);
                """;

    serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            // TODO Auto-generated method stub
           PatientInvoice intolerance = invoices.get(i);
       
           ps.setString(1,intolerance.getCreatedAt());
           ps.setLong(2,    intolerance.getId());
           ps.setString(3,     intolerance.getUuid());
           ps.setString(4,intolerance.getPatientId());
           ps.setString(5,intolerance.getPatientName());

           ps.setLong(6,intolerance.getId());
           ps.setString(7,    intolerance.getPatientBirthDate());
           ps.setString(8,     intolerance.getPatientGender());
           ps.setString(9,intolerance.getPatientMobile());
           ps.setString(10,intolerance.getPayerName());

           ps.setString(11,intolerance.getManagingOrganizationId());
           ps.setString(12,    intolerance.getPayerId());
           ps.setString(13,     intolerance.getPayerType());
           ps.setString(14,intolerance.getCurrency());
           ps.setString(15,intolerance.getVisitId());

           ps.setString(16,intolerance.getPaymentMethod());
           ps.setString(17,    intolerance.getInvoiceDate());
           ps.setDouble(18,intolerance.getAmountPaid());
           ps.setString(19,intolerance.getDueDate());
           ps.setString(20, intolerance.getExternalId());
           ps.setString(21, intolerance.getExternalSystem());
    




        }

        @Override
        public int getBatchSize() {
            // TODO Auto-generated method stub
           return invoices.size();
        }
        
    })     ;       
return invoices.size();
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
                migrateInvoice(invoiceRepository.findBatch(startIndex, batchSize));
                return 1;
            });
        }

        return callables;
    }
}
