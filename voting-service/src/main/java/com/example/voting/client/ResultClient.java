package com.example.voting.client;

import com.example.voting.entity.Ballot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResultClient {

    private final LoadBalancerClient loadBalancerClient;
    private final RestClient restClient = RestClient.create();

    @Value("${internal.api-key}")
    private String internalKey;

    // only anonymous data: ballot id, election id, candidate id. Never the voter.
    public record BallotNotification(UUID ballotId, Long electionId, Long candidateId) {}

    /** @return true if Result Service accepted the ballot */
    public boolean send(Ballot ballot) {
        try {
            ServiceInstance instance = loadBalancerClient.choose("result-service");
            if (instance == null) {
                log.warn("result-service is not available, ballot will be retried");
                return false;
            }
            restClient.post()
                    .uri(instance.getUri() + "/internal/ballots")
                    .header("X-Internal-Key", internalKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new BallotNotification(ballot.getId(), ballot.getElectionId(), ballot.getCandidateId()))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("Could not notify result-service, ballot will be retried: {}", e.getMessage());
            return false;
        }
    }
}