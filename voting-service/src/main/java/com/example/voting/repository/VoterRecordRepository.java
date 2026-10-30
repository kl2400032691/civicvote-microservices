package com.example.voting.repository;

import com.example.voting.entity.VoterRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VoterRecordRepository extends JpaRepository<VoterRecord, UUID> {
    boolean existsByElectionIdAndVoterHash(Long electionId, String voterHash);
}