package com.example.election.dto;

import java.time.LocalDateTime;

public record CreateElectionRequest(
    String title,
    String description,
    LocalDateTime startTime,
    LocalDateTime endTime
) {}