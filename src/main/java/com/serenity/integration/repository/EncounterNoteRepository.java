package com.serenity.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.EncounterNote;

@Repository
public interface EncounterNoteRepository extends  JpaRepository<EncounterNote, String>{

    @Query(value = "SELECT * FROM encounternote  where practitionerroletype  !='unknown' and updatedat is not null AND practitionername is not null AND updatedat is not null  OFFSET ?1 LIMIT ?2",nativeQuery = true)
    List<EncounterNote> findOffset(int offset,int limit);
    @Query(value = "SELECT * FROM encounternote  OFFSET ?1 LIMIT ?2",nativeQuery = true)
    List<EncounterNote> findOffsetData(int offset,int limit);

    @Query(value = "select * from encounternote  order by id OFFSET ?1 LIMIT ?2",nativeQuery = true)
    List<EncounterNote> findOffsetLimit(int offset,int limit);

    List<EncounterNote> findByNoteType(String offset);

    @Query(value = "select * from encounternote where notetype='progress-note' and patientid is not null order by id",nativeQuery = true)
    List<EncounterNote> findPrressnote();


    int countByNoteType(String notetype);
    int countByExternalSystem(String notetype); 

}
