package com.example.result.dto;

import java.util.List;

public record ResultResponse(Long electionId, String title, String status, long totalVotes,
                             List<CandidateResult> results) {}