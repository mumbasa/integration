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
             select
  "public"."invoice"."id" AS "id",
  "public"."invoice"."uuid" AS "uuid",
  "public"."invoice"."created_at" AS "created_at",
  "public"."invoice"."modified_at" AS "modified_at",
  "public"."invoice"."patientid" AS "patientid",
  "public"."invoice"."issuerid" AS "issuerid",
  "public"."invoice"."visitid" AS "visitid",
  "public"."invoice"."note" AS "note",
  "public"."invoice"."accountid" AS "accountid",
  "public"."invoice"."payment_method" AS "payment_method",
  "public"."invoice"."cashierid" AS "cashierid",
  "public"."invoice"."status" AS "status",
  "public"."invoice"."settlement_date" AS "settlement_date",
  "public"."invoice"."user_friendly_id" AS "user_friendly_id",
  oca."managing_organization_id" AS "managing_organization_id",
  o."name" as "payer_name" ,
  p.gender as "patient_gender",
  p.birth_date  as "dob",
  p.mr_number as "mr_number",
  p.mobile as "patient_mobile",
 concat(p.first_name,' ',p.last_name) as "patient_name"
FROM
  "public"."invoice"
LEFT JOIN "public"."ChargeItem" AS "ChargeItem - UUID" 
  ON CAST("public"."invoice"."uuid" AS TEXT) = CAST("ChargeItem - UUID"."invoiceid" AS TEXT)
LEFT JOIN "public"."organization_clientaccount" AS oca
  ON "ChargeItem - UUID"."payer_account_id" = oca."uuid"
 left join organization o on o."id" =oca.managing_organization_id
 left join patient p  on p."uuid" =uuid(invoice.patientid) 
 order by id asc offset ? limit ?

                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                PatientInvoice request = new PatientInvoice();
                request.setId(set.getLong("id"));
                request.setUuid(set.getString("uuid"));
                request.setUpdatedAt(set.getString("modified_at"));
                request.setCreatedAt(set.getString("created_at"));
                request.setPatientId(set.getString("patientid"));
                request.setVisitId(set.getString("visitid"));
                request.setPayerId(set.getString("owner_id"));
                request.setPaymentMethod(set.getString("payment_method"));
                request.setPayerName(set.getString("payer_name"));
                request.setExternalSystem("opd");
                request.setNote(set.getString("note"));
                request.setPatientBirthDate(set.getString("dob"));
                request.setPatientGender(set.getString("patient_gender"));
                request.setPatientMobile(set.getString("patient_mobile"));
                request.setPatientMrNumber(set.getString("mr_number"));
                request.setPatientName(set.getString("patient_name"));
                request.setManagingOrganizationId(set.getString("managing_organization_id"));
                request.setCurrency(set.getString("currency")==null?"GHS":set.getString("currency"));
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
?,?::date,?,?,?,
uuid(?),uuid(?),?,?,uuid(?),
?,?::date,?,?::date,?,
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
           ps.setString(10,intolerance.getPayerName()==null?"":intolerance.getPayerName());

           ps.setString(11,intolerance.getManagingOrganizationId());
           ps.setString(12, intolerance.getPayerId()==null?"":intolerance.getPayerId());
           ps.setString(13, intolerance.getPayerType());
           ps.setString(14,intolerance.getCurrency());
           ps.setString(15,intolerance.getVisitId());

           ps.setString(16,intolerance.getPaymentMethod());
           ps.setString(17,intolerance.getInvoiceDate());
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
            List<Future<Integer>> futures = executorService.invokeAll(submitTask2(batchSize, 100));
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
