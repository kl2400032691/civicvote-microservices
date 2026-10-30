package com.example.result.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "processed_ballots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedBallot {

    @Id
    private UUID ballotId;
}