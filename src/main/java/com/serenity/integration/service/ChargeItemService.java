package com.serenity.integration.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

import com.serenity.integration.models.ChargeItem;
import com.serenity.integration.models.MedicalRequest;
import com.serenity.integration.models.PatientData;
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


    public void getLegacyChargeItem(int batchSize,LocalDate date) {
  
        String sql = "select count(*) from \"ChargeItem\" ci where created_on::date <= ?";
        long rows = legJdbcTemplate.queryForObject(sql,new Object[]{date}, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<ChargeItem> serviceRequests = new ArrayList<ChargeItem>();

            int startIndex = i * batchSize;
            String sqlQuery = """
         SELECT
"ChargeItem"."id" AS "id",
"ChargeItem"."appointmentid" AS "appointment_id",
ps."uuid" AS "stock_item_id",
"ChargeItem"."category" AS "category",
"ChargeItem"."charge" AS "charge",
"ChargeItem"."clinic_name" AS "location_name",
"ChargeItem"."clinicid" AS "location_id",
"ChargeItem"."created_on" AS "created_at",
"ChargeItem"."transactionid" AS "transaction_id",
"ChargeItem"."currency" AS "currency",
"ChargeItem"."invoiceid" AS "invoice_id",
"ChargeItem"."medicationrequestid" AS "medication_request_id",
"ChargeItem"."patient_mobile" AS "patient_mobile",
"ChargeItem"."patientid" AS "patient_id",
"ChargeItem"."patientname" AS "patient_name",
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
 "Encounter"."id" AS "encounter_id",
  cic.uuid  as cancellation_id,
  cic.requested_date_time as cancelation_requested_at,
   cic.requested_by_uuid as requested_by_id,
    cic.requested_by_name as requested_by,
     cic.canceled_date_time as canceled_at,
     cic.approved_by_name as approved_by,
     cic.approved_by_uuid as approved_by_id,
      cic.approved_date_time as approved_at,
       cic.canceled_by_uuid as canceled_by_id,
       cic.canceled_by_name as canceled_by_name,
       cic.reason as reason,
       cashier_name
FROM
"ChargeItem"  left JOIN "encounter" AS "Encounter" ON "ChargeItem"."id" = "Encounter"."charge_item_id"  
left join charge_item_cancelation cic on cic.charge_item_id="ChargeItem".id 
left join pharmacy_stock ps on stock_item_id =ps.id
LEFT JOIN
    "public"."organization_clientaccount" ca ON "ChargeItem".payer_account_id::uuid = ca.uuid
LEFT JOIN
    "public"."organization" o ON ca.owner_id = o.id
where "ChargeItem".created_on::date <= ?
            order by "ChargeItem".id offset ? limit ?
            """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, date,startIndex, batchSize);
            while (set.next()) {
                ChargeItem request = new ChargeItem();

               request.setAppointmentId(set.getString("appointment_id"));
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
                request.setProviderId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                request.setProviderName("Nyaho Medical Center");
                request.setPolicyId(set.getString("policy_id"));
                request.setPayerId(set.getString("payer_id"));
                request.setPayerName(set.getString("payer_name"));
                request.setQuantity(set.getInt("quantity"));
                request.setInvoiceId(set.getString("invoice_id"));
                request.setServiceId(set.getString("service_id"));
                request.setServiceOrProductName(set.getString("service_or_product_name"));
                request.setServiceRequestId(set.getString("service_request_id"));
                request.setUserFriendlyId(set.getString("user_friendly_id"));
                request.setRevenueTagDisplay(set.getString("revenue_tag_display"));
               request.setPatientContribution(set.getDouble("patient_contribution"));
               request.setPayerContribution(set.getDouble("payer_contribution"));

               request.setRelationship(set.getString("relationship"));
               request.setPractitionerId(set.getString("practitioner_id"));
                request.setCreatedAt(set.getString("created_at"));
                request.setEncounterId(set.getString("encounter_id"));
               request.setPractitionerName(set.getString("practitioner_name"));
               request.setUpdatedAt(set.getString("created_at"));
               request.setStockItemId(set.getString("stock_item_id"));
               request.setPaidAt(set.getString("paid_at"));
               request.setMedicationRequestId(set.getString("medication_request_id"));
                request.setPaymentMethod(set.getString("payment_method"));
                request.setStatus(set.getString("status"));
                request.setCashierName(set.getString("cashier_name"));
                request.setCancellationRequestId(set.getString("cancellation_id"));
                request.setCanceledAt(set.getString("canceled_at"));
                request.setCancellationRequestedAt(set.getString("cancelation_requested_at"));
                request.setCancellationRequestedById(set.getString("requested_by_id"));
                request.setCancellationRequestedByName(set.getString("requested_by"));
request.setCreatedByName(set.getString("created_by_name"));
                request.setCancellationApprovedName(set.getString("approved_by"));
                request.setCancellationApprovedById(set.getString("approved_by_id"));
                request.setCancellationApprovedAt(set.getString("approved_at"));
                request.setTransactionId(set.getString("transaction_id"));
                request.setCanceledById(set.getString("canceled_by_id"));
                request.setCanceledByName(set.getString("canceled_by_name"));
                request.setCancellationReason(set.getString("reason"));
                
               
                serviceRequests.add(request);

            }
           chargeItemRepository.saveAll(serviceRequests);
           // migrateChargeItems(serviceRequests);
            logger.info("Saved chargeItem");
        }
        logger.info("Cleaning Requests");
      cleanItems();


    }


    public void getLegacyChargeItem(int batchSize,String current,LocalDate date) {
  
        String sql = "select count(*) from \"ChargeItem\" ci where created_on::date >?::date and created_on::date <= ?";
        long rows = legJdbcTemplate.queryForObject(sql,new Object[]{current,date}, Long.class);
 logger.info("New charge items =>"+rows);
        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<ChargeItem> serviceRequests = new ArrayList<ChargeItem>();

            int startIndex = i * batchSize;
            String sqlQuery = """
         SELECT
"ChargeItem"."id" AS "id",
"ChargeItem"."appointmentid" AS "appointment_id",
ps."uuid" AS "stock_item_id",
"ChargeItem"."category" AS "category",
"ChargeItem"."charge" AS "charge",
"ChargeItem"."clinic_name" AS "location_name",
"ChargeItem"."clinicid" AS "location_id",
"ChargeItem"."created_on" AS "created_at",
"ChargeItem"."transactionid" AS "transaction_id",
"ChargeItem"."currency" AS "currency",
"ChargeItem"."invoiceid" AS "invoice_id",
"ChargeItem"."medicationrequestid" AS "medication_request_id",
"ChargeItem"."patient_mobile" AS "patient_mobile",
"ChargeItem"."patientid" AS "patient_id",
"ChargeItem"."patientname" AS "patient_name",
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
 "Encounter"."id" AS "encounter_id",
  cic.uuid  as cancellation_id,
  cic.requested_date_time as cancelation_requested_at,
   cic.requested_by_uuid as requested_by_id,
    cic.requested_by_name as requested_by,
     cic.canceled_date_time as canceled_at,
     cic.approved_by_name as approved_by,
     cic.approved_by_uuid as approved_by_id,
      cic.approved_date_time as approved_at,
       cic.canceled_by_uuid as canceled_by_id,
       cic.canceled_by_name as canceled_by_name,
       cic.reason as reason,
       cashier_name
FROM
"ChargeItem"  left JOIN "encounter" AS "Encounter" ON "ChargeItem"."id" = "Encounter"."charge_item_id"  
left join charge_item_cancelation cic on cic.charge_item_id="ChargeItem".id 
left join pharmacy_stock ps on stock_item_id =ps.id
LEFT JOIN
    "public"."organization_clientaccount" ca ON "ChargeItem".payer_account_id::uuid = ca.uuid
LEFT JOIN
    "public"."organization" o ON ca.owner_id = o.id
where  "ChargeItem".created_on::date >?::date  and "ChargeItem".created_on::date <= ?
            order by "ChargeItem".id offset ? limit ?
            """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, current,date,startIndex, batchSize);
            while (set.next()) {
                ChargeItem request = new ChargeItem();

               request.setAppointmentId(set.getString("appointment_id"));
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
                request.setProviderId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                request.setProviderName("Nyaho Medical Center");
                request.setPolicyId(set.getString("policy_id"));
                request.setPayerId(set.getString("payer_id"));
                request.setPayerName(set.getString("payer_name"));
                request.setQuantity(set.getInt("quantity"));
                request.setInvoiceId(set.getString("invoice_id"));
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
               request.setUpdatedAt(set.getString("created_at"));
               request.setStockItemId(set.getString("stock_item_id"));
               request.setPaidAt(set.getString("paid_at"));
               request.setPayerContribution(set.getDouble("payer_contribution"));

               request.setMedicationRequestId(set.getString("medication_request_id"));
                request.setPaymentMethod(set.getString("payment_method"));
                request.setStatus(set.getString("status"));
                request.setCashierName(set.getString("cashier_name"));
                request.setCancellationRequestId(set.getString("cancellation_id"));
                request.setCanceledAt(set.getString("canceled_at"));
                request.setCancellationRequestedAt(set.getString("cancelation_requested_at"));
                request.setCancellationRequestedById(set.getString("requested_by_id"));
                request.setCancellationRequestedByName(set.getString("requested_by"));
request.setCreatedByName(set.getString("created_by_name"));
                request.setCancellationApprovedName(set.getString("approved_by"));
                request.setCancellationApprovedById(set.getString("approved_by_id"));
                request.setCancellationApprovedAt(set.getString("approved_at"));
                request.setTransactionId(set.getString("transaction_id"));
                request.setCanceledById(set.getString("canceled_by_id"));
                request.setCanceledByName(set.getString("canceled_by_name"));
                request.setCancellationReason(set.getString("reason"));
                
               
                serviceRequests.add(request);

            }
           chargeItemRepository.saveAll(serviceRequests);
           // migrateChargeItems(serviceRequests);
            logger.info("Saved chargeItem");
        }
        logger.info("Cleaning Requests");
      cleanItems();


    }


    public long dumpFutureCallable(long offset,long limit) {
        System.err.println("starting.............."+offset);
    List<ChargeItem> serviceRequests = new ArrayList<ChargeItem>();
    String sqlQuery = """
          SELECT
 "ChargeItem"."id" AS "id",
 "ChargeItem"."appointmentid" AS "appointment_id",
 ps."uuid" AS "stock_item_id",
 "ChargeItem"."category" AS "category",
 "ChargeItem"."charge" AS "charge",
 "ChargeItem"."clinic_name" AS "location_name",
 "ChargeItem"."clinicid" AS "location_id",
 "ChargeItem"."created_on" AS "created_at",
 "ChargeItem"."transactionid" AS "transaction_id",
 "ChargeItem"."currency" AS "currency",
 "ChargeItem"."invoiceid" AS "invoice_id",
 "ChargeItem"."medicationrequestid" AS "medication_request_id",
 "ChargeItem"."patient_mobile" AS "patient_mobile",
 "ChargeItem"."patientid" AS "patient_id",
 "ChargeItem"."patientname" AS "patient_name",
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
  "Encounter"."id" AS "encounter_id",
   cic.uuid  as cancellation_id,
   cic.requested_date_time as cancelation_requested_at,
    cic.requested_by_uuid as requested_by_id,
     cic.requested_by_name as requested_by,
      cic.canceled_date_time as canceled_at,
      cic.approved_by_name as approved_by,
      cic.approved_by_uuid as approved_by_id,
       cic.approved_date_time as approved_at,
        cic.canceled_by_uuid as canceled_by_id,
        cic.canceled_by_name as canceled_by_name,
        cic.reason as reason,
        cashier_name
 FROM
 "ChargeItem"  left JOIN "encounter" AS "Encounter" ON "ChargeItem"."id" = "Encounter"."charge_item_id"  
 left join charge_item_cancelation cic on cic.charge_item_id="ChargeItem".id 
 left join pharmacy_stock ps on stock_item_id =ps.id
 LEFT JOIN
     "public"."organization_clientaccount" ca ON "ChargeItem".payer_account_id::uuid = ca.uuid
 LEFT JOIN
     "public"."organization" o ON ca.owner_id = o.id
 
             order by "ChargeItem".id offset ? limit ?
             """;
             SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, offset, limit);
             while (set.next()) {
                 ChargeItem request = new ChargeItem();
          
                 request.setAppointmentId(set.getString("appointment_id"));
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
                 request.setPayerContribution(set.getDouble("payer_contribution"));
                 request.setProviderId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                 request.setProviderName("Nyaho Medical Center");
                 request.setPolicyId(set.getString("policy_id"));
                 request.setPayerId(set.getString("payer_id"));
                 request.setPayerName(set.getString("payer_name"));
                 request.setQuantity(set.getInt("quantity"));
                 request.setInvoiceId(set.getString("invoice_id"));
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
                request.setUpdatedAt(set.getString("created_at"));
                request.setStockItemId(set.getString("stock_item_id"));
                request.setPaidAt(set.getString("paid_at"));
                request.setMedicationRequestId(set.getString("medication_request_id"));
                 request.setPaymentMethod(set.getString("payment_method"));
                 request.setStatus(set.getString("status"));
                 request.setCashierName(set.getString("cashier_name"));
                 request.setCancellationRequestId(set.getString("cancellation_id"));
                 request.setCanceledAt(set.getString("canceled_at"));
                 request.setCancellationRequestedAt(set.getString("cancelation_requested_at"));
                 request.setCancellationRequestedById(set.getString("requested_by_id"));
                 request.setCancellationRequestedByName(set.getString("requested_by"));
 request.setCreatedByName(set.getString("created_by_name"));
                 request.setCancellationApprovedName(set.getString("approved_by"));
                 request.setCancellationApprovedById(set.getString("approved_by_id"));
                 request.setCancellationApprovedAt(set.getString("approved_at"));
                 request.setTransactionId(set.getString("transaction_id"));
                 request.setCanceledById(set.getString("canceled_by_id"));
                 request.setCanceledByName(set.getString("canceled_by_name"));
                 request.setCancellationReason(set.getString("reason"));
                 
                
                 serviceRequests.add(request);
             }
            chargeItemRepository.saveAll(serviceRequests);
            cleanItems();
             logger.info("Saved chargeItem");
             return limit;     
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
                request.setTransactionId(set.getString("transactionid"));
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


public void OpdPullThread(int batchSize) {
    String sql = "select count(*) from \"ChargeItem\" ci";
    long rows = legJdbcTemplate.queryForObject(sql, Long.class);
    logger.info("Rows size is: {}", rows);

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    try {
        List<Future<Integer>> futures = executorService.invokeAll(cleanTask2(batchSize, 1000));
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




public List<Callable<Integer>> cleanTask2(int batchSize, long rows) {
    List<Callable<Integer>> callables = new ArrayList<>();
    long batches = (rows + batchSize - 1) / batchSize; // Safe ceiling division
    
    logger.info("Total batches: {}", batches);
    for (int i = 0; i < batches; i++) {
        final int batchNumber = i;

        callables.add(() -> {
            int startIndex = batchNumber * batchSize;
            logger.info("Processing batch {}/{} indices [{}]", batchNumber + 1, batches, startIndex);
           try{
            dumpFutureCallable(startIndex, batchSize);
            
           }catch (Exception e){
            e.printStackTrace();

           }
           return 1;
        });
    }
//migrateClean();
    return callables;
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
            migrateChargeItems(chargeItemRepository.findBhy(startIndex, batchSize));
            
           }catch (Exception e){
            e.printStackTrace();

           }
           return 1;
        });
    }
//migrateClean();
    return callables;
}

public void migrateClean(){
    String sql="""
            update chargeitem 
set patient_gender=p.gender,patient_mobile=p.mobile,patient_mr_number=p.mr_number,patient_name=concat(p.first_name,' ',p.last_name) 
from patients p
where p.uuid=patient_id::"uuid" 
            """;
            serenityJdbcTemplate.update(sql);
}

public void migrateChargeItems(List<ChargeItem> items) {
    String sql = """
        INSERT INTO chargeitem (
            created_at, charge, patient_contribution, payer_contribution, unit_price, 
            pk, "uuid", category, created_by_name, currency, 
            encounter_id, location_id, location_name, medication_request_id, practitioner_id, 
            practitioner_name, product_id, provider_id, provider_name, quantity, 
            revenue_tag_display, relationship, service_id, service_or_product_name, service_request_id,
            visit_id, user_friendly_id, invoice_id, paid_at, patient_id, 
            appointment_id, payer_name, payment_method, status, updated_at, payer_id,
            cancellation_requested_at, cancellation_requested_by_name, cancellation_requested_by_id, 
            cancellation_approved_at, cancellation_approved_name, cancellation_approved_by_id, 
            canceled_at, canceled_by_name, canceled_by_id,cancellation_reason,cancellation_request_uuid,cashier_name,
            patient_birth_date,patient_gender,patient_mobile,patient_mr_number,patient_name,transaction_id
        ) VALUES (
            ?::timestamp, ?, ?, ?, ?, 
            ?, uuid(?), ?, ?, ?, 
            ?, ?, ?, ?, ?, 
            ?, ?, ?, ?, ?, 
            ?, ?, ?, ?, ?, 
            ?, ?, ?, ?::timestamp, ?, 
            ?, ?, ?, ?, now(), ?, 
            ?::timestamp, ?, ?::uuid, ?::timestamp, ?, ?::uuid, 
            ?::timestamp, ?, ?::uuid,?,?::uuid,?,
             ?::date, ?, ?, ?, ?,?
        )
    """;

    serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            ChargeItem item = items.get(i);

            ps.setString(1, item.getCreatedAt());
            ps.setDouble(2, item.getCharge());
            ps.setDouble(3, item.getPatientContribution());
            ps.setDouble(4, item.getPayerContribution());
            ps.setDouble(5, item.getUnitPrice());

            ps.setLong(6, item.getId());
            ps.setString(7, item.getUuid());
            ps.setString(8, item.getCategory());
            ps.setString(9, item.getCreatedByName());
            ps.setString(10, item.getCurrency());

            ps.setString(11, item.getEncounterId());
            ps.setString(12, item.getLocationId() == null ? "2f7d4c40-fe53-491d-877b-c2fee7edc1f2" : item.getLocationId());
            ps.setString(13, item.getLocationName() == null ? "Airport Main" : item.getLocationName());
            ps.setString(14, item.getMedicationRequestId() == null ? "" : item.getMedicationRequestId());
            ps.setString(15, item.getPractitionerId());

            ps.setString(16, item.getPractitionerName());
            ps.setString(17, item.getStockItemId());
            ps.setString(18, item.getProviderId() == null ? "" : item.getProviderId());
            ps.setString(19, item.getProviderName() == null ? "" : item.getProviderName());
            ps.setInt(20, item.getQuantity());

            ps.setString(21, item.getRevenueTagDisplay());
            ps.setString(22, item.getRelationship());
            ps.setString(23, item.getServiceId() == null ? "" : item.getServiceId());
            ps.setString(24, item.getServiceOrProductName() == null ? "" : item.getServiceOrProductName());
            ps.setString(25, item.getServiceRequestId());

            ps.setString(26, item.getVisitId());
            ps.setString(27, item.getUserFriendlyId() == null ? "" : item.getUserFriendlyId());
            ps.setString(28, item.getInvoiceId() == null ? "" : item.getInvoiceId());
            ps.setString(29, item.getPaidAt());
            ps.setString(30, item.getPatientId());

            ps.setString(31, item.getAppointmentId());
            ps.setString(32, item.getPayerName() == null ? "" : item.getPayerName());
            ps.setString(33, item.getPaymentMethod() == null ? "" : item.getPaymentMethod());
            ps.setString(34, item.getStatus() == null ? "" : item.getStatus());
            ps.setString(35, item.getPayerId() == null ? "" : item.getPayerId());

            ps.setString(36, item.getCancellationRequestedAt());
            ps.setString(37, item.getCancellationRequestedByName());
            ps.setString(38, item.getCancellationRequestedById());
            ps.setString(39, item.getCancellationApprovedAt());
            ps.setString(40, item.getCancellationApprovedName());
            ps.setString(41, item.getCancellationApprovedById());
            ps.setString(42, item.getCanceledAt());
            ps.setString(43, item.getCanceledByName());
            ps.setString(44, item.getCanceledById());

            ps.setString(45, item.getCancellationReason());
            ps.setString(46, item.getCancellationRequestId());
            ps.setString(47, item.getCashierName());

            ps.setString(48, item.getPatientBirthDate());
            ps.setString(49, item.getPatientGender());
            ps.setString(50, item.getPatientMobile());
            ps.setString(51, item.getPatientMrNumber());
            ps.setString(52, item.getPatientName());
            ps.setString(53, item.getTransactionId());

        }

        @Override
        public int getBatchSize() {
            return items.size();
        }
    });
}


public void cleanItems(){
String sql = """
    update charge_item set patientmrnumber =p.mrnumber ,patientbirthdate=p.birthdate,patientgender=p.gender,patientmobile=p.mobile,patientname=concat(p.firstname,' ',p.lastname)
from patient_information p
where patientid = p.uuid
        """;

        vectorJdbcTemplate.update(sql);
sql="""
        
WITH ranked AS (
  SELECT id, ROW_NUMBER() OVER (PARTITION BY uuid ORDER BY id) AS rn
  FROM charge_item
)
DELETE FROM charge_item
WHERE id IN (
  SELECT id FROM ranked WHERE rn > 1
);

        """;

        vectorJdbcTemplate.update(sql);
}


public void mig(int batchSize){

    long totalSize = chargeItemRepository.count();
    long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

    for (int i = 0; i < batches; i++) {

        int startIndex = i * batchSize;
        migrateChargeItems(chargeItemRepository.findBhy(startIndex, batchSize));

}}


public void updateChargeItems(String current, String now) {
   List<ChargeItem> requests = chargeItemRepository.findUpdatess(LocalDate.parse(current),LocalDate.parse(now));
        logger.info("ChargeItems to migrate =>"+requests.size());
        migrateChargeItems(requests);
}
}
