package com.example.result.repository;

import com.example.result.entity.ProcessedBallot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedBallotRepository extends JpaRepository<ProcessedBallot, UUID> {
}