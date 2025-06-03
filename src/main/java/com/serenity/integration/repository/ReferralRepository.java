package com.serenity.integration.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.Referal;
@Repository
public interface ReferralRepository extends JpaRepository<Referal,Long> {

    @Query(value ="SELECT * FROM referrals order by id OFFSET ? LIMIT ?",nativeQuery = true)
    List<Referal> findBhy(int startIndex, int batchSize);

    @Query(value ="SELECT * FROM referrals where created_at::date >?1 and created_at::date <=?2 order by id",nativeQuery = true)
    List<Referal> findUpdatess(LocalDate current,LocalDate now);

}
