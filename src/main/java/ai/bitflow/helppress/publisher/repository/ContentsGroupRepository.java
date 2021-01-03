package ai.bitflow.helppress.publisher.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ai.bitflow.helppress.publisher.domain.ContentsGroup;


@Repository
public interface ContentsGroupRepository extends JpaRepository <ContentsGroup, String> {

	List<ContentsGroup> findAllByOrderByOrderNo();
	
}
