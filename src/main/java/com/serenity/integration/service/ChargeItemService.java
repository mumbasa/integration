package com.serenity.integration.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.serenity.integration.models.EncounterNote;
import com.serenity.integration.models.PatientData;
import com.serenity.integration.models.ServiceRequest;
import com.serenity.integration.repository.AllergyRepository;
import com.serenity.integration.repository.ChargeItemRepository;
import com.serenity.integration.repository.DoctorRepository;
import com.serenity.integration.repository.PatientRepository;
import com.serenity.integration.repository.ServiceRequestRepository;

@Service
public class ChargeItemService {

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
    ChargeItemRepository chargeItemRepository;

    @Autowired
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;

    Logger logger = LoggerFactory.getLogger(getClass());


    public void getLegacyChargeItem(int batchSize) {
        
        String sql = "select count(*) from \"ChargeItem\" ci";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<ChargeItem> serviceRequests = new ArrayList<ChargeItem>();

            int startIndex = i * batchSize;
            String sqlQuery = """
         SELECT
"ChargeItem"."id" AS "id",         
"ChargeItem"."category" AS "category",
"ChargeItem"."charge" AS "charge",
"ChargeItem"."clinic_name" AS "location_name",
"ChargeItem"."clinicid" AS "location_id",
"ChargeItem"."created_on" AS "created_at",
"ChargeItem"."created_on" AS "updated_at",
"ChargeItem"."currency" AS "currency",
"ChargeItem"."invoiceid" AS "invoice_id",
"ChargeItem"."medicationrequestid" AS "medication_request_id",
"ChargeItem"."patient_mobile" AS "patient_mobile",
"ChargeItem"."patientid" AS "patient_id",
"ChargeItem"."patientname" AS "patient_name",
"ChargeItem"."payerid" AS "payer_id",
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
"ChargeItem"."quantity" AS "quantity",
"ChargeItem"."unit_price" AS "unit_price",
"ChargeItem"."uuid" AS "uuid",
"ChargeItem"."policy_id" AS "policy_id",
"ChargeItem"."service_id" AS "service_id",
"ChargeItem"."visit_id" AS "visit_id",
"ChargeItem"."patient_contribution" AS "patient_contribution",
"ChargeItem"."user_friendly_id" AS "user_friendly_id",
"ChargeItem"."created_by" AS "created_by_name",
"ChargeItem"."revenue_tag_display" AS "revenue_tag_display",
"ChargeItem"."paid_at" AS "paid_at",
"ChargeItem"."payer_contribution" AS "payer_contribution",
 "Encounter"."id" AS "encounter_id"
FROM
"ChargeItem" left JOIN "encounter" AS "Encounter" ON "ChargeItem"."id" = "Encounter"."charge_item_id"  order by "ChargeItem".id offset ? limit ?
            """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                ChargeItem request = new ChargeItem();
                request.setId(set.getLong("id"));
                request.setUuid(set.getString("uuid"));
                request.setCharge(set.getDouble("charge"));
                request.setCurrency(set.getString("currency"));
                request.setUnitPrice(set.getDouble("unit_price"));
                request.setCategory(set.getString("category"));
                request.setVisitId(set.getString("visit_id"));
                request.setLocationId(set.getString("location_id")==null?"2f7d4c40-fe53-491d-877b-c2fee7edc1f2":set.getString("location_id"));
                request.setLocationName(set.getString("location_name")==null?"Airport Main":set.getString("location_name"));
                request.setPatientId(set.getString("patient_id"));
                request.setLocationName(set.getString("location_name"));
                request.setProviderId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                request.setProviderName("Nyaho Medical Center");
                request.setQuantity(set.getInt("quantity"));
                request.setServiceId(set.getString("service_id"));
                request.setServiceOrProductName(set.getString("service_or_product_name"));
                request.setServiceRequestId(set.getString("service_request_id"));
                request.setUserFriendlyId(set.getString("user_friendly_id"));
                request.setRevenueTagDisplay(set.getString("revenue_tag_display"));
               request.setPatientContribution(set.getDouble("patient_contribution"));
               request.setRelationship(set.getString("relationship"));
               request.setPractitionerId(set.getString("practitioner_id"));
                request.setCreatedAt(set.getString("created_at"));
                request.setEncounterId(set.getString("encounter_id"));
               request.setPractitionerName(set.getString("practitioner_name"));
               request.setUpdatedAt(set.getString("updated_at"));
               request.setPaidAt(set.getString("paid_at"));
                
             
              
                serviceRequests.add(request);

            }
           // chargeItemRepository.saveAll(serviceRequests);
           migrateChargeitems(serviceRequests);
            logger.info("Saved chargeItem");
        }
        logger.info("Cleaning Requests");
       

    }


    public int getLegacyChargeItemFuture(int batch,int batchSize) {
       
            List<ChargeItem> serviceRequests = new ArrayList<ChargeItem>();

            
            String sqlQuery = """
                    select * from "ChargeItem" ci order by ci.id asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, batch, batchSize);
            while (set.next()) {
                ChargeItem request = new ChargeItem();
                request.setId(set.getLong("id"));
                request.setUuid(UUID.randomUUID().toString());
                request.setCharge(set.getDouble("charge"));
                request.setCurrency(set.getString("currency"));
                request.setUnitPrice(set.getDouble("unit_price"));
                request.setCategory(set.getString("category"));
                request.setVisitId(set.getString("visit_id"));
                request.setLocationId(set.getString("clinicid"));
                request.setLocationName(set.getString("clinic_name"));
                request.setProviderId(set.getString("provider_id"));
                request.setProviderName(set.getString("providername"));
                request.setQuantity(set.getInt("quantity"));
                request.setServiceId(set.getString("serviceid"));
                request.setServiceOrProductName(set.getString("service_or_product_name"));
                request.setServiceRequestId(set.getString("servicerequestid"));
                request.setUserFriendlyId(set.getString("user_friendly_id"));
                request.setRevenueTagDisplay(set.getString("revenue_tag_display"));
               request.setPatientContribution(set.getDouble("patient_contribution"));
               request.setRelationship(set.getString("relationship"));
               request.setPractitionerId(set.getString("practitionerid"));
                request.setPractitionerName(set.getString("practitionername"));
               request.setPayerContribution(set.getDouble("requestername"));
                request.setPayerName(set.getString("payer_name"));
                request.setPatientId(set.getString("patientid"));
                request.setInvoiceId(set.getString("invoiceid"));
                request.setCreatedAt(set.getString("created_on"));
                request.setPaidAt(set.getString("paid_at"));
                request.setPaymentMethod(set.getString("payment_method"));
                request.setStatus(set.getString("status"));
                request.setTransactionId(set.getLong("transactionid"));
                serviceRequests.add(request);

            }
            chargeItemRepository.saveAllAndFlush(serviceRequests);
            logger.info("Saved chargeItem");
            return batchSize;
        }
      


     public void chargeThread(int batchSize) {
    
    
    long rows = chargeItemRepository.count();
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
           try{
            migrateChargeitems(chargeItemRepository.findBhy(startIndex, batchSize));
            
           }catch (Exception e){
            e.printStackTrace();

           }
           return 1;
        });
    }

    return callables;
}


public void migrateChargeitems(List<ChargeItem> items){
String sql ="""
        INSERT INTO chargeitem
(created_at,  charge, patient_contribution, payer_contribution, unit_price, 
pk, "uuid", category, created_by_name, currency, 
encounter_id, location_id, location_name, medication_request_id, practitioner_id, 
practitioner_name, product_id, provider_id, provider_name,quantity, 
revenue_tag_display, relationship, service_id, service_or_product_name, service_request_id,
  visit_id, user_friendly_id, invoice_id, paid_at, patient_id, 
  appointment_id, payer_name, payment_method,status,updated_at)
  VALUES (
  ?::timestamp,?,?,?,?,
  ?,uuid(?),?,?,?,
  ?,?,?,?,?,
  ?,?,?,?,?,
  ?,?,?,?,?,
  ?,?,?,?::timestamp,?,
  ?,?,?,? ,now()
  )
"""    
        ;
    serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            ChargeItem item = items.get(i);
            ps.setString(1, item.getCreatedAt());
            ps.setDouble(2, item.getCharge());
            ps.setDouble(3, item.getPatientContribution());
            ps.setDouble(4, item.getUnitPrice());
            ps.setDouble(5, item.getPayerContribution());

            ps.setLong(6, item.getId());
            ps.setString(7,item.getUuid());
            ps.setString(8, item.getCategory());
            ps.setString(9, item.getCreatedByName());
            ps.setString(10, item.getCurrency());

            ps.setString(11, item.getEncounterId());
            ps.setString(12,item.getLocationId()==null?"2f7d4c40-fe53-491d-877b-c2fee7edc1f2":item.getLocationId());
            ps.setString(13, item.getLocationName()==null?"Airport Main":item.getLocationName());
            ps.setString(14, item.getMedicationRequestId());
            ps.setString(15, item.getPractitionerId());

            ps.setString(16, item.getPractitionerName());
            ps.setString(17,item.getProductId());
            ps.setString(18, item.getProviderId()==null?"":item.getProviderId());
            ps.setString(19, item.getProviderName()==null?"":item.getProviderName());
            ps.setInt(20, item.getQuantity());

            ps.setString(21, item.getRevenueTagDisplay());
            ps.setString(22,item.getRelationship());
            ps.setString(23, item.getServiceId()==null?"":item.getServiceId());
            ps.setString(24, item.getServiceOrProductName()==null?"":item.getServiceOrProductName());
            ps.setString(25, item.getServiceRequestId());

            ps.setString(26, item.getVisitId());
            ps.setString(27, item.getUserFriendlyId()==null?"":item.getUserFriendlyId());
            ps.setString(28, item.getInvoiceId()==null?"":item.getInvoiceId());
            ps.setString(29, item.getPaidAt());
            ps.setString(30, item.getPatientId());

            ps.setString(31, item.getAppointmentId());
            ps.setString(32, item.getPayerName()==null?"":item.getPayerName());
            ps.setString(33, item.getPaymentMethod()==null?"":item.getPaymentMethod());
            ps.setString(34, item.getStatus()==null?"":item.getStatus());

        }

        @Override
        public int getBatchSize() {
            // TODO Auto-generated method stub
            return items.size();
        }
        
    });

}
}
