package com.example.election.controller;

import com.example.election.dto.*;
import com.example.election.entity.Candidate;
import com.example.election.entity.Election;
import com.example.election.enums.ElectionStatus;
import com.example.election.repository.CandidateRepository;
import com.example.election.repository.ElectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/elections")
@RequiredArgsConstructor
public class ElectionController {

    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;

    // Create a new election
    @PostMapping
    public ResponseEntity<ElectionResponse> createElection(@RequestBody CreateElectionRequest request) {
        Election election = new Election();
        election.setTitle(request.title());
        election.setDescription(request.description());
        election.setStartTime(request.startTime());
        election.setEndTime(request.endTime());
        election.setStatus(ElectionStatus.UPCOMING);

        Election saved = electionRepository.save(election);
        return ResponseEntity.ok(ElectionResponse.fromEntity(saved));
    }

    // Add a candidate to an election
    @PostMapping("/{electionId}/candidates")
    public ResponseEntity<String> addCandidate(@PathVariable Long electionId,
                                                @RequestBody CandidateRequest request) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new RuntimeException("Election not found"));

        Candidate candidate = new Candidate();
        candidate.setName(request.name());
        candidate.setParty(request.party());
        candidate.setElection(election);

        candidateRepository.save(candidate);
        return ResponseEntity.ok("Candidate added successfully");
    }

    // List all elections
    @GetMapping
    public ResponseEntity<List<ElectionResponse>> getAllElections() {
        List<ElectionResponse> elections = electionRepository.findAll()
                .stream()
                .map(ElectionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(elections);
    }

    // Get a single election by ID
    @GetMapping("/{id}")
    public ResponseEntity<ElectionResponse> getElection(@PathVariable Long id) {
        Election election = electionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Election not found"));
        return ResponseEntity.ok(ElectionResponse.fromEntity(election));
    }

    // Open voting for an election
    @PutMapping("/{id}/open")
    public ResponseEntity<String> openElection(@PathVariable Long id) {
        Election election = electionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Election not found"));
        election.setStatus(ElectionStatus.OPEN);
        electionRepository.save(election);
        return ResponseEntity.ok("Election is now OPEN");
    }

    // Close voting for an election
    @PutMapping("/{id}/close")
    public ResponseEntity<String> closeElection(@PathVariable Long id) {
        Election election = electionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Election not found"));
        election.setStatus(ElectionStatus.CLOSED);
        electionRepository.save(election);
        return ResponseEntity.ok("Election is now CLOSED");
    }

    // Get candidates for an election
    @GetMapping("/{electionId}/candidates")
    public ResponseEntity<List<Candidate>> getCandidates(@PathVariable Long electionId) {
        return ResponseEntity.ok(candidateRepository.findByElectionId(electionId));
    }
}