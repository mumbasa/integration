package com.serenity.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.ServiceRequest;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest,Long> {
@Query(value = "select  * from service_request sr  WHERE encounterid is not null  order by id asc offset ?1 limit ?2",nativeQuery = true)
    List<ServiceRequest> findOffset(int offset,int limit);
    @Query(value = "select  count(*) from service_request WHERE encounterid is not null",nativeQuery = true)
    long getParactionerIdCount();
}
