package com.example.result.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tallies",
       uniqueConstraints = @UniqueConstraint(columnNames = {"election_id", "candidate_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tally {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "election_id", nullable = false)
    private Long electionId;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(nullable = false)
    private long votes;
}