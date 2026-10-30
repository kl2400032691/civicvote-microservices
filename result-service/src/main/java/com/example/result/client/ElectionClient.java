package com.example.result.client;

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

    public record ElectionInfo(Long id, String title, String description, String status) {}
    public record CandidateInfo(Long id, String name, String party) {}

    private String baseUrl() {
        ServiceInstance instance = loadBalancerClient.choose("election-service");
        if (instance == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Election service is not reachable");
        }
        return instance.getUri().toString();
    }

    public ElectionInfo getElection(Long electionId) {
        try {
            return restClient.get()
                    .uri(baseUrl() + "/api/elections/{id}", electionId)
                    .retrieve()
                    .body(ElectionInfo.class);
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Election service is not reachable");
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Election not found");
        }
    }

    public List<CandidateInfo> getCandidates(Long electionId) {
        try {
            return restClient.get()
                    .uri(baseUrl() + "/api/elections/{id}/candidates", electionId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CandidateInfo>>() {});
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Election service is not reachable");
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Election not found");
        }
    }
}