package com.example.result.repository;

import com.example.result.entity.Tally;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TallyRepository extends JpaRepository<Tally, Long> {

    List<Tally> findByElectionId(Long electionId);

    // one atomic statement: inserts the first vote or adds 1, so simultaneous votes can't overwrite each other
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO tallies (election_id, candidate_id, votes)
            VALUES (:electionId, :candidateId, 1)
            ON CONFLICT (election_id, candidate_id)
            DO UPDATE SET votes = tallies.votes + 1
            """, nativeQuery = true)
    void increment(@Param("electionId") Long electionId, @Param("candidateId") Long candidateId);
}