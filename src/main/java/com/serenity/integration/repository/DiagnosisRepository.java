package com.serenity.integration.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.serenity.integration.models.Diagnosis;
import com.serenity.integration.models.Encounter;

public interface DiagnosisRepository extends JpaRepository<Diagnosis,Long>{

       @Query(value = "SELECT * FROM diagnosis where substring(visitid , 1 , 2) != 'LL' order by id OFFSET ?  LIMIT 1000 ",nativeQuery = true)
    List<Diagnosis> getfirst100k(int offset);
    @Query(value = "SELECT * FROM diagnosis  order by id OFFSET ?1  LIMIT ?2 ",nativeQuery = true)
    List<Diagnosis> findBySystemLimit(int offset, int limit );

    @Query(value = "SELECT count(*) FROM diagnosis where patientid is  null",nativeQuery = true)
  long findBySystemLimitCount();

  @Query(value = "SELECT * FROM diagnosis where createdat::date >?1 and createdat::date <=?2 order by id",nativeQuery = true)
    List<Diagnosis> findUpdatess(LocalDate localDate, LocalDate localDate2);


}
