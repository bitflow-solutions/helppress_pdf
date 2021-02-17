package ai.bitflow.helppress.publisher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ai.bitflow.helppress.publisher.domain.Sequences;


@Repository
public interface SequencesRepository extends JpaRepository <Sequences, Integer> {

}
