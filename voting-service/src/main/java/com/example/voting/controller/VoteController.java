package com.example.voting.controller;

import com.example.voting.dto.VoteRequest;
import com.example.voting.service.VotingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VotingService votingService;

    @PostMapping
    public ResponseEntity<String> castVote(@Valid @RequestBody VoteRequest request,
                                           Authentication authentication) {
        votingService.castVote(authentication.getName(), request.electionId(), request.candidateId());
        return ResponseEntity.ok("Vote cast successfully");
    }

    @GetMapping("/status/{electionId}")
    public ResponseEntity<Map<String, Boolean>> hasVoted(@PathVariable Long electionId,
                                                         Authentication authentication) {
        boolean voted = votingService.hasVoted(authentication.getName(), electionId);
        return ResponseEntity.ok(Map.of("voted", voted));
    }
}