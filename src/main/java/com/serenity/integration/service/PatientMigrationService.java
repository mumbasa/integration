package com.serenity.integration.service;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serenity.integration.models.PatientData;
import com.serenity.integration.repository.PatientRepository;

@Service
public class PatientMigrationService {

    @Autowired
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;

    @Autowired
    @Qualifier("legJdbcTemplate")
    JdbcTemplate legJdbcTemplate;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    PatientService patientService;

        Logger logger = LoggerFactory.getLogger(getClass());

    public void getPatients() {
        String sql = "SELECT external_id from public.patients";
        List<String> set = serenityJdbcTemplate.queryForList(sql, String.class);
        List<PatientData> patientData = patientRepository.findTop5();
        task(patientData);

        System.err.println("patiend count is " + patientData.size() + set.size());

    }

    public void getPatientsThreads() {
       // String truncate ="Truncate patients";
        //serenityJdbcTemplate.update(truncate);

        String sql = "SELECT external_id from public.patients";
        Set<String> set = new HashSet<>(serenityJdbcTemplate.queryForList(sql, String.class));
        List<PatientData> patientData = patientRepository.findySystem().stream().filter(e -> !set.contains(e.getExternalId())).toList();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            List<Future<Integer>> futures = executorService.invokeAll(sumitTask(patientData,  10000));
            for (Future<Integer> future : futures) {
                System.out.println("future.get = " + future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        executorService.shutdown();
        System.err.println("patiend count is " + patientData.size() + set.size());

    }


   public void migratePatientThread(int batchSize) {

        long rows = patientRepository.count();
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
                task2(patientRepository.findySystem(startIndex, batchSize));
                return 1;
            });
        }

        return callables;
    }



    public Set<Callable<Integer>> sumitTask(List<PatientData> data, int size) {
        Set<Callable<Integer>> callables = new HashSet<Callable<Integer>>();
        long totalSize = data.size();
        long batches = (totalSize + size - 1) / size; // Ceiling division


        for (int i = 0; i <batches; i++) {

            final int batchNumber = i; // For use in lambda
                System.err.println("Round submission " + i);
               
                callables.add(new Callable<Integer>() {
                    int startIndex = batchNumber * size;
                    int endIndex =(int) Math.min(startIndex + size, totalSize);
                    List<PatientData> ds = data.subList(startIndex,endIndex);
                    @Override
                    public Integer call() throws Exception {
                        // TODO Auto-generated method stub
                        return task(ds);

                    }

                });
            };
            
return callables;
        }

        
    

    public void sumitTasker(int size) {
        String sql = "SELECT external_id from public.patients";
        List<String> ids = serenityJdbcTemplate.queryForList(sql, String.class);
        System.err.println("ids are " + ids.size());
        List<PatientData> patientData = patientRepository.findTop5();// stream().filter(e ->
                                                                     // !ids.contains(e.getExternalId())).collect(Collectors.toList());
        System.err.println(patientData.size() + "----------------- size");
        System.err.println(patientData.stream().map(PatientData::getExternalId).toList());
        int rounds = patientData.size() / size;

        for (int i = 0; i <= rounds; i++) {
            if (i < rounds) {
                System.err.println("Round submission " + i);
                List<PatientData> ds = patientData.subList(i * size, (i * size) + size);
                try {
                    task2(ds);
                } catch (Exception e) {
                    e.printStackTrace();
                    for (PatientData d : ds) {
                        List<PatientData> f = new ArrayList<>();
                        f.add(d);
                        task2(f);

                    }

                }
            } else {
                System.err.println("Finishing Round submission " + i);
                List<PatientData> ds = patientData.subList((i * size), patientData.size());

                try {
                    task2(ds);
                } catch (Exception e) {

                    for (PatientData d : ds) {
                        List<PatientData> f = new ArrayList<>();
                        f.add(d);
                        task2(f);

                    }

                    e.printStackTrace();
                }

            }
            ;
        }

    }

    public void sumitTask2(int size) {
        String sql = "SELECT external_id from public.patients";
        List<String> ids = serenityJdbcTemplate.queryForList(sql, String.class);
        System.err.println("ids are " + ids.size());
        List<PatientData> patientData = patientRepository.findAll();

        int rounds = patientData.size() / size;

        for (int i = 0; i <= rounds; i++) {
            if (i < rounds) {
                System.err.println("Round submission " + i);
                List<PatientData> ds = patientData.subList(i * size, (i * size) + size);
                try {
                    task(ds);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            } else {
                System.err.println("Finishing Round submission " + i);

                List<PatientData> ds = patientData.subList((i * size), patientData.size());
                task(ds);

            }
            ;
        }

    }

    public int task(List<PatientData> data) {
          ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper
        String sql = """
                
INSERT INTO public.patients (
    created_at, id, "uuid", first_name, last_name, full_name, other_names, 
    mobile, email, birth_date, gender, nationality, mr_number, blood_type, 
    managing_organization_id, managing_organization_name, marital_status, 
    name_prefix, occupation, national_mobile_number, passport_number, 
    external_id, external_system, 
      religious_affliation,  employer,  multiple_birth_boolean, multiple_birth_integer, 
    photo,  previous_payment_method, previous_patient_account_uuid, payment_currency, 
    contribution_value,contribution_type, related_persons, address
) 
VALUES (
    CAST(? AS TIMESTAMP WITH TIME ZONE), nextval('patients_id_seq'::regclass), uuid(?), ?, ?, ?, ?, 
    ?, ?, CAST(? AS DATE),  ?, ?, ?, ?, 
    CAST(? AS UUID), ?, ?, 
    ?, ?, ?, ?, 
    ?,?, 

    -- Values for newly added fields
    CAST(? AS VARCHAR[]), ?, ?,?, 
    ?,  ?, cast(? AS UUID),?,
     ?, ?, ?::jsonb,?::jsonb
);


                """;
                   

        serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @SuppressWarnings("null")
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PatientData k = data.get(i);
                try{
                ps.setString(1, k.getCreatedAt().split("T")[0] + " +0000");
                }catch (Exception e){
                    ps.setString(1,LocalDate.now() + " +0000");

                }
                ps.setString(2, k.getUuid());
                ps.setString(3, k.getFirstName());
                ps.setString(4, k.getLastName());
                ps.setString(5, k.getFullName() == null ? k.getFirstName() + " " + k.getLastName() : k.getOtherNames());
                ps.setString(6, k.getOtherNames() == null ? "" : k.getOtherNames());
                ps.setString(7, k.getMobile());
                ps.setString(8, k.getEmail());
                ps.setString(9, k.getBirthDate());
                ps.setString(10, k.getGender());
                ps.setString(11, k.getNationality());
                ps.setString(12, k.getMrNumber());
                ps.setString(13, k.getBloodType());
                ps.setString(14, k.getManagingOrganizationId());
                ps.setString(15, "Nyaho Medical Center");
                ps.setString(16, k.getMaritalStatus());
                ps.setString(17, k.getTitle());
                ps.setString(18, k.getOccupation());
                ps.setString(19, k.getNationalMobileNumber());
                ps.setString(20, k.getPassportNumber());
                ps.setString(21, k.getExternalId());
                ps.setString(22, k.getExternalSystem());

                if(k.getReligiousAffiliation() !=null){
                String[] reliString = k.getReligiousAffiliation().replaceAll("[{}]","").split(",");
                Array sqlArray = ps.getConnection().createArrayOf("VARCHAR", reliString);

                ps.setArray(23, sqlArray);
                }else{
                    ps.setArray(23, null);
                }
                ps.setString(24, k.getEmployer());
                ps.setBoolean(25, k.isMultipleBirth());
                ps.setInt(26, k.getMultipleBirthInteger());

                ps.setString(27, k.getPhoto());
                ps.setString(28, k.getPaymentMethod());
                ps.setString(29, k.getPreviousPatientAccountUuid());
                ps.setString(30, k.getPaymentCurrency());

                ps.setDouble(31, k.getContributionValue());
                ps.setString(32, k.getContributionType());
                try {
                    String validJson = objectMapper.writeValueAsString(objectMapper.readTree(k.getRelatedPerson()));
                    ps.setString(33,validJson);

                } catch (JsonProcessingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  ps.setString(33, null);
                }


                try {
                    String validJson = objectMapper.writeValueAsString(objectMapper.readTree(k.getAddress()));
                    ps.setString(34, validJson);


                } catch (JsonProcessingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    ps.setString(34, null);
                }

                


            }

            @Override
            public int getBatchSize() {
                // TODO Auto-generated method stub
                return data.size();
            }

        });
        return data.size();
    }

    public int task2(List<PatientData> data) {
        String sql = "INSERT INTO public.patients(created_at, id,  \"uuid\", first_name, last_name, full_name, other_names, mobile, email, birth_date, gender, nationality, mr_number,  blood_type,  managing_organization_id, managing_organization_name,marital_status, name_prefix, occupation,  national_mobile_number, passport_number,  external_id, external_system,is_deleted,updated_at) VALUES (CAST(? AS TIMESTAMP WITH TIME ZONE),nextval('patients_id_seq'::regclass),uuid(?),?,?,?,?,?,?,CAST(? AS DATE),?,?,?,?,CAST(? AS UUID),?,?,?,?,?,?,?,?,?,?::timestamp)";
        serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @SuppressWarnings("null")
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PatientData k = data.get(i);
                try {
                    ps.setString(1, k.getCreatedAt());
                } catch (Exception e) {
                    ps.setString(1, LocalDateTime.now().toString());

                }
                ps.setString(2, k.getUuid());
                ps.setString(3, k.getFirstName());
                ps.setString(4, k.getLastName());
                ps.setString(5, k.getFullName());
                ps.setString(6, k.getOtherNames() == null ? "" : k.getOtherNames());
                ps.setString(7, k.getMobile());
                ps.setString(8, k.getEmail());
                ps.setString(9, k.getBirthDate());
                ps.setString(10, k.getGender());
                ps.setString(11, k.getNationality());
                ps.setString(12, k.getMrNumber());
                ps.setString(13, k.getBloodType());
                ps.setString(14, k.getManagingOrganizationId());
                ps.setString(15, "Nyaho Medical Center");
                ps.setString(16, k.getMaritalStatus());
                ps.setString(17, k.getTitle());
                ps.setString(18, k.getOccupation());
                ps.setString(19, k.getNationalMobileNumber());
                ps.setString(20, k.getPassportNumber());
                ps.setString(21, k.getExternalId());
                ps.setString(22, k.getExternalSystem());
                ps.setBoolean(23, k.isDeleted());
                ps.setString(24, k.getUpdatedAt());

            }

            @Override
            public int getBatchSize() {
                // TODO Auto-generated method stub
                return data.size();
            }

        });
        return data.size();

    }

    public void patientUpdate(String current,String now) {
        List<PatientData> patients = patientRepository.findySystem(LocalDate.parse(current),LocalDate.parse(now));
     task2(patients);
    }
}
