package ai.bitflow.helppress.publisher.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ai.bitflow.helppress.publisher.constant.ApplicationConstant;
import ai.bitflow.helppress.publisher.domain.ChangeHistory;


@Repository
public interface ChangeHistoryRepository extends JpaRepository <ChangeHistory, Integer> {

	public List<ChangeHistory> findTop300ByOrderByUpdDtDesc();
	public List<ChangeHistory> findTop300ByMethodNotInOrderByUpdDtDesc(List<String> type);
	public Optional<ChangeHistory> findTopByTypeAndMethodAndFilePathAndUpdDtGreaterThanEqual
			(String type, String method, String filePath, LocalDateTime time);
	
	@Query(value =
	        "SELECT "
	        + " MAX(id) as id"
	        + " FROM ChangeHistory WHERE released IS NULL"
	        + " GROUP BY filePath"
	        + " ORDER BY MAX(updDt) DESC")
	public List<Integer> findAllChangedFileIds();
	
	@Query(value =
	        "SELECT "
	        + " MAX(id) as id"
	        + " FROM ChangeHistory WHERE released IS NULL"
	        + " AND type != '" + ApplicationConstant.TYPE_RELEASE + "'"
	        + " GROUP BY filePath"
	        + " ORDER BY MAX(updDt) DESC")
	public List<Integer> findAllChangedFileIdsExcludeRelease();
	
	@Query(value =
	        "SELECT "
	        + " MAX(id) as id"
	        + " FROM ChangeHistory WHERE released IS NULL" 
	        + " AND userid = :userid"
	        + " GROUP BY filePath"
	        + " ORDER BY MAX(updDt) DESC")
	public List<Integer> findAllChangedFileIdsByMe(@Param("userid") String userid);
	
	public List<ChangeHistory> findAllByIdInOrderByUpdDtDesc(List<Integer> ids);
	
	public List<ChangeHistory> findAllByIdInOrderByFilePathAsc(List<Integer> ids);
	
	public List<ChangeHistory> findAllByReleasedOrderByUpdDtAsc(char released);

}
