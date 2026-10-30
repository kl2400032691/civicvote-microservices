package com.example.election.controller;

import com.example.election.dto.*;
import com.example.election.entity.Candidate;
import com.example.election.entity.Election;
import com.example.election.enums.ElectionStatus;
import com.example.election.repository.CandidateRepository;
import com.example.election.repository.ElectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/elections")
@RequiredArgsConstructor
public class ElectionController {

    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;

    private Election findElection(Long id) {
        return electionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Election not found"));
    }

    // Create a new election (ADMIN)
    @PostMapping
    public ResponseEntity<ElectionResponse> createElection(@RequestBody CreateElectionRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        if (request.startTime() == null || request.endTime() == null
                || !request.endTime().isAfter(request.startTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        Election election = new Election();
        election.setTitle(request.title());
        election.setDescription(request.description());
        election.setStartTime(request.startTime());
        election.setEndTime(request.endTime());
        election.setStatus(ElectionStatus.UPCOMING);

        Election saved = electionRepository.save(election);
        return ResponseEntity.ok(ElectionResponse.fromEntity(saved));
    }

    // Add a candidate to an election (ADMIN)
    @PostMapping("/{electionId}/candidates")
    public ResponseEntity<String> addCandidate(@PathVariable Long electionId,
                                               @RequestBody CandidateRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidate name is required");
        }
        Election election = findElection(electionId);

        Candidate candidate = new Candidate();
        candidate.setName(request.name());
        candidate.setParty(request.party());
        candidate.setElection(election);

        candidateRepository.save(candidate);
        return ResponseEntity.ok("Candidate added successfully");
    }

    // List all elections (public)
    @GetMapping
    public ResponseEntity<List<ElectionResponse>> getAllElections() {
        List<ElectionResponse> elections = electionRepository.findAll()
                .stream()
                .map(ElectionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(elections);
    }

    // Get a single election by ID (public)
    @GetMapping("/{id}")
    public ResponseEntity<ElectionResponse> getElection(@PathVariable Long id) {
        return ResponseEntity.ok(ElectionResponse.fromEntity(findElection(id)));
    }

    // Open voting (ADMIN)
    @PutMapping("/{id}/open")
    public ResponseEntity<String> openElection(@PathVariable Long id) {
        Election election = findElection(id);
        election.setStatus(ElectionStatus.OPEN);
        electionRepository.save(election);
        return ResponseEntity.ok("Election is now OPEN");
    }

    // Close voting (ADMIN)
    @PutMapping("/{id}/close")
    public ResponseEntity<String> closeElection(@PathVariable Long id) {
        Election election = findElection(id);
        election.setStatus(ElectionStatus.CLOSED);
        electionRepository.save(election);
        return ResponseEntity.ok("Election is now CLOSED");
    }

    // Candidates of an election (public)
    @GetMapping("/{electionId}/candidates")
    public ResponseEntity<List<CandidateResponse>> getCandidates(@PathVariable Long electionId) {
        List<CandidateResponse> candidates = candidateRepository.findByElectionId(electionId)
                .stream()
                .map(c -> new CandidateResponse(c.getId(), c.getName(), c.getParty()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(candidates);
    }
}