package com.serenity.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.ChargeItem;

@Repository
public interface ChargeItemRepository extends JpaRepository<ChargeItem,Long>{

}
