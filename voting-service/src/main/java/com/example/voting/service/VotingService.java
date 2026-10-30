package com.example.voting.service;

import com.example.voting.client.ElectionClient;
import com.example.voting.entity.Ballot;
import com.example.voting.entity.VoterRecord;
import com.example.voting.repository.BallotRepository;
import com.example.voting.repository.VoterRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class VotingService {

    private final VoterRecordRepository voterRecordRepository;
    private final BallotRepository ballotRepository;
    private final VoterHasher voterHasher;
    private final ElectionClient electionClient;
    private final TransactionTemplate transactionTemplate;
    private final BallotNotifier ballotNotifier;

    public void castVote(String username, Long electionId, Long candidateId) {

        // 1. Is the election OPEN, and does the candidate belong to it?
        electionClient.validate(electionId, candidateId);

        // 2. Hash the voter id and check for an earlier vote
        String voterHash = voterHasher.hash(username, electionId);

        if (voterRecordRepository.existsByElectionIdAndVoterHash(electionId, voterHash)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already voted in this election");
        }

        // 3. Save "this person voted" and "this candidate got a vote" together
        Ballot ballot = transactionTemplate.execute(status -> {
            try {
                voterRecordRepository.saveAndFlush(new VoterRecord(null, electionId, voterHash));
            } catch (DataIntegrityViolationException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already voted in this election");
            }
            return ballotRepository.save(new Ballot(null, electionId, candidateId, false));
        });

        // 4. Tell Result Service. The vote is already saved, so a failure here must never undo it.
        try {
            ballotNotifier.notifyBallot(ballot);
        } catch (Exception e) {
            log.warn("Result notification failed, the scheduled retry will send it: {}", e.getMessage());
        }
    }

    public boolean hasVoted(String username, Long electionId) {
        return voterRecordRepository.existsByElectionIdAndVoterHash(
                electionId, voterHasher.hash(username, electionId));
    }
}