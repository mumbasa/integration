package com.serenity.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.ChargeItem;

@Repository
public interface ChargeItemRepository extends JpaRepository<ChargeItem,Long>{

 @Query(value ="SELECT * FROM charge_item order by id OFFSET ? LIMIT ?",nativeQuery = true)
    List<ChargeItem> findBhy(int startIndex, int batchSize);

    @Query(value ="SELECT count(*) FROM charge_item",nativeQuery = true)
    List<ChargeItem> findCount();
}
