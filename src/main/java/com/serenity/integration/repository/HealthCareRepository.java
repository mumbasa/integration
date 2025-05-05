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
@Query(value = "SELECT * from healthcareservice offset ?1 limit ?2",nativeQuery = true)
List<HealthCareServices> findByOffset(int page,int size);

@Query(value = "SELECT * from healthcare_service where service_class != 'Diagnostic'",nativeQuery = true)
List<HealthCareServices> findNonDiagnostic();
@Query(value = "SELECT pk from healthcareservice",nativeQuery = true)
List<Long> findAllId();

}
