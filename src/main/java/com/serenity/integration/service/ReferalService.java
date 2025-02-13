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
import com.serenity.integration.models.Referal;
import com.serenity.integration.repository.DoctorRepository;
import com.serenity.integration.repository.EncounterNoteRepository;
import com.serenity.integration.repository.PatientRepository;
import com.serenity.integration.repository.ReferralRepository;
import com.serenity.integration.repository.VisitRepository;

@Service
public class ReferalService {
    @Autowired
    @Qualifier(value = "hisJdbcTemplate")
    JdbcTemplate hisJdbcTemplate;

    @Autowired
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;

    @Autowired
    @Qualifier("vectorJdbcTemplate")
    JdbcTemplate vectorJdbcTemplate;

    @Autowired
    EncounterNoteRepository encounterNoteRepository;
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    VisitRepository visitRepository;

    @Autowired
    @Qualifier(value = "legJdbcTemplate")
    JdbcTemplate legJdbcTemplate;

    @Autowired
    ReferralRepository referralRepository;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    DoctorRepository doctorRepository;


     public void getLegacyReferral(int batchSize) {
        Map<String, PatientData> mps = patientRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));

                Map<String, Doctors> doctorMap = doctorRepository.findHisPractitioners().stream()
                .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));
        
        String sql = "SELECT count(*) from service_request";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<Referal> serviceRequests = new ArrayList<Referal>();

            int startIndex = i * batchSize;
            String sqlQuery = """
                    select * from referral_request rr  join patient p on p.id=rr.patient_id join encounter e on e.id=rr.encounter_id order by rr.id asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                Referal request = new Referal();
              //  request.setId(set.getLong("id"));
                request.setUuid(set.getString("id"));
                request.setCreatedAt(set.getString("created_at"));
                request.setDescription(set.getString("description"));
                request.setPriority(set.getString("priority"));
                request.setReason(set.getString("reason"));
                request.setRecipientExtraDetail(set.getString("recipient_extra_detail"));
                request.setReferralType(set.getString("referral_type"));
                request.setStatus(set.getString("status"));
                request.setSpecialty(set.getString("specialty"));
                request.setRecipientId(set.getString("recipient_id"));
                request.setRequesterId(set.getString("requester_id"));
                request.setRequestingOrganizationId(set.getString("requesting_organization_id"));
                request.setReplacesId(set.getString("replaces_id"));
                String encounterId = set.getString("encounter_id");
                if (encounterId != null) {
                    request.setEncounterId(encounterId);
                }
                String patientMrNumber = set.getString("mr_number");
                if (patientMrNumber != null) {
                                      request.setPatientId(mps.get(patientMrNumber).getMrNumber());            
                }
                
            
                              serviceRequests.add(request);

            }
            referralRepository.saveAll(serviceRequests);
            logger.info("Saved Allergy");
        }
        logger.info("Cleaning Requests");
       

    }
    
    
}
