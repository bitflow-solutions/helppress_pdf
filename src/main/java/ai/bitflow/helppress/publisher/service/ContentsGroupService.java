package ai.bitflow.helppress.publisher.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ai.bitflow.helppress.publisher.constant.ApplicationConstant;
import ai.bitflow.helppress.publisher.dao.ChangeHistoryDao;
import ai.bitflow.helppress.publisher.dao.FileDao;
import ai.bitflow.helppress.publisher.domain.ContentsGroup;
import ai.bitflow.helppress.publisher.repository.ContentsGroupRepository;
import ai.bitflow.helppress.publisher.vo.req.ContentsGroupReq;

/**
 * 도움말그룹 관련 서비스
 * @author 김성준
 */
@Service
public class ContentsGroupService {

	private final Logger logger = LoggerFactory.getLogger(ContentsGroupService.class);
	
	@Autowired
	private ContentsGroupRepository grepo;
	
	@Autowired
	private ChangeHistoryDao chdao;
	
	@Autowired
	private FileDao fdao;
	
	
	/**
	 * 전체 카테고리 조회
	 * @return
	 */
	@Cacheable(value="groups")
	public List<ContentsGroup> getGroups() {
		return grepo.findAllByOrderByOrderNo();
    }
	
	/**
	 * 도움말그룹 조회
	 * @param id
	 * @return
	 */
	public ContentsGroup getGroup(String id) {
		Optional<ContentsGroup> row = grepo.findById(id);
		if (row.isPresent()) {
			return row.get();
		} else {
			return null;
		}
    }
	
	/**
	 * 도움말그룹 생성
	 * @param params
	 * @return
	 */
	@CacheEvict(value="groups", allEntries=true)
	@Transactional
    public String newGroup(ContentsGroupReq params, String userid) {
		
		String method = ApplicationConstant.METHOD_ADD;
		
		ContentsGroup item = new ContentsGroup();
		item.setGroupId(params.getGroupId());
		item.setName(params.getName());
		item.setOrderNo(params.getOrderNo());
		String ret = grepo.save(item).getGroupId();
		
		List<ContentsGroup> list = grepo.findAll();
		fdao.makeAllContentGroupHTML(list, method, userid);
		
		return ret;
	}
	
	/**
	 * 도움말그룹 수정
	 * @param params
	 * @return
	 */
	@CacheEvict(value="groups", allEntries=true)
	@Transactional
    public ContentsGroup updateGroup(ContentsGroupReq params, String userid) {
		
		String method = ApplicationConstant.METHOD_MODIFY;
		
		Optional<ContentsGroup> row = grepo.findById(params.getGroupId());
		ContentsGroup item1 = null;
		if (!row.isPresent()) {
			return null;
		} else {
			item1 = row.get();
			if (params.getTree()!=null) {
				item1.setTree(params.getTree());
			}
			if (params.getName()!=null) {
				item1.setName(params.getName());
			}
			if (params.getOrderNo()!=null) {
				item1.setOrderNo(params.getOrderNo());
			}
		}
		ContentsGroup ret = grepo.save(item1);
		
		List<ContentsGroup> list = grepo.findAll();
		fdao.makeAllContentGroupHTML(list, method, userid);
		
		return ret;
    }
	
	/**
	 * 도움말그룹 삭제
	 * Todo: 그룹 파일도 삭제
	 * @param id
	 */
	@CacheEvict(value="groups", allEntries=true)
	@Transactional
    public void deleteGroup(String groupid, String userid) {
		
		String type   = ApplicationConstant.TYPE_GROUP;
		String method = ApplicationConstant.METHOD_DELETE;
		Optional<ContentsGroup> row = grepo.findById(groupid);
		
		if (row.isPresent()) {
			ContentsGroup item = row.get();
			grepo.deleteById(groupid);
			List<ContentsGroup> list = grepo.findAll();
			fdao.makeAllContentGroupHTML(list, method, userid);
		}
		
    }
	
}
