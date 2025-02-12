package com.serenity.integration.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.serenity.integration.models.Diagnosis;
import com.serenity.integration.models.ServiceRequest;
import com.serenity.integration.repository.DoctorRepository;
import com.serenity.integration.repository.PatientRepository;
import com.serenity.integration.repository.ServiceRequestRepository;

@Service
public class ServiceRequestService {
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
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;

    Logger logger = LoggerFactory.getLogger(getClass());
    public void getLegacyRequest(int batchSize) {
        String sql = "SELECT count(*) from service_request";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<ServiceRequest> serviceRequests = new ArrayList<ServiceRequest>();

            int startIndex = i * batchSize;
            String sqlQuery = """
                    select * from service_request sr join patient p on p.id =sr.patient_id order by sr.id asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                ServiceRequest request = new ServiceRequest();
              //  request.setAccessionNumber(set.getString("accession_number"));
                request.setOccurence(set.getString("occurence"));
                request.setCategory(set.getString("category"));
                String encounterId = set.getString("encounter_id");
                if (encounterId != null) {
                    request.setEncounterId(UUID.fromString(encounterId));
                }
                request.setCreatedAt(set.getString("created_at"));
                request.setDisplay(set.getString("display"));
                request.setDoNotPerform(false);
                request.setIntent(set.getString("intent"));
                request.setCode(set.getString("code"));
                request.setPurpose(set.getString("purpose"));
                request.setStatus(set.getString("status"));
                request.setModifiedAt(set.getString("modified_at"));
                request.setDiagnosticServiceSection(set.getString("diagnostic_service_section"));
                request.setPaid(true);
                request.setGroupIdentifier(set.getString("group_identifier"));
                request.setChargeItemId(set.getString("charge_item_uuid"));
                request.setHealthcareServiceId(set.getString("healthcare_service_id"));
                request.setSampleReceivedDateTime(set.getString("sample_received_date_time"));
                serviceRequests.add(request);

            }
            serviceRequestRepository.saveAll(serviceRequests);
            logger.info("Saved Requests");
        }
    }
}
