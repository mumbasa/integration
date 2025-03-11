package com.serenity.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.HealthCareServices;

@Repository
public interface HealthCareRepository extends JpaRepository<HealthCareServices,Long> {
HealthCareServices findByServiceName(String servicename);
HealthCareServices findByPk(long pk);
@Query(value = "SELECT pk from healthcareservice",nativeQuery = true)
List<Long> findAllId();

}
