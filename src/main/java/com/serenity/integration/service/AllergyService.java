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

import com.serenity.integration.models.AllergyIntolerance;
import com.serenity.integration.models.Doctors;
import com.serenity.integration.models.PatientData;
import com.serenity.integration.models.ServiceRequest;
import com.serenity.integration.repository.AllergyRepository;
import com.serenity.integration.repository.DoctorRepository;
import com.serenity.integration.repository.PatientRepository;
import com.serenity.integration.repository.ServiceRequestRepository;

@Service
public class AllergyService {

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
    AllergyRepository allergyRepository;

    @Autowired
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;

    Logger logger = LoggerFactory.getLogger(getClass());


    public void getLegacyAllergies(int batchSize) {
        Map<String, PatientData> mps = patientRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));

                Map<String, Doctors> doctorMap = doctorRepository.findHisPractitioners().stream()
                .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));
        
        String sql = "Select count(*) from allergy_intolerance ";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<AllergyIntolerance> serviceRequests = new ArrayList<AllergyIntolerance>();

            int startIndex = i * batchSize;
            String sqlQuery = """
                    select * from allergy_intolerance ai join patient p on p.id=ai.patient_id join encounter e on e.id=ai.encounter_id order by ai.id asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                AllergyIntolerance request = new AllergyIntolerance();
              //  request.setId(set.getLong("id"));
                request.setUuid(set.getString("id"));
                request.setRecordedDate(set.getString("created_at"));
                request.setCategory(set.getString("type"));
                request.setAllergyIntoleranceType(set.getString("type"));
                request.setClinicalStatus(set.getString("clinical_status"));
                request.setVerificationStatus(set.getString("verification_status"));
                request.setCode(set.getString("code"));
                request.setServiceProviderId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                String encounterId = set.getString("encounter_id");
                if (encounterId != null) {
                    request.setEncounterId(encounterId);
                }
                String patientMrNumber = set.getString("mr_number");
                if (patientMrNumber != null) {
                                      request.setPatientId(mps.get(patientMrNumber).getMrNumber());            
                }
                
                String practitionerId =set.getString("chief_complaint_author_id")==null?set.getString("history_of_presenting_illness_author_id"):set.getString("chief_complaint_author_id");
               if(practitionerId !=null){
                System.err.println(practitionerId+"----------");
                request.setPractitionerId(practitionerId);
                //request.setPractitionerName(doctorMap.get(practitionerId).getFullName());
               }
                request.setVisitId(set.getString("visit_id"));
                serviceRequests.add(request);

            }
            allergyRepository.saveAll(serviceRequests);
            logger.info("Saved Allergy");
        }
        logger.info("Cleaning Requests");
       

    }
}
