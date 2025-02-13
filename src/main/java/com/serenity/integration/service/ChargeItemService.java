package com.serenity.integration.service;

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
        
        String sql = "select count(*) from \"ChargeItem\" ci ";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<ChargeItem> serviceRequests = new ArrayList<ChargeItem>();

            int startIndex = i * batchSize;
            String sqlQuery = """
                    select * from "ChargeItem" ci order by ci.id asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                ChargeItem request = new ChargeItem();
                request.setId(set.getLong("id"));
                request.setUuid(set.getString("id"));
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
               request.setPayerContribution(set.getDouble("requester_name"));
                
             
              
                serviceRequests.add(request);

            }
            chargeItemRepository.saveAll(serviceRequests);
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
               request.setPayerContribution(set.getDouble("requester_name"));
       
             
              
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
            return getLegacyChargeItemFuture(startIndex, batchSize);
        });
    }

    return callables;
}


public void migrateChargeitems(){

    
}
}
