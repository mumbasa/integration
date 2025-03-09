package com.serenity.integration.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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

import com.serenity.integration.models.Doctors;
import com.serenity.integration.models.Observation;
import com.serenity.integration.models.PatientData;
import com.serenity.integration.repository.DoctorRepository;
import com.serenity.integration.repository.ObservationRepository;
import com.serenity.integration.repository.PatientRepository;

@Service
public class ObservationService {

    @Qualifier(value = "hisJdbcTemplate")
    JdbcTemplate hisJdbcTemplate;

    @Autowired
    @Qualifier(value = "legJdbcTemplate")
    JdbcTemplate legJdbcTemplate;

    @Autowired
    @Qualifier(value = "vectorJdbcTemplate")
    JdbcTemplate vectorJdbcTemplate;

    @Autowired
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    DoctorRepository doctorRepository;

    @Autowired
    ObservationRepository observationRepository;
    Logger logger = LoggerFactory.getLogger(getClass());

    public void getLegacyObservations(int batchSize) {
        Map<String,String> vitalMap = new HashMap<>();
        vitalMap.put("SBP", "8480-6");
        vitalMap.put("BLOOD_PRESSURE", "8462-4");
        vitalMap.put("RESPIRATORY_RATE", "9279-1");
        vitalMap.put("OXYGEN_SATURATION", "20564-1");
        vitalMap.put("DEGREES_CELCIUS", "8310-5");
        vitalMap.put("temperature", "8310-5");
        vitalMap.put("WEIGHT_KG", "3141-9");
        vitalMap.put("PULSE", "8867-4");
        vitalMap.put("BLOOD_SUGAR", "2339-0");
        vitalMap.put("BMI", "39156-5");
        vitalMap.put("HEIGHT_CM", "8302-2");
        vitalMap.put("HEART_RATE", "8867-4");
        vitalMap.put("AVPU", "67775-7");
        

        Map<String,String> unitlMap = new HashMap<>();
        unitlMap.put("SBP", "mm[Hg]");
        unitlMap.put("BLOOD_PRESSURE", "mm[Hg]");
        unitlMap.put("RESPIRATORY_RATE", "beats/min");
        unitlMap.put("OXYGEN_SATURATION", "%");
        unitlMap.put("DEGREES_CELCIUS", "°C");
        unitlMap.put("temperature", "°C");
        unitlMap.put("WEIGHT_KG", "kg");
        unitlMap.put("PULSE", "beats/min");
        unitlMap.put("BLOOD_SUGAR", "mmol/L");
        unitlMap.put("BMI", "kg/m2");
        unitlMap.put("HEIGHT_CM", "cm");
        unitlMap.put("HEART_RATE", "beats/min");
        unitlMap.put("AVPU", "");


        Map<String,String> displayMap = new HashMap<>();
        displayMap.put("SBP", "Systolic blood pressure");
        displayMap.put("BLOOD_PRESSURE", "Diastolic blood pressure");
        displayMap.put("RESPIRATORY_RATE", "Respiratory rate");
        displayMap.put("OXYGEN_SATURATION", "Oxygen saturation in Blood%");
        displayMap.put("DEGREES_CELCIUS", "Body Temperature");
        displayMap.put("temperature", "Body Temperature");
        displayMap.put("WEIGHT_KG", "Body weight Measured");
        displayMap.put("PULSE", "Heart rate");
        displayMap.put("BLOOD_SUGAR", "Blood Sugar");
        displayMap.put("BMI", "Body mass index (BMI) [Ratio]");
        displayMap.put("HEIGHT_CM", "Body height");
        displayMap.put("HEART_RATE", "Heart rate");
        displayMap.put("AVPU", "Level of responsiveness (AVPU)");


      /*   Map<String, PatientData> mps = patientRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));
        Map<String, Doctors> doc = doctorRepository.findOPDPractitioners().stream()
                .collect(Collectors.toMap(e -> e.getExternalId(), e -> e)); */
        String sql = "SELECT count(*) from observation";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<Observation> serviceRequests = new ArrayList<Observation>();

            int startIndex = i * batchSize;
            String sqlQuery = """

            SELECT o.id, o."uuid", o.created_at, o.is_deleted, o.modified_at, o.status, category, code,
             issued, unit, value, data_absent_reason, body_site, "method", specimen, device, effective_date_time,
              diagnostic_report_id, encounter_id, p.uuid as patient_id, e.visit_id, display, interpretation,
               reference_range_high, reference_range_low, "rank", performer_id, performer_name
FROM observation o join patient p on p.id=o.patient_id join encounter e  on e.id =o.encounter_id 
            order by o.id asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                Observation request = new Observation();
                request.setUuid(set.getString(2));
                request.setCreatedAt(set.getString(3));
                request.setPatientId(set.getString("patient_id"));
                request.setEncounterId(set.getString("encounter_id"));
                request.setStatus(set.getString("status")==null?"registered":set.getString("status"));
                request.setEffectiveDateTime(set.getString("effective_date_time"));
                String codes=set.getString("unit");
                
                if(vitalMap.keySet().contains(set.getString("unit"))){
                    request.setCode(vitalMap.get(codes));
                    request.setDisplay(displayMap.get(codes));
                    request.setUnit(unitlMap.get(codes));
                    request.setCategory("vital-signs");
                    request.setEnconterType("vitals-observation");
                }
                else{
                request.setCode(set.getString("unit"));
                request.setDisplay(set.getString("display"));
                request.setUnit(set.getString("unit"));
                request.setCategory(set.getString("category"));
                }
                if(request.getCategory()==null){
                    request.setCategory("vital-signs");
                }
                request.setUpdatedAt (set.getString("created_at"));
                request.setIssued(set.getString("issued"));
                request.setValue(set.getString("value"));
                request.setBodySite(set.getString("body_site"));
                request.setInterpretation(set.getString("interpretation"));
                request.setRank(set.getInt("rank"));
                request.setReferenceRangeHigh(set.getString("reference_range_high"));
                request.setReferenceRangeLow(set.getString("reference_range_low"));
                request.setSpecimen(set.getString("specimen"));
                request.setVisitId(set.getString("visit_id"));
                serviceRequests.add(request);

            }
            observationRepository.saveAll(serviceRequests);
            logger.info("Saved Observation result");
        }
        cleanObservation();
    }

    public void migrateObservation(List<Observation> observations) {
        String sql = """
                        INSERT INTO observations
                (created_at,  issued_at, unit, effective_datetime, id,
                encounter_id, service_provider_id, patient_id,  "uuid", practitioner_id,
                practitioner_name, status, category, tag, code,
                 display,  value, score, "rank", reference_range_high,
                 reference_range_low, reference_range_text, interpretation, body_site, "method",
                 specimen, service_request_id, diagnostic_report_id
                  )
                VALUES( ?::timestamp, ?::timestamp, ?, ?::timestamp, ?,
                uuid(?), uuid(?), uuid(?), uuid(?), uuid(?),
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, uuid(?), uuid(?))
                        """;
        serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // TODO Auto-generated method stub
                Observation observation = observations.get(i);
                ps.setString(1, observation.getCreatedAt());
                ps.setString(2, observation.getIssued());
                ps.setString(3, observation.getUnit());
                ps.setString(4, observation.getEffectiveDateTime());
                ps.setLong(5, observation.getId());

                ps.setString(6, observation.getEncounterId());
                ps.setString(7, "161380e9-22d3-4627-a97f-0f918ce3e4a9");
                ps.setString(8, observation.getPatientId());
                ps.setString(9, observation.getUuid());
                ps.setString(10, observation.getPractitionerId());

                ps.setString(11, observation.getPractitionerName());
                ps.setString(12, observation.getStatus()==""?"registered" :observation.getStatus());
                ps.setString(13, observation.getCategory()==null?"laboratory":observation.getCategory());
                ps.setString(14, observation.getTag());
                ps.setString(15, observation.getCode());

                ps.setString(16, observation.getDisplay());
                ps.setString(17, observation.getValue()==null?"":observation.getValue());
                ps.setString(18, observation.getScore());
                ps.setInt(19, observation.getRank());
                ps.setString(20, observation.getReferenceRangeHigh());

                ps.setString(21, observation.getReferenceRangeLow());
                ps.setString(22, observation.getReferenceRangeText());
                ps.setString(23, observation.getInterpretation());
                ps.setString(24, observation.getBodySite());
                ps.setString(25, observation.getMethod());

                ps.setString(26, observation.getSpecimen());
                ps.setString(27, observation.getServiceRequestId());
                ps.setString(28, observation.getDiagnosticReportId());

                // ps.setString(7, observation.getServiceProviderId());

            }

            @Override
            public int getBatchSize() {
                // TODO Auto-generated method stub
                return observations.size();
            }

        });

    }

    public void cleanObservation() {

        String sql = """
                            update observations
                set practitionerid =e.assigned_to_id ,practitionername=e.assigned_to_name
                from encounter e
                where observations.encounterid =e."uuid" and practitionerid is null ;
                            """;
        vectorJdbcTemplate.update(sql);
    }

    public void migrateObservationThread(int batchSize) {

        long rows = observationRepository.count();
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
                migrateObservation(observationRepository.findByPractitionerIdNotNull(startIndex, batchSize));
                return 1;
            });
        }

        return callables;
    }
}
