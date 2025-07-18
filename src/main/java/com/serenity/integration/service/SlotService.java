package com.serenity.integration.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.serenity.integration.models.AllergyIntolerance;
import com.serenity.integration.models.Slot;
import com.serenity.integration.repository.SlotRepo;

import io.swagger.v3.oas.annotations.servers.Server;
@Service
public class SlotService {

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

    @Autowired
    SlotRepo slotRepo;
    Logger LOGGER = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    public void getLegacySlot(int batchSize,LocalDate date ) {
       // Map<String, PatientData> mps = patientRepository.findAll().stream()
         //       .collect(Collectors.toMap(e -> e.getExternalId(), e -> e));

        String sql = "Select count(*) from slot where ts::date <=?";
        long rows = legJdbcTemplate.queryForObject(sql, new Object[]{date},Long.class);

        long totalSize = rows;
        long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            List<Slot> serviceRequests = new ArrayList<Slot>();

            int startIndex = i * batchSize;
            String sqlQuery = """
SELECT id,"end","start" from slot s
where s.ts::date <=?
            order by ts asc offset ? LIMIT ?
                     """;
            SqlRowSet set = legJdbcTemplate.queryForRowSet(sqlQuery, date,startIndex, batchSize);
            while (set.next()) {
                Slot request = new Slot();
              //  request.setId(set.getLong("id"));
                request.setId(set.getString("id"));
                request.setPlannedStart(set.getString("start"));
                request.setPlannedEnd(set.getString("end"));
                serviceRequests.add(request);

            }
            slotRepo.saveAll(serviceRequests);
            LOGGER.info("Saved Slot");
        }
        LOGGER.info("updating encounter");

updateEncounter();
    }

    public void updateEncounter(){

        String sql ="""
                update encounter set planned_start =slot.plannedstart ,planned_end =slot.plannedend 
from slot 
where slotid =slot.id 
                """;
                vectorJdbcTemplate.execute(sql);
    }
}
