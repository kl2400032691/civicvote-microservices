package com.example.voting.client;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ElectionClient {

    private final LoadBalancerClient loadBalancerClient;
    private final RestClient restClient = RestClient.create();

    // mirrors what Election Service returns
    public record ElectionInfo(Long id, String title, String description, String status) {}
    public record CandidateInfo(Long id, String name, String party) {}

    // asks Eureka + the load balancer where election-service is running
    private String electionBaseUrl() {
        ServiceInstance instance = loadBalancerClient.choose("election-service");
        if (instance == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Election service is not reachable");
        }
        return instance.getUri().toString();
    }

    public void validate(Long electionId, Long candidateId) {
        String base = electionBaseUrl();
        ElectionInfo election;
        List<CandidateInfo> candidates;

        try {
            election = restClient.get()
                    .uri(base + "/api/elections/{id}", electionId)
                    .retrieve()
                    .body(ElectionInfo.class);

            candidates = restClient.get()
                    .uri(base + "/api/elections/{id}/candidates", electionId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CandidateInfo>>() {});
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Election service is not reachable");
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Election not found");
        }

        if (election == null || !"OPEN".equals(election.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Election is not open for voting");
        }

        boolean validCandidate = candidates != null
                && candidates.stream().anyMatch(c -> c.id().equals(candidateId));
        if (!validCandidate) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidate does not belong to this election");
        }
    }
}