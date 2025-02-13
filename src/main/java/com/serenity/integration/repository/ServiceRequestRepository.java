package com.serenity.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.ServiceRequest;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest,Long> {
@Query(value = "select count(*) from service_request sr where practitionerid IS NOT null limit 100",nativeQuery = true)
    List<ServiceRequest> findAllBy();
}
