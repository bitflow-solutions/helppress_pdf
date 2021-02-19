package ai.bitflow.helppress.publisher.service;

import java.io.File;
import java.util.Calendar;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ai.bitflow.helppress.publisher.constant.ApplicationConstant;
import ai.bitflow.helppress.publisher.dao.ChangeHistoryDao;
import ai.bitflow.helppress.publisher.dao.FileDao;
import ai.bitflow.helppress.publisher.dao.NodeDao;
import ai.bitflow.helppress.publisher.domain.ChangeHistory;
import ai.bitflow.helppress.publisher.domain.Contents;
import ai.bitflow.helppress.publisher.domain.ContentsGroup;
import ai.bitflow.helppress.publisher.domain.Sequences;
import ai.bitflow.helppress.publisher.domain.idclass.PkContents;
import ai.bitflow.helppress.publisher.repository.ContentsGroupRepository;
import ai.bitflow.helppress.publisher.repository.ContentsRepository;
import ai.bitflow.helppress.publisher.repository.SequencesRepository;
import ai.bitflow.helppress.publisher.vo.req.DeleteNodeReq;
import ai.bitflow.helppress.publisher.vo.req.NewNodeReq;
import ai.bitflow.helppress.publisher.vo.req.UpdateNodeReq;
import ai.bitflow.helppress.publisher.vo.res.result.NodeUpdateResult;


@Service
public class NodeService {

	private final Logger logger = LoggerFactory.getLogger(NodeService.class);
	
	@Autowired
	private ChangeHistoryDao chdao;
	
	@Autowired
	private ContentsRepository contentsrepo;
	
	@Autowired
	private SequencesRepository seqrepo;
	
	@Autowired
	private NodeDao ndao;
	
	@Autowired
	private FileDao fdao;
	
	@Autowired
	private ContentsGroupRepository grepo;
	
	
	public NodeUpdateResult getNodes(String groupId) {
		NodeUpdateResult ret = new NodeUpdateResult();
		Optional<ContentsGroup> row = grepo.findById(groupId);
		if (row.isPresent()) {
			ContentsGroup item = row.get();
			ret.setGroupId(item.getGroupId());
		}
		return ret;
	}
	
	/**
	 * 새 "빈" 컨텐츠 추가
	 * e.g.) String textOnly = Jsoup.parse(params.getContent()).text();
	 * @param item
	 */
	@Transactional
	public NodeUpdateResult newNode(NewNodeReq params, String userid) {
		
		String method = ApplicationConstant.METHOD_ADD;
		String title = "";
		
		NodeUpdateResult ret = new NodeUpdateResult();
		ret.setMethod(method);
		
		Contents item1 = new Contents();
		if (params.getFolder()==null || params.getFolder()==false) {
			title = "새 도움말";
			item1.setAuthor(userid);		
		} else {
			title = "새 폴더";
		}
		item1.setTitle(title);
		item1.setGroupId(params.getGroupId());
		item1.setId("!" + RandomStringUtils.randomAlphanumeric(4));
		
		Sequences seqs = new Sequences();
		seqrepo.save(seqs);

//		if (params.getFolder()==null || params.getFolder()==false) {
//			// 폴더의 경우 INSERT하여 고유키(시퀀스)만 추출한 뒤 테이블에서 바로 삭제
//			contentsrepo.save(item1);
//		}

		// 테이블 저장 후 ID 반환 (JavaScript 트리에서 노드 key로 사용됨)
		Integer seq = seqs.getId();
		String key = item1.getId();
		params.setTitle(title + " (" + seq + ")");
		params.setKey(key);
		
		// 메뉴 트리에 노드 추가
		ndao.addNode(params);
		
		// 변경이력 저장
		String type = ApplicationConstant.TYPE_GROUP;;
		String filePath = params.getGroupId() + ApplicationConstant.EXT_HTML;
		Optional<ContentsGroup> row = grepo.findById(params.getGroupId());
		if (row.isPresent()) {
			long now = Calendar.getInstance().getTimeInMillis();
			chdao.addHistory(userid, type, method, row.get().getName(), filePath, now + ApplicationConstant.EXT_HTML, ApplicationConstant.REASON_TREE_ADD);
		}
		
		ret.setGroupId(params.getGroupId());
		ret.setKey(params.getKey());
		ret.setParentKey(params.getParentKey());
		ret.setFolder(params.getFolder());
		ret.setTitle(params.getTitle());
		
		return ret;
	}
	
	/**
	 * 노드 삭제 - 폴더인 경우 그룹변경, 도움말인 경우 그룹변경 + 컨텐츠 삭제
	 * @param params
	 * @return
	 */
	@Transactional
	public NodeUpdateResult deleteNode(DeleteNodeReq params, String userid) {
		
		String method = ApplicationConstant.METHOD_DELETE;
		String type = "";
		
		NodeUpdateResult ret = new NodeUpdateResult();
		ret.setGroupId(params.getGroupId());
		ret.setMethod(method);
		ret.setKey(params.getKey());
		
		// 변경 이력
		ChangeHistory item2 = new ChangeHistory();
		item2.setMethod(method);
		
		if (params.getFolder()==null || params.getFolder()==false) {
			// 1. 선택한 노드가 도움말인 경우
			type = ApplicationConstant.TYPE_CONTENT;
			item2.setType(type);
			item2.setFilePath(params.getKey() + ApplicationConstant.EXT_CONTENT);
			// 1) 테이블 행삭제
			PkContents pk = new PkContents(params.getGroupId(), params.getKey());
			Optional<Contents> row = contentsrepo.findById(pk);
			if (row.isPresent()) {
				Contents item1 = row.get();
				contentsrepo.delete(item1);
			}
			// 2) 파일 삭제
			boolean success = fdao.deleteFile(params.getGroupId() + File.separator + params.getKey());
			chdao.addHistory(userid, type, method, params.getTitle(), params.getGroupId() + File.separator + item2.getFilePath(), null, ApplicationConstant.REASON_DELETE_CONTENT);
			// 3) Todo: 첨부 이미지 폴더 삭제
		} else {
			type = ApplicationConstant.TYPE_FOLDER;
			item2.setType(type);
			// 2. 선택한 노드가 폴더인 경우: 하위 노드도 삭제
			if (params.getChild()!=null && params.getChild().size()>0) {
				for (String contentKey : params.getChild()) {
					// 1) 테이블 행삭제
					PkContents pk = new PkContents(params.getGroupId(), contentKey);
					Optional<Contents> row = contentsrepo.findById(pk);
					if (row.isPresent()) {
						Contents item1 = row.get();
						logger.debug("deleting child " + item1.toString());
						contentsrepo.delete(item1);
					}
					// 2) 파일 삭제
					boolean success = fdao.deleteFile(params.getGroupId() + File.separator + contentKey);
				}
			}
			
		}
		
		boolean foundNode = ndao.deleteNodeByKey(params);
		// logger.debug("found node " + foundNode);
		
		Optional<ContentsGroup> row = grepo.findById(params.getGroupId());
		if (row.isPresent()) {
			// 도움말 그룹 히스토리 저장
			chdao.addHistory(userid, ApplicationConstant.TYPE_GROUP, ApplicationConstant.METHOD_MODIFY, 
					row.get().getName(), ret.getGroupId() + ApplicationConstant.EXT_HTML, null, ApplicationConstant.REASON_DELETE_CONTENT_OR_FOLDER);	
		}
		
		return ret;
	}
	
	/**
	 * 노드이름(제목) 변경
	 * @param params
	 * @param userid
	 * @return
	 */
	@Transactional
	public NodeUpdateResult updateNode(UpdateNodeReq params, String userid) {
		
		NodeUpdateResult ret = new NodeUpdateResult();
		
		String method = ApplicationConstant.METHOD_MODIFY;
		String type = ApplicationConstant.TYPE_GROUP;
		String filePath = params.getGroupId() + ApplicationConstant.EXT_HTML;
		
		// 트리구조 저장
		boolean foundNode = false;
		if (params.getMenuCode()!=null) {
			foundNode = ndao.updateNodeKey(params.getGroupId(), params.getKey(), params.getMenuCode());
		} else if (params.getTitle()!=null) {
			// 제목 변경
			foundNode = ndao.replaceTitleByKey(params);
		} else if (params.getParentKey()!=null) {
			// 순서 변경
			foundNode = ndao.updateNodeOrder(params);
		} else {
			return ret;
		}
		
		String title = ndao.getGroupTitle(params);

		long now = Calendar.getInstance().getTimeInMillis();
		
		Optional<ContentsGroup> row = grepo.findById(params.getGroupId());
		ContentsGroup item1 = null;
		if (!row.isPresent()) {
			return null;
		} else {
			item1 = row.get();
			fdao.makeOneContentGroupHTML(item1, now);
		}
		
		// 변경이력 저장
//		logger.debug("rename " + params.getRename());
		if (params.getRename()!=null && params.getRename()==true) {
			chdao.addHistory(userid, type, method, title, filePath, now + ApplicationConstant.EXT_HTML, 
					ApplicationConstant.REASON_TREE_RENAME);
		} else {
			chdao.addHistory(userid, type, method, title, filePath, now + ApplicationConstant.EXT_HTML, 
					ApplicationConstant.REASON_CHANGE_TREE);
		}

		ret.setMethod(method);
		ret.setUsername(userid);
		ret.setKey(params.getKey());
		ret.setGroupId(params.getGroupId());
		ret.setTitle(params.getTitle());
		
		return ret;
	}
	
}
