package com.serenity.integration.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
import com.serenity.integration.models.ChargeItem;
import com.serenity.integration.models.Doctors;
import com.serenity.integration.models.PatientData;
import com.serenity.integration.models.PatientInvoice;
import com.serenity.integration.models.ServiceRequest;
import com.serenity.integration.repository.AllergyRepository;
import com.serenity.integration.repository.ChargeItemRepository;
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
    ChargeItemRepository chargeItemRepository;

    @Autowired
    DoctorRepository doctorRepository;


    @Autowired
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;

    Logger logger = LoggerFactory.getLogger(getClass());


    public void getLegacyInvoice(int batchSize) {
    
        Set<String> ids = new HashSet();


        Map<String, PatientData> mps = patientRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getUuid(), e -> e));

        String sql = "Select count(*) from \"ChargeItem\" as ci where invoiceid is not null and visit_id is not null and payment_method  !='cash'";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);
        logger.info("row are :---"+rows);
        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<PatientInvoice> serviceRequests = new ArrayList<PatientInvoice>();

            int startIndex = i * batchSize;
            String sqlQuery = """
             SELECT
    MIN(i.uuid::text) AS invoice_uuid,
    MIN(i.created_at) AS created_at,
    MIN(i.modified_at) AS updated_at,
    p.uuid AS patient_id,
    MIN(p.first_name || ' ' || p.last_name) AS patient_name,
    MIN(p.mr_number) AS patient_mr_number,
    MIN(p.birth_date) AS patient_birth_date,
    MIN(p.gender) AS patient_gender,
    MIN(p.mobile) AS patient_mobile,
    MIN(o.name) AS payer_name,
    ci.provider_id AS managing_organization_id,
    o.uuid AS payer_id,
    CASE
        WHEN o.uuid IS NOT NULL THEN 'ORGANIZATION'
        ELSE 'PATIENT'
    END AS payer_type,
    MIN(ci.currency) AS currency,
    ci.visitid AS visit_id,
    MIN(i.payment_method) AS payment_method,
    CAST(MIN(i.created_at) AS DATE) AS invoice_date,
    MIN(i.user_friendly_id) AS external_id,
    'opd' AS external_system
FROM
    "public"."ChargeItem" ci
JOIN
    "public"."patient" p ON ci.patient_id = p.id
JOIN
    "public"."invoice" i ON ci.invoiceid::uuid = i.uuid
LEFT JOIN
    "public"."organization_clientaccount" ca ON ci.payer_account_id::uuid = ca.uuid
LEFT JOIN
    "public"."organization" o ON ca.owner_id = o.id
LEFT JOIN
    "public"."finance_transaction" t ON t.charge_item_id = ci.id
WHERE
    ci.status IN ('billed', 'paid')

ORDER BY
    MIN(i.created_at) desc offset ? limit ?;
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                PatientInvoice request = new PatientInvoice();
             
                request.setUuid(set.getString("invoice_uuid"));
                request.setUpdatedAt(set.getString("updated_at"));
                request.setCreatedAt(set.getString("created_at"));
                request.setPatientId(set.getString("patient_id"));
                request.setVisitId(set.getString("visit_id"));
                request.setPayerId(set.getString("payer_id"));
                request.setPaymentMethod(set.getString("payment_method"));
                request.setPayerName(set.getString("payer_name"));
                request.setDueDate(set.getString("due_date"));
                request.setExternalSystem(set.getString("external_system"));
                request.setExternalId(set.getString("external_id"));
                request.setInvoiceDate(set.getString("invoice_date"));
                request.setAmountPaid(set.getDouble("amount_paid"));
                request.setPayerType(set.getString("payer_type"));
                request.setPatientBirthDate(set.getString("patient_birth_date"));
                request.setPatientGender(set.getString("patient_gender"));
                request.setPatientMobile(set.getString("patient_mobile"));
                request.setPatientMrNumber(set.getString("patient_mr_number"));
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

 public void getLegacyChargeItem(int batchSize) {
        Set<String> ids = new HashSet<>();
        String sql = "select count(*) from \"ChargeItem\" ci  where payment_method !='cash' and visit_id is not null and invoiceid is not null";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<PatientInvoice> serviceRequests = new ArrayList<PatientInvoice>();

            int startIndex = i * batchSize;
            String sqlQuery = """
         select 
"ChargeItem"."id" AS "id",
"ChargeItem"."category" AS "category",
"ChargeItem"."charge" AS "charge",
"ChargeItem"."created_on" AS "created_at",
"ChargeItem"."created_on" AS "updated_at",
"ChargeItem"."currency" AS "currency",
"ChargeItem"."invoiceid" AS "invoice_id",
"ChargeItem"."patientid" AS "patient_id",
o.uuid AS "payer_id",
"ChargeItem"."payername" AS "payer_name",
"ChargeItem"."payment_method" AS "payment_method",
"ChargeItem"."practitionerid" AS "practitioner_id",
"ChargeItem"."practitionername" AS "practitioner_name",
'161380e9-22d3-4627-a97f-0f918ce3e4a9' AS "provider_id",
'Nyaho Medical Center' AS "provider_name",
"ChargeItem"."relationship" AS "relationship",
"ChargeItem"."service_or_product_name" AS "service_or_product_name",
"ChargeItem"."servicerequestid" AS "service_request_id",
"ChargeItem"."status" AS "status",
"ChargeItem"."uuid" AS "uuid",
"ChargeItem"."visit_id" AS "visit_id",
"ChargeItem"."patient_contribution" AS "patient_contribution",
"ChargeItem"."user_friendly_id" AS "user_friendly_id",
"ChargeItem"."created_by" AS "created_by_name",
"ChargeItem"."revenue_tag_display" AS "revenue_tag_display",
"ChargeItem"."paid_at" AS "paid_at",
"ChargeItem"."payer_contribution" AS "payer_contribution",
 CASE
        WHEN o.uuid IS NOT NULL THEN 'ORGANIZATION'
        ELSE 'PATIENT'
    END AS payer_type
FROM
"ChargeItem"  left JOIN "encounter" AS "Encounter" ON "ChargeItem"."id" = "Encounter"."charge_item_id" 
LEFT JOIN
    "public"."organization_clientaccount" ca ON "ChargeItem".payer_account_id::uuid = ca.uuid
LEFT JOIN
    "public"."organization" o ON ca.owner_id = o.id
    where payment_method != 'cash' and "ChargeItem".visit_id is not null and invoiceid is not null 
order by "ChargeItem".id
            offset ? limit ?
            """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                if(!ids.contains((set.getString("invoice_id")))){

                PatientInvoice request = new PatientInvoice();
       
                request.setUuid(set.getString("invoice_id"));
                request.setCurrency(set.getString("currency"));
                request.setVisitId(set.getString("visit_id"));
                request.setPatientId(set.getString("patient_id"));
                request.setManagingOrganizationId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                request.setPayerId(set.getString("payer_id"));
                request.setPayerName(set.getString("payer_name"));            
                request.setCreatedAt(set.getString("created_at"));
                request.setUpdatedAt(set.getString("updated_at"));
                request.setInvoiceDate(set.getString("created_at"));
               request.setUpdatedAt(set.getString("updated_at"));
                request.setPaymentMethod(set.getString("payment_method"));
                request.setPayerType(set.getString("payer_type"));
                request.setPayerId(set.getString("payer_id"));
                
               
                serviceRequests.add(request);
            ids.add(set.getString("invoice_id"));
            }
             

            }
           invoiceRepository.saveAll(serviceRequests);
         
           // migrateChargeItems(serviceRequests);
            logger.info("Saved chargeItem");
            cleanItems();
            logger.info("Cleaning Requests");

        }
     

    }

    public void cleanItems(){
        String sql = """
             update patient_invoice set patientmrnumber =p.mrnumber ,patientbirthdate=p.birthdate,patientgender=p.gender,patientmobile=p.mobile,patientname=concat(p.firstname,' ',p.lastname)
        from patient_information p
        where patientid = p.uuid and   patientmrnumber is  null
                """;
        
                vectorJdbcTemplate.update(sql);
    }




    public int migrateInvoice(List<PatientInvoice> invoices){
        String sql ="""
               INSERT INTO invoices
(created_at,  external_system, "uuid", patient_id, patient_name, 
patient_mr_number, patient_birth_date, patient_gender, patient_mobile, payer_name, 
managing_organization_id, payer_id, payer_type, currency, visit_id, 
payment_method, invoice_date,
 updated_at)
VALUES( 
?::timestamp,?,uuid(?),?,?,
?,?::date,?,?,?,
uuid(?),?,?,?,uuid(?),
?,?::date,
now()
);
                """;

    serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            // TODO Auto-generated method stub
           PatientInvoice intolerance = invoices.get(i);
       
           ps.setString(1,intolerance.getCreatedAt());
           ps.setString(2,   "opd");
           ps.setString(3,     intolerance.getUuid());
           ps.setString(4,intolerance.getPatientId());
           ps.setString(5,intolerance.getPatientName());
           ps.setString(6,intolerance.getPatientMrNumber());
           ps.setString(7,    intolerance.getPatientBirthDate()==null?LocalDate.now().toString():intolerance.getPatientBirthDate());
           ps.setString(8,     intolerance.getPatientGender());
           ps.setString(9,intolerance.getPatientMobile());
           ps.setString(10,intolerance.getPayerName()==null?"":intolerance.getPayerName());

           ps.setString(11,intolerance.getManagingOrganizationId());
           ps.setString(12, intolerance.getPayerId()==null?"":intolerance.getPayerId());
           ps.setString(13, intolerance.getPayerType().toLowerCase());
           ps.setString(14,intolerance.getCurrency());
           ps.setString(15,intolerance.getVisitId());

           ps.setString(16,intolerance.getPaymentMethod());
           ps.setString(17,intolerance.getCreatedAt());
        
 
        }

        @Override
        public int getBatchSize() {
            // TODO Auto-generated method stub
           return invoices.size();
        }
        
    })     ;       
return invoices.size();
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

    public void invoiceDump(){

String sql ="""
 INSERT INTO public.invoices (
  created_at,
  updated_at,
  uuid,
  patient_id,
  patient_name,
  patient_mr_number,
  patient_birth_date,
  patient_gender,
  patient_mobile,
  payer_name,
  managing_organization_id,
  payer_id,
  payer_type,
  currency,
  visit_id,
  payment_method,
  invoice_date,
  amount_paid,
  due_date,
  external_id,
  external_system
)
SELECT
  MIN(c.created_at) AS created_at,
  MAX(c.updated_at) AS updated_at,
  c.invoice_id AS uuid,
  c.patient_id,
  c.patient_name,
  c.patient_mr_number,
  CAST(c.patient_birth_date AS DATE) AS patient_birth_date,
  c.patient_gender,
  c.patient_mobile,
  c.payer_name,
  c.account_id AS managing_organization_id,
  c.payer_id,
  'organization' AS payer_type,
  c.currency,
  c.visit_id,
  c.payment_method,
  MIN(c.created_at) AS invoice_date,
  SUM(c.charge) AS amount_paid,
  c.paid_at AS due_date,
  c.transaction_id AS external_id,
  'chargeitem_migration' AS external_system
FROM
  public.chargeitem c
WHERE
  c.status = 'paid' AND c.invoice_id IS NOT NULL
GROUP BY
  c.invoice_id,
  c.patient_id,
  c.patient_name,
  c.patient_mr_number,
  CAST(c.patient_birth_date AS DATE),
  c.patient_gender,
  c.patient_mobile,
  c.payer_name,
  c.account_id,
  c.payer_id,
  c.currency,
  c.visit_id,
  c.payment_method,
  c.transaction_id,
  c.paid_at;



        """;

        serenityJdbcTemplate.update(sql);
    
    
    }

    public void da(){

        String sql = "select count(*) from \"ChargeItem\" ci where payment_method !='cash' and visit_id is not null";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);
        System.err.println(rows);

    }

}
