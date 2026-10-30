package com.example.result.service;

import com.example.result.client.ElectionClient;
import com.example.result.dto.CandidateResult;
import com.example.result.dto.ResultResponse;
import com.example.result.entity.ProcessedBallot;
import com.example.result.entity.Tally;
import com.example.result.repository.ProcessedBallotRepository;
import com.example.result.repository.TallyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final TallyRepository tallyRepository;
    private final ProcessedBallotRepository processedBallotRepository;
    private final ElectionClient electionClient;

    // Called by Voting Service for every ballot
    @Transactional
    public void recordBallot(UUID ballotId, Long electionId, Long candidateId) {
        if (processedBallotRepository.existsById(ballotId)) {
            return;   // already counted (a retry), so ignore it
        }
        processedBallotRepository.save(new ProcessedBallot(ballotId));
        tallyRepository.increment(electionId, candidateId);
    }

    public ResultResponse getResults(Long electionId) {
        ElectionClient.ElectionInfo election = electionClient.getElection(electionId);
        List<ElectionClient.CandidateInfo> candidates = electionClient.getCandidates(electionId);

        Map<Long, Long> counts = tallyRepository.findByElectionId(electionId).stream()
                .collect(Collectors.toMap(Tally::getCandidateId, Tally::getVotes));

        long total = candidates.stream()
                .mapToLong(c -> counts.getOrDefault(c.id(), 0L))
                .sum();

        List<CandidateResult> results = candidates.stream()
                .map(c -> {
                    long votes = counts.getOrDefault(c.id(), 0L);
                    double pct = total == 0 ? 0.0 : Math.round(votes * 10000.0 / total) / 100.0;
                    return new CandidateResult(c.id(), c.name(), c.party(), votes, pct);
                })
                .sorted(Comparator.comparingLong(CandidateResult::votes).reversed())
                .toList();

        return new ResultResponse(electionId, election.title(), election.status(), total, results);
    }
}