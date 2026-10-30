package com.example.voting.repository;

import com.example.voting.entity.Ballot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface BallotRepository extends JpaRepository<Ballot, UUID> {

    List<Ballot> findByNotifiedFalse();

    @Modifying
    @Transactional
    @Query("update Ballot b set b.notified = true where b.id = :id")
    void markNotified(@Param("id") UUID id);
}