package com.serenity.integration.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.RelatedPerson;
@Repository
public interface RelatedRepo extends JpaRepository<RelatedPerson,Long>{
@Query(value = "SELECT * from relatedperson where createdat::date >?1 and createdat::date <=?2  order by id",nativeQuery =true )
    List<RelatedPerson> findByAll(LocalDate cuDate,LocalDate now);

}
