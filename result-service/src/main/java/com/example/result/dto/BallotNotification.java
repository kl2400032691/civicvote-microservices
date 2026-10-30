package com.example.result.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BallotNotification(@NotNull UUID ballotId, @NotNull Long electionId, @NotNull Long candidateId) {}