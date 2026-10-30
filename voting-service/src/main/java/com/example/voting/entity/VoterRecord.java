package com.example.voting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "voter_records",
       uniqueConstraints = @UniqueConstraint(columnNames = {"election_id", "voter_hash"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoterRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "election_id", nullable = false)
    private Long electionId;

    @Column(name = "voter_hash", nullable = false)
    private String voterHash;
}