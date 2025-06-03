package com.serenity.integration.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.ServiceRequest;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest,Long> {
@Query(value = "select  * from service_request sr  order by id asc offset ?1 limit ?2",nativeQuery = true)
    List<ServiceRequest> findOffset(int offset,int limit);

    @Query(value = "select  count(*) from service_request WHERE encounterid  is null",nativeQuery = true)
    long getParactionerIdCount();

    @Query(value = "select  * from service_request sr where createdat::date >?1 and createdat::date <=?2  order by id asc",nativeQuery = true)
    List<ServiceRequest> findUpdatess(LocalDate localDate, LocalDate localDate2);
}
