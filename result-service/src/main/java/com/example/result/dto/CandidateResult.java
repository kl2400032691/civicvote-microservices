package com.example.result.dto;

public record CandidateResult(Long candidateId, String name, String party, long votes, double percentage) {}