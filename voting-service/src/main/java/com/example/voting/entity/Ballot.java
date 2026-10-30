package com.example.voting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "ballots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ballot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "election_id", nullable = false)
    private Long electionId;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    // true once Result Service has confirmed it counted this ballot
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean notified;
}