package ai.bitflow.helppress.publisher.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ai.bitflow.helppress.publisher.domain.ChangeHistory;
import ai.bitflow.helppress.publisher.repository.ChangeHistoryRepository;

@Component
public class ChangeHistoryDao {

	private final Logger logger = LoggerFactory.getLogger(ChangeHistoryDao.class);

	@Autowired
	private ChangeHistoryRepository chrepo;
	
	/**
	 * 변경이력 저장
	 * @param userid
	 * @param type
	 * @param method
	 * @param title
	 * @param filePath
	 */
    public void addHistory(String userid, String type, String method, String title, String filePath, String realPath, String comment) {
		ChangeHistory item = new ChangeHistory();
		item.setUserid(userid);
		item.setType(type);
		item.setTitle(title);
		item.setComment(comment);
		item.setMethod(method);
		item.setFilePath(filePath);
		item.setRealPath(realPath);
//		item.setReleased(null);
		chrepo.save(item);
	}

	/**
	 * 변경이력 가져오기
	 * @return
	 */
	public List<ChangeHistory> getHistories() {
		List<String> exclude = new ArrayList<>();
//		exclude.add("ADD");
		exclude.add("REN");
		return chrepo.findTop300ByMethodNotInOrderByUpdDtDesc(exclude);
	}
	
	public List<ChangeHistory> findAllChangedByName() {
		List<Integer> list = chrepo.findAllChangedFileIds();
		return chrepo.findAllByIdInOrderByFilePathAsc(list);
	}
	
	
	public List<ChangeHistory> findAllChangedFileIdsExcludeRelease() {
		List<Integer> list = chrepo.findAllChangedFileIdsExcludeRelease();
		return chrepo.findAllByIdInOrderByFilePathAsc(list);
	}
	
	/**
	 * 
	 * @return
	 */
	public void deleteUnused() {
		List<ChangeHistory> list = chrepo.findAllByReleasedOrderByUpdDtAsc('Y');
		List<ChangeHistory> sublist = new ArrayList<ChangeHistory>();
		int size = list.size();
		logger.debug("The row count to delete candidate " + size);
		if (size>100) {
			logger.debug("Deleting " + (size - 100) + " rows");
			for (ChangeHistory item : list) {
				sublist.add(item);
				size--;
				if (size<101) {
					break;
				}
			}
			chrepo.deleteAll(sublist);
		}
	}
	
	public List<ChangeHistory> findAllChangedByMe(String userid) {
		List<Integer> list = chrepo.findAllChangedFileIdsByMe(userid);
		return chrepo.findAllByIdInOrderByUpdDtDesc(list);
	}
	
	public void releaseAll() {
		List<ChangeHistory> list = chrepo.findAll();
		for (ChangeHistory item : list) {
			item.setReleased('Y');
		}
		chrepo.saveAll(list);
	}
	
	public void releasePart(List<ChangeHistory> list) {
		for (ChangeHistory item : list) {
			item.setReleased('Y');
		}
		chrepo.saveAll(list);
	}

	public Optional<ChangeHistory> findById(Integer id) {
		return chrepo.findById(id);
	}

}
