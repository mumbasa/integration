package com.serenity.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.ServicePrice;

@Repository
public interface ServicePriceRepo extends JpaRepository<ServicePrice,Long> {
    
    public List<ServicePrice> findByHealthcareServiceId(String id);

@Query(value = "SELECT * from service_prices  where customer_group_id is not  null and healthcare_service_id !='0'",nativeQuery = true)
    public List<ServicePrice> findByHealthcareServiceId();

    
@Query(value = "SELECT * from service_prices  where customer_group_id is not  null and healthcare_service_id !='0' OFFSET ?1 LIMIT ?2",nativeQuery = true)
public List<ServicePrice> findServicePrices(int offset,int limit);

}
