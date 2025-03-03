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

import com.serenity.integration.models.DiagnosticReport;
import com.serenity.integration.models.Doctors;
import com.serenity.integration.models.Observation;
import com.serenity.integration.models.PatientData;
import com.serenity.integration.repository.DoctorRepository;
import com.serenity.integration.repository.ObservationRepository;
import com.serenity.integration.repository.PatientRepository;

import io.micrometer.observation.ObservationRegistry;

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
        Map<String, PatientData> mps = patientRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));
        Map<String, Doctors> doc = doctorRepository.findOPDPractitioners().stream()
                .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));
        String sql = "SELECT count(*) from observation";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<Observation> serviceRequests = new ArrayList<Observation>();

            int startIndex = i * batchSize;
            String sqlQuery = """
                    select * from observation dr join patient p on p.id = dr.patient_id order by dr.id asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                Observation request = new Observation();
                request.setUuid(set.getString(2));
                request.setCreatedAt(set.getString(3));
                request.setPatientId(mps.get(set.getString("mr_number")).getUuid());
                request.setEncounterId(set.getString("encounter_id"));
                request.setStatus(set.getString("status")==null?"registered":set.getString("status"));
                request.setEffectiveDateTime(set.getString("effective_date_time"));
                request.setCode(set.getString("code"));
                request.setUnit(set.getString("unit"));
                request.setCategory(set.getString("category"));
                request.setCreatedAt(set.getString(3));
                request.setIssued(set.getString("issued"));
                request.setBodySite(set.getString("body_site"));
                request.setInterpretation(set.getString("interpretation"));
                request.setRank(set.getInt("rank"));
                request.setReferenceRangeHigh(set.getString("reference_range_high"));
                request.setReferenceRangeLow(set.getString("reference_range_low"));
                request.setSpecimen(set.getString("specimen"));
                request.setDisplay(set.getString("display"));
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
