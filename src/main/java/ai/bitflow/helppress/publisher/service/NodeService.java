package ai.bitflow.helppress.publisher.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ai.bitflow.helppress.publisher.constant.ApplicationConstant;
import ai.bitflow.helppress.publisher.dao.ChangeHistoryDao;
import ai.bitflow.helppress.publisher.dao.FileDao;
import ai.bitflow.helppress.publisher.dao.NodeDao;
import ai.bitflow.helppress.publisher.domain.ChangeHistory;
import ai.bitflow.helppress.publisher.domain.Contents;
import ai.bitflow.helppress.publisher.domain.ContentsGroup;
import ai.bitflow.helppress.publisher.repository.ContentsGroupRepository;
import ai.bitflow.helppress.publisher.repository.ContentsRepository;
import ai.bitflow.helppress.publisher.vo.req.DeleteNodeReq;
import ai.bitflow.helppress.publisher.vo.req.NewNodeReq;
import ai.bitflow.helppress.publisher.vo.req.UpdateNodeReq;
import ai.bitflow.helppress.publisher.vo.res.result.NodeUpdateResult;
import ai.bitflow.helppress.publisher.vo.tree.Node;


@Service
public class NodeService {

	private final Logger logger = LoggerFactory.getLogger(NodeService.class);
	
	@Autowired
	private ChangeHistoryDao chdao;
	
	@Autowired
	private ContentsRepository contentsrepo;
	
	@Autowired
	private NodeDao ndao;
	
	@Autowired
	private FileDao fdao;
	
	@Autowired
	private ContentsGroupRepository grepo;
	
	
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
			title = "새 도움말 (";
			item1.setAuthor(userid);		
		} else {
			title = "새 폴더 (";
		}
		item1.setTitle(title);
		// 테이블 저장 후 ID 반환 (JavaScript 트리에서 노드 key로 사용됨)
		contentsrepo.save(item1);
		
		// 파일 생성
		String key = String.format("%05d", item1.getId());
		String groupid = params.getGroupId();
		if (params.getFolder()!=null && params.getFolder()==true) {
			contentsrepo.delete(item1);
		}
		
		params.setTitle(title + item1.getId() + ")");
		params.setKey(key);
		
		ndao.addNode(params);
		
		// 변경이력 저장
		String type = ApplicationConstant.TYPE_GROUP;;
		String filePath = groupid + ".html";
		chdao.addHistory(userid, type, method, title, filePath, null);
		
		ret.setParentKey(params.getParentKey());
		ret.setGroupId(params.getGroupId());
		ret.setFolder(params.getFolder());
		ret.setKey(key);
		ret.setTitle(title);
		
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
			// 1. 도움말인 경우
			type = ApplicationConstant.TYPE_CONTENT;
			item2.setType(type);
			item2.setFilePath(params.getKey() + ".html");
			// 1) 테이블 행삭제
			Optional<Contents> row = contentsrepo.findById(Integer.parseInt(params.getKey()));
			if (row.isPresent()) {
				Contents item1 = row.get();
				contentsrepo.delete(item1);
			}
			// 2) 파일 삭제
			boolean success = fdao.deleteFile(params.getKey());
			chdao.addHistory(userid, type, method, params.getTitle(), item2.getFilePath(), null);
			// 3) Todo: 첨부 이미지 폴더 삭제
		} else {
			type = ApplicationConstant.TYPE_FOLDER;
			item2.setType(type);
			// 2. 폴더인 경우: 하위 노드도 삭제
			if (params.getChild()!=null && params.getChild().size()>0) {
				for (String contentKey : params.getChild()) {
					// 1) 테이블 행삭제
					Optional<Contents> row = contentsrepo.findById(Integer.parseInt(contentKey));
					if (row.isPresent()) {
						Contents item1 = row.get();
						contentsrepo.delete(item1);
					}
					// 2) 파일 삭제
					boolean success = fdao.deleteFile(contentKey);
				}
			}
			
		}
		
		// Todo: 트리구조 저장
		boolean foundNode = ndao.deleteNodeByKey(params);
		logger.debug("found node " + foundNode);
		
		// 변경이력 저장 - 도움말 또는 그룹
		chdao.addHistory(userid, ApplicationConstant.TYPE_GROUP, ApplicationConstant.METHOD_MODIFY, params.getTitle(), ret.getGroupId() + ".html", null);
		
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
		String filePath = params.getGroupId() + ".html";
		
		// Todo: 트리구조 저장
		boolean foundNode = false;
		if (params.getTitle()!=null) {
			// 제목 변경
			foundNode = ndao.replaceTitleByKey(params);
		} else if (params.getParentKey()!=null) {
			// 순서 변경
			foundNode = ndao.updateNodeOrder(params);
		} else {
			return ret;
		}
		
		logger.debug("found node " + foundNode);
		String title = ndao.getGroupTitle(params);
		
		Optional<ContentsGroup> row = grepo.findById(params.getGroupId());
		ContentsGroup item1 = null;
		if (!row.isPresent()) {
			return null;
		} else {
			item1 = row.get();
			fdao.makeOneContentGroupHTML(item1);
		}
		
		// 변경이력 저장
		chdao.addHistory(userid, type, method, title, filePath, null);

		ret.setMethod(method);
		ret.setUsername(userid);
		ret.setKey(params.getKey());
		ret.setGroupId(params.getGroupId());
		ret.setTitle(params.getTitle());
		
		return ret;
	}
	
}
