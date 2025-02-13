package com.serenity.integration.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.serenity.integration.models.Diagnosis;
import com.serenity.integration.models.PatientData;
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
        Map<String, PatientData> mps = patientRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));
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
                request.setUuid(set.getString("uuid"));
                request.setOccurence(set.getString("occurence"));
                request.setCategory(set.getString("category"));
                String encounterId = set.getString("encounter_id");
                if (encounterId != null) {
                    request.setEncounterId(UUID.fromString(encounterId));
                }
                String patientMrNumber = set.getString("mr_number");
                if (patientMrNumber != null) {
                    request.setPatientFullName(mps.get(patientMrNumber).getFullName());
                    request.setPatientMobile(mps.get(patientMrNumber).getMobile());
                    request.setPatientGender(mps.get(patientMrNumber).getGender());
                    request.setPatientId(mps.get(patientMrNumber).getMrNumber());
                    request.setPatientBirthDate((mps.get(patientMrNumber).getBirthDate()));

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
                request.setNote(set.getString("note"));
                request.setGroupIdentifier(set.getString("group_identifier"));
                request.setChargeItemId(set.getString("charge_item_uuid"));
                request.setHealthcareServiceId(set.getString("healthcare_service_id"));
                request.setSampleReceivedDateTime(set.getString("sample_received_date_time"));
                serviceRequests.add(request);

            }
            serviceRequestRepository.saveAll(serviceRequests);
            logger.info("Saved Requests");
        }
        logger.info("Cleaning Requests");
        addVields();

    }

    public void addVields() {

        String sql = """
                                update service_request sr
                set practitionerid =uuid(e.assigned_to_id),visitid=e.visit_id
                from encounter e
                where uuid(e."uuid") =sr.encounterid ;
                                """;
        vectorJdbcTemplate.update(sql);
    }

    public void migrateServiceRequestToSerenity(List<ServiceRequest> requests) {
        String sql = """
                                INSERT INTO public.service_requests
                (created_at,  due_date, sample_received_date_time, charge, occurence,
                 id, body_site, encounter_id, patient_id, practitioner_id,
                  visit_id, service_provider_id, "uuid",  display,
                  category, code, diagnostic_service_section, purpose, priority,
                   healthcare_service_id, healthcare_service_name, charge_item_id, status,
                    status_reason, group_identifier, intent, practitioner_name,patient_mr_number,
                    patient_mobile, patient_birth_date, patient_gender, patient_full_name,
                    encounter_class, notes)
                VALUES(to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'),  0, to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'),
                 nextval('service_requests_id_seq'::regclass), uuid(?), ?, uuid(?), uuid(?),
                 uuid(?),uuid(?), uuid(?), ?,
                 ?, ?, ?, ?, ?,
                 ?, ?, ?, ?,
                 ?, ?, ?, ?,?,
                ?,?,?,?,
                  ?, ?),
                                """;

        serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // TODO Auto-generated method stub
                ServiceRequest request = requests.get(i);
                ps.setString(1, request.getCreatedAt());
                ps.setString(2, request.getDueDate());
                ps.setString(3, request.getSampleReceivedDateTime());
                ps.setString(4, request.getOccurence());
                ps.setString(6, request.getBodySite());
                ps.setString(7, request.getEncounterId().toString());
                ps.setString(8, request.getPatientId());
                ps.setString(9, request.getPractitionerId().toString());
                ps.setString(10, request.getVisitId());
                ps.setString(11, "161380e9-22d3-4627-a97f-0f918ce3e4a9");
                ps.setString(12,UUID.randomUUID().toString());
                ps.setString(13, request.getDisplay());
                ps.setString(14, request.getCategory());
                ps.setString(15, request.getCode());
                ps.setString(16, request.getDiagnosticServiceSection());
                ps.setString(17, request.getPurpose());
                ps.setString(18, request.getPriority());
                ps.setString(19, request.getHealthcareServiceId());
                ps.setString(20, request.getHealthcareServiceName());
                ps.setString(21, request.getChargeItemId());
                ps.setString(22, request.getStatus());
                ps.setString(23, request.getStatusReason());
                ps.setString(24, request.getGroupIdentifier());
                ps.setString(25, request.getPractitionerName());
                ps.setString(26, request.getPatientMrNumber());

                ps.setString(27, request.getPatientMobile());
                ps.setString(28, request.getPatientBirthDate());
                ps.setString(29, request.getPatientGender());
                ps.setString(30, request.getPatientFullName());
                ps.setString(31, "ambulatory");
                ps.setString(32, request.getNote());

            }

            @Override
            public int getBatchSize() {
                // TODO Auto-generated method stub
                return requests.size();
            }

        });

    }

    public void migrate(){
        List<ServiceRequest> requests  =serviceRequestRepository.findAllBy();
        migrateServiceRequestToSerenity(requests);





    }
}
