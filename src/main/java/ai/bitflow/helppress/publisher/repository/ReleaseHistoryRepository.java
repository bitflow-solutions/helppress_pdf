package ai.bitflow.helppress.publisher.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ai.bitflow.helppress.publisher.domain.ReleaseHistory;


@Repository
public interface ReleaseHistoryRepository extends JpaRepository <ReleaseHistory, Integer> {

	public List<ReleaseHistory> findTop300ByOrderByUpdDtDesc();
	public ReleaseHistory findOneByOrderByIdDesc();
	
}
