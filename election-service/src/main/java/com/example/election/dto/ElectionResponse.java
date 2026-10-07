package com.example.election.dto;

import com.example.election.entity.Election;

public record ElectionResponse(
    Long id,
    String title,
    String description,
    String status
) {
    public static ElectionResponse fromEntity(Election election) {
        return new ElectionResponse(
            election.getId(),
            election.getTitle(),
            election.getDescription(),
            election.getStatus().name()
        );
    }
}