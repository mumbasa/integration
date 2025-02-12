package com.serenity.integration.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.serenity.integration.models.DiagnosticReport;
import com.serenity.integration.models.Doctors;
import com.serenity.integration.models.PatientData;
import com.serenity.integration.models.ServiceRequest;
import com.serenity.integration.repository.DiagnosticReportRepository;
import com.serenity.integration.repository.DoctorRepository;
import com.serenity.integration.repository.PatientRepository;

public class DiagnosticReportService {
@Autowired
DiagnosticReportRepository diagnosticReportRepository;
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
    Logger logger = LoggerFactory.getLogger(getClass());

    public void getLegacyRequest(int batchSize) {
           Map<String, PatientData> mps = patientRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));
        Map<String, Doctors> doc = doctorRepository.findHisPractitioners().stream()
                .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));
        String sql = "SELECT count(*) from diagnostic_report";
        long rows = legJdbcTemplate.queryForObject(sql, Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<DiagnosticReport> serviceRequests = new ArrayList<DiagnosticReport>();

            int startIndex = i * batchSize;
            String sqlQuery = """
                    select * from diagnostic_report dr join patient p on p.id = dr.patient_id  join service_request sr on dr.based_on_id =sr.id order by dr.id asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, startIndex, batchSize);
            while (set.next()) {
                DiagnosticReport request = new DiagnosticReport();
                request.setUuid(set.getString("dr.uuid"));
                request.setBasedOnId(set.getString("sr.uuid"));
                request.setIssuedDate(set.getString("dr.created_at"));
                request.setPatientId(mps.get(set.getString("mr_number")).getUuid());
                request.setServiceProviderId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                if (set.getString("requesting_practitioner_role_id") != null) {
                    request.setPerformerId(set.getString("requesting_practitioner_role_id"));
                    request.setPerformerName(doc.get(set.getString("requesting_practitioner_role_id")).getFullName());
                }
                
                request.setCreatedAt(set.getString("dr.created_at"));
            
                serviceRequests.add(request);

            }
            diagnosticReportRepository.saveAll(serviceRequests);
            logger.info("Saved Diagnostic result");
        }
    }
}



