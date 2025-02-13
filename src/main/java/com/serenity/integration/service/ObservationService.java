package com.serenity.integration.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
        String sql = "SELECT count(*) from observations";
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
                request.setStatus(set.getString("status"));
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
            logger.info("Saved Diagnostic result");
        }
    }




}
