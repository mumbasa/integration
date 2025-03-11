package com.serenity.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.ServicePrice;

@Repository
public interface ServicePriceRepo extends JpaRepository<ServicePrice,Long> {

}
