package com.example.result.controller;

import com.example.result.dto.BallotNotification;
import com.example.result.dto.ResultResponse;
import com.example.result.service.ResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    @Value("${internal.api-key}")
    private String internalKey;

    // any logged-in user (voter or admin) can view results
    @GetMapping("/api/results/{electionId}")
    public ResponseEntity<ResultResponse> getResults(@PathVariable Long electionId) {
        return ResponseEntity.ok(resultService.getResults(electionId));
    }

    // service-to-service only: not routed by the Gateway, and needs the internal key
    @PostMapping("/internal/ballots")
    public ResponseEntity<Void> receiveBallot(
            @RequestHeader(value = "X-Internal-Key", required = false) String key,
            @Valid @RequestBody BallotNotification notification) {

        if (key == null || !MessageDigest.isEqual(
                key.getBytes(StandardCharsets.UTF_8), internalKey.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal key");
        }
        resultService.recordBallot(notification.ballotId(), notification.electionId(), notification.candidateId());
        return ResponseEntity.ok().build();
    }
}