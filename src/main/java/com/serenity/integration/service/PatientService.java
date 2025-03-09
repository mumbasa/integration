package com.serenity.integration.service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.serenity.integration.models.Doctors;
import com.serenity.integration.models.EncounterNote;
import com.serenity.integration.models.Observation;
import com.serenity.integration.models.PatientData;
import com.serenity.integration.models.RelatedPerson;
import com.serenity.integration.repository.PatientRepository;

@Service
public class PatientService {

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    @Qualifier(value = "hisJdbcTemplate")
    JdbcTemplate hisJdbcTemplate;

    @Autowired
    @Qualifier(value = "legJdbcTemplate")
    JdbcTemplate legJdbcTemplate;

    @Autowired
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;

    @Autowired
    @Qualifier(value = "vectorJdbcTemplate")
    JdbcTemplate vectorJdbcTemplate;

    Logger LOGGER = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    @Value("${serenity.token}")
    private String serenityToken;

    static final String DIGITS = "0123456789";
    static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final SecureRandom RANDOM = new SecureRandom();

    public void loadPatients() {
      getLegacyPatients();
      getHisPatients();
      removeDuplicates();


    }




    public void getLegacyRelated(int batchSize) {
      
        String sql = "SELECT count(*) from patient_relatedperson";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<RelatedPerson> serviceRequests = new ArrayList<RelatedPerson>();

            int startIndex = i * batchSize;
            String sqlQuery = """
                        select pr.id, pr.created_at, pr.is_deleted, pr.modified_at, pr."uuid" as uuid, 
                        pr.first_name, pr.last_name, pr.is_active, pr.other_names, pr.mobile,pr.email, 
                        relationship, line_address, place_of_work, period_start, period_end, pr.birth_date, 
                        pr.gender, p.uuid as patient_id FROM patient_relatedperson pr join patient 
                        p on p.id=pr.patient_id  order by pr.id asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                RelatedPerson request = new RelatedPerson();
                request.setUuid(set.getString("uuid"));
                request.setCreatedAt(set.getString("created_at"));
                request.setPatientId(set.getString("patient_id"));
                request.setBirthDate(set.getString("birth_date"));
                request.setEmail(set.getString("email"));
                request.setMobile(set.getString("mobile"));
                request.setLastName(set.getString("last_name"));
                request.setFirstName(set.getString("first_name"));
                request.setOtherNames(set.getString("other_names"));
                request.setUpdatedAt(set.getString("modified_at"));
                request.setRelationship(set.getString("relationship"));
                request.setLineAddress(set.getString("line_address"));
                request.setPeriodStart(set.getString("period_start"));
                request.setPeriodEnd(set.getString("period_end"));
                request.setGender(set.getString("gender"));
                request.setId(set.getLong("id"));
                request.setActive(set.getBoolean("is_active"));
             
                serviceRequests.add(request);

            }
          
            LOGGER.info("Saved Related result");
            saveRelatedPersion(serviceRequests);
        }
    }

        public void saveRelatedPersion(List<RelatedPerson> persons){
            String sql ="""
                    INSERT INTO related_persons
(patient_id, id, created_at, updated_at, "uuid", 
mobile, national_mobile_number, first_name, last_name, email, 
birth_date, gender, is_active, other_names, relationship,
 line_address, place_of_work, period_start, period_end)
VALUES
(uuid(?), ?, ?::timestamp, ?::timestamp, uuid(?), 
?, ?, ?, ?, ?, 
?::date, ?, ?, ?, ?,
 ?, ?, ?, ?);
                    """;

serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        // TODO Auto-generated method stub
        RelatedPerson person = persons.get(i);
        ps.setString(1, person.getPatientId());
        ps.setLong(2, person.getId());
        ps.setString(3, person.getCreatedAt());
        ps.setString(4, person.getUpdatedAt());
        ps.setString(5, person.getUuid());

        ps.setString(6, person.getMobile());
        ps.setString(7, person.getNationalMobileNumber());
        ps.setString(8, person.getFirstName());
        ps.setString(9, person.getLastName());
        ps.setString(10, person.getEmail());

        ps.setString(11, person.getBirthDate());
        ps.setString(12, person.getGender());
        ps.setBoolean(13, person.isActive());
        ps.setString(14, person.getOtherNames());
        ps.setString(15, person.getRelationship());

        ps.setString(16, person.getLineAddress());
        ps.setString(17, person.getPlaceOfWork());
        ps.setString(18, person.getPeriodStart());
        ps.setString(19, person.getPeriodEnd());


        
    }

    @Override
    public int getBatchSize() {
        // TODO Auto-generated method stub
       return persons.size();
    }
    
}
);

        }

    
    public void getHisPatients() {
        List<PatientData> fallouts = new ArrayList<>();
        Set<UUID> uuids = new HashSet<>();
        Set<String> mrs = new HashSet<>();
        String sql = "SELECT * FROM patient_master";
        SqlRowSet record = hisJdbcTemplate.queryForRowSet(sql);
        while (record.next()) {
            PatientData pd = new PatientData();
            pd.setExternalId(record.getString("patient_id"));
            pd.setLastName(record.getString("plastname"));
            pd.setFirstName(record.getString("pfirstname"));
            pd.setMobile(record.getString("mobile").isEmpty() ? "" : record.getString("mobile").replaceAll("-", ""));
            pd.setMobile(generateMobile(pd.getMobile().replaceAll("\u0000", "")));
            pd.setEmail(record.getString("email"));
            pd.setBirthDate(record.getString("dob"));
            String str = record.getString("dateenrolled");
            if (str != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
                pd.setCreatedAt(dateTime.toString());
                String mr = generateMRNumber("NMC", dateTime);
                pd.setMrNumber(checkAndGenereate(mrs, mr, "NMC", dateTime));
            }

            pd.setUuid(checkAndGenereateUUID(uuids, UUID.randomUUID()).toString());
            // nationalId(record.getString("countryid");
            pd.setNationalMobileNumber(record.getString("mobile"));
            pd.setGender(record.getString("gender").toUpperCase());
            pd.setExternalSystem("his");
            pd.setNationalMobileNumber(record.getString("phone"));
            pd.setFullName(record.getString("pname"));
            pd.setTitle(record.getString("title"));
            pd.setOccupation(record.getString("occupation"));
            pd.setEmployer(record.getString("employer"));
            pd.setBloodType(record.getString("bloodgroup"));
            pd.setMaritalStatus(record.getString("maritalstatus"));
            pd.setNationality(record.getString("country"));
            pd.setPassportNumber(record.getString("passport_no"));
            pd.setBirthTime(record.getString("timeofbirth"));
            pd.setReligiousAffiliation(record.getString("religiousaffiliation"));
            pd.setManagingOrganizationId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
            pd.setManagingOrganization("Nyaho Medical Centre");

            fallouts.add(pd);
        }
        int cycle = 0;
        int rounds = (fallouts.size() / 1000);
        for (int i = 0; i <= rounds; i++) {
            LOGGER.info("adding round " + i);
            try {

                if (cycle < rounds) {
                    // if(i==310 | i==1205){
                    // for(PatientData dd : fallouts.subList(i * 100, (i * 100) + 100)){
                    // try{
                    // patientRepository.save(dd);
                    // }catch (Exception e){
                    // System.err.println(dd);
                    // }

                    // }

                    //
                    patientRepository.saveAllAndFlush(fallouts.subList(i * 1000, (i * 1000) + 1000));
                } else {
                    patientRepository.saveAllAndFlush(fallouts.subList(cycle * 1000, fallouts.size()));

                }

            } catch (Exception e) {
                System.err.println("error at" + i);
                e.printStackTrace();
            }
            cycle++;
        }

    }

    public static String generateMRNumber(String prefix, LocalDateTime createdAt) {
        // Format the date for a more precise timestamp (e.g., YYMMDD)
        String dateSuffix = createdAt.format(DateTimeFormatter.ofPattern("yy"));

        // Generate a short UUID (for uniqueness)
        String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Format the MR number with prefix, date, random suffix, and unique ID
        return String.format("%s-%s-%s", prefix.toUpperCase(), dateSuffix, uniqueId);

    }

    public static String generateMobile(String number) {
        // Format the date for a more precise timestamp (e.g., YYMMDD)
        try {
            number = number.replaceAll("[^0-9]", "");
            if (number.length() == 10 & number.startsWith("0")) {
                return number = "+233" + number.substring(1, number.length());

            } else if (number.length() == 9) {
                return number = "+233" + number;

            } else {
                return number;
            }
        } catch (Exception e) {
            return "";

        }
    }

    public int getHisNotes() {
        List<PatientData> fallouts = new ArrayList<>();
        Set<String> mrs = new HashSet<>();
        String sql = "SELECT * FROM patient_master";
        SqlRowSet record = hisJdbcTemplate.queryForRowSet(sql);
        while (record.next()) {
            PatientData pd = new PatientData();
            pd.setExternalId(record.getString("patient_id"));
            pd.setLastName(record.getString("plastname"));
            pd.setFirstName(record.getString("pfirstname"));
            pd.setMobile(record.getString("mobile").isEmpty() ? "" : record.getString("mobile").replaceAll("-", ""));
            pd.setMobile(generateMobile(pd.getMobile().replaceAll("\u0000", "")));
            pd.setEmail(record.getString("email"));
            pd.setBirthDate(record.getString("dob"));
            String str = record.getString("dateenrolled");
            if (str != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
                pd.setCreatedAt(dateTime.toString());
                String mr = generateMRNumber("NMC", dateTime);
                pd.setMrNumber(checkAndGenereate(mrs, mr, "NMC", dateTime));
            }

            pd.setUuid(UUID.randomUUID().toString());
            // nationalId(record.getString("countryid");
            pd.setNationalMobileNumber(record.getString("mobile"));
            pd.setGender(record.getString("gender").toUpperCase());
            pd.setExternalSystem("his");
            pd.setNationalMobileNumber(record.getString("phone"));
            pd.setFullName(record.getString("pname"));
            pd.setTitle(record.getString("title"));
            pd.setOccupation(record.getString("occupation"));
            pd.setEmployer(record.getString("employer"));
            pd.setBloodType(record.getString("bloodgroup"));
            pd.setMaritalStatus(record.getString("maritalstatus"));
            pd.setNationality(record.getString("country"));
            pd.setPassportNumber(record.getString("passport_no"));
            pd.setBirthTime(record.getString("timeofbirth"));
            pd.setReligiousAffiliation(record.getString("religiousaffiliation"));
            pd.setManagingOrganizationId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
            fallouts.add(pd);
        }
        // List<PatientData> cleaned = fallouts.stream().filter(e ->
        // !data.contains(e.getExternalId())).toList();
        patientRepository.saveAll(fallouts);
        return 1;
    }

    public static String checkAndGenereate(Set<String> mrNumbers, String mrNumber, String prefix,
            LocalDateTime createdAt) {
        int attempts = 0;
        final int MAX_ATTEMPTS = 10;
        if (!mrNumbers.contains(mrNumber)) {
            mrNumbers.add(mrNumber);
            return mrNumber;
        }
        do {
            mrNumber = generateMRNumber(prefix, createdAt);
            attempts++;

            if (attempts >= MAX_ATTEMPTS) {
                throw new RuntimeException("Failed to generate unique MR number after " + MAX_ATTEMPTS + " attempts");
            }
        } while (mrNumbers.contains(mrNumber));

        mrNumbers.add(mrNumber);

        return mrNumber;
    }

    public static UUID checkAndGenereateUUID(Set<UUID> uuids, UUID uuid) {
        if (uuids.contains(uuid)) {
            uuid = UUID.randomUUID();
            while (uuids.contains(uuid)) {
                uuid = UUID.randomUUID();
                if (!uuids.contains(uuid)) {
                    uuids.add(uuid);
                }

            }

        } else {
            uuids.add(uuid);
        }
        return uuid;
    }

    public static void main(String[] args) {
        Set<String> data = new HashSet<>();
        data.add("NMC-15-6E0DED30");
        System.err.println(checkAndGenereate(data, "NMC-15-6E0DED30", "NMC", LocalDateTime.parse("2015-05-28T00:00")));
        System.err.println(generateMobile(null));

    }

  
    public void getLegacyPatients(int offset) {
        List<PatientData> patientData = new ArrayList<>();
        // Note: Set<String> mrNumbers is declared but never used

        String sql = "SELECT * FROM patient where patient.mr_number  like 'NMC/%' ORDER BY id OFFSET ? LIMIT 1000";
        try {
            SqlRowSet resultSet = legJdbcTemplate.queryForRowSet(sql, offset);
            while (resultSet.next()) {
                try {
                    PatientData data = new PatientData();

                    // Basic data mapping
                    data.setUuid(resultSet.getString("uuid"));
                    data.setExternalId(resultSet.getString("mr_number"));
                    data.setExternalSystem("opd");

                    // Date handling
                    String createdAtStr = resultSet.getString("created_at");
                    if (createdAtStr != null) {
                        String timestampStr = createdAtStr.split("\\.")[0];
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime dateTime = LocalDateTime.parse(timestampStr, formatter);
                        data.setCreatedAt(dateTime.toString());

                        // Generate MR number
                        String mrNumber = generateMRNumber("NMC", dateTime);
                        data.setMrNumber(mrNumber);
                    }

                    // Patient demographics
                    data.setBirthDate(resultSet.getString("birth_date"));
                    data.setFirstName(resultSet.getString("first_name"));
                    data.setLastName(resultSet.getString("last_name"));
                    data.setGender(resultSet.getString("gender"));
                    data.setEmail(resultSet.getString("email"));
                    data.setMobile(resultSet.getString("mobile"));
                    data.setNationalMobileNumber(resultSet.getString("national_mobile_number"));

                    patientData.add(data);
                } catch (Exception e) {
                    LOGGER.error("Error processing patient record: " + e.getMessage());
                    // Continue with next record instead of failing entire batch
                }
            }

            if (!patientData.isEmpty()) {
                patientRepository.saveAll(patientData);
                LOGGER.info("Processed {} patients starting from offset {}", patientData.size(), offset);
            } else {
                LOGGER.warn("No patients found for offset {}", offset);
            }

        } catch (Exception e) {
            LOGGER.error("Error in patient migration: " + e.getMessage(), e);
            throw new RuntimeException("Failed to process patient batch", e);
        }
    }

    public List<PatientData> getLegacyPatients() {
        List<PatientData> patientData = new ArrayList<>();
        // Note: Set<String> mrNumbers is declared but never used

        String sql = "SELECT * FROM patient   ORDER BY id";

        SqlRowSet resultSet = legJdbcTemplate.queryForRowSet(sql);
        while (resultSet.next()) {
            try {
                PatientData data = new PatientData();

                // Basic data mapping
                data.setUuid(resultSet.getString("uuid"));
                data.setId(resultSet.getLong("id"));
                data.setExternalId(resultSet.getString("mr_number"));
                data.setExternalSystem("opd");

                // Date handling
                String createdAtStr = resultSet.getString("created_at");
                if (createdAtStr != null) {
                    String timestampStr = createdAtStr.split("\\.")[0];
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime dateTime = LocalDateTime.parse(timestampStr, formatter);
                    data.setCreatedAt(dateTime.toString());

                    // Generate MR number
                    String mrNumber = generateMRNumber("NMC", dateTime);
                    data.setMrNumber(mrNumber);
                }

                // Patient demographics
                data.setBirthDate(resultSet.getString("birth_date"));
                data.setFirstName(resultSet.getString("first_name"));
                data.setLastName(resultSet.getString("last_name"));
                data.setFullName(resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
                data.setGender(resultSet.getString("gender"));
                data.setEmail(resultSet.getString("email"));
                data.setMobile(resultSet.getString("mobile"));
                data.setManagingOrganizationId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                data.setManagingOrganization("Nyaho Medical Center");
                data.setNationalMobileNumber(resultSet.getString("national_mobile_number"));

                patientData.add(data);
            } catch (Exception e) {
                LOGGER.error("Error processing patient record: " + e.getMessage());
                // Continue with next record instead of failing entire batch
            }
        }
        patientRepository.saveAll(patientData);
        LOGGER.info("finish this job");
        return patientData;
    }

    public void removeDuplicates() {
        String sql = "DELETE FROM patient_information T1\n" + //
                        "USING patient_information T2\n" + //
                        "WHERE T1.ctid > T2.ctid\n" + //
                        "AND T1.externalid = T2.externalid;\n" + //
                        "";
        String sql2 ="update patient_information set birthdate='' where externalsystem ='opd' and birthdate is null";
        sql="update patient_information set mobile='' where externalsystem ='opd' and mobile is null";        
        vectorJdbcTemplate.update(sql);
        vectorJdbcTemplate.update(sql2);
        vectorJdbcTemplate.update(sql);


     
    }

    public void getLegacyPatientsThreads() {
        List<PatientData> data = getLegacyPatients();
        LOGGER.info("Rows " + data.size());
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            List<Future<Integer>> futures = executorService.invokeAll(submitTask2(1000, data));
            for (Future<Integer> future : futures) {
                System.out.println("future.get = " + future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        executorService.shutdown();
        System.err.println("patiend count is " + data.size());

    }

    public Set<Callable<Integer>> submitTask2(int batchSize, List<PatientData> rows) {

        Set<Callable<Integer>> callables = new HashSet<>();
        int totalSize = rows.size();
        int batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            final int batchNumber = i; // For use in lambda

            callables.add(() -> {
                int startIndex = batchNumber * batchSize;
                int endIndex = Math.min(startIndex + batchSize, totalSize);
                LOGGER.debug("Processing batch {}/{}, indices [{}]",
                        batchNumber + 1, batches, startIndex);
                patientRepository.saveAll(rows.subList(startIndex, endIndex));

                return 1;
            });
        }

        return callables;
    }

    public void getHISPatientsThreads() {
        String sql = "SELECT externalid from patient_information";
        List<String> set = vectorJdbcTemplate.queryForList(sql, String.class);
        Set<String> sets = new HashSet<>(set);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            List<Future<Integer>> futures = executorService.invokeAll(submitHisTask(270691, sets, 1000));
            for (Future<Integer> future : futures) {
                System.out.println("future.get =  " + future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        executorService.shutdown();
        System.err.println("patiend count is " + set.size() + set.size());

    }

    public Set<Callable<Integer>> submitHisTask(int batchSize, Set<String> ids, int rows) {

        Set<Callable<Integer>> callables = new HashSet<>();
        int totalSize = rows;
        int batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            final int batchNumber = i; // For use in lambda

            callables.add(() -> {
                int startIndex = batchNumber * batchSize;
                int endIndex = Math.min(startIndex + batchSize, totalSize);
                LOGGER.debug("Processing batch {}/{}, indices [{}]",
                        batchNumber + 1, batches, startIndex);
                // getHisNote(ids, startIndex);

                return 1;
            });
        }

        return callables;
    }
}
