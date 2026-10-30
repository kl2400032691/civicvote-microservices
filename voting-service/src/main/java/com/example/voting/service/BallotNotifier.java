package com.example.voting.service;

import com.example.voting.client.ResultClient;
import com.example.voting.entity.Ballot;
import com.example.voting.repository.BallotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BallotNotifier {

    private final BallotRepository ballotRepository;
    private final ResultClient resultClient;

    public void notifyBallot(Ballot ballot) {
        if (resultClient.send(ballot)) {
            ballotRepository.markNotified(ballot.getId());
        }
    }

    // safety net: re-sends any ballot Result Service has not confirmed yet
    @Scheduled(initialDelay = 20000, fixedDelay = 30000)
    public void retryPending() {
        List<Ballot> pending = ballotRepository.findByNotifiedFalse();
        if (!pending.isEmpty()) {
            log.info("Retrying {} ballot(s) not yet confirmed by result-service", pending.size());
        }
        for (Ballot ballot : pending) {
            notifyBallot(ballot);
        }
    }
}