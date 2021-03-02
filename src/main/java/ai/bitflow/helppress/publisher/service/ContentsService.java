package ai.bitflow.helppress.publisher.service;

import java.util.Calendar;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ai.bitflow.helppress.publisher.constant.ApplicationConstant;
import ai.bitflow.helppress.publisher.dao.ChangeHistoryDao;
import ai.bitflow.helppress.publisher.dao.FileDao;
import ai.bitflow.helppress.publisher.dao.NodeDao;
import ai.bitflow.helppress.publisher.domain.Contents;
import ai.bitflow.helppress.publisher.domain.idclass.PkContents;
import ai.bitflow.helppress.publisher.repository.ContentsRepository;
import ai.bitflow.helppress.publisher.vo.req.ContentsReq;


@Service
public class ContentsService implements ApplicationConstant {

	private final Logger logger = LoggerFactory.getLogger(ContentsService.class);
	
	@Autowired
	private ContentsRepository contentsrepo;
	
	@Autowired
	private ChangeHistoryDao chdao;
	
	@Autowired
	private NodeDao ndao;
	
	@Autowired
	private FileDao fdao;
	

	/**
	 * 컨텐츠 수정
	 * @param params
	 * @param key
	 * @return
	 */
	@Transactional
	public String updateContent(ContentsReq params, String groupId, String key, String userid) {
		// id가 폴더이면 childDoc, id가 파일이면 업데이트
		PkContents pk = new PkContents(groupId, key);
		Optional<Contents> row1 = contentsrepo.findById(pk);
		if (row1.isPresent()) {
			// 기존 파일 업데이트
			Contents item1 = row1.get();
			item1.setType(ApplicationConstant.TYPE_HTML);
			item1.setContent(params.getContent());
			Contents item2 = contentsrepo.save(item1);
			fdao.updateContentFile(item2);
			
			// 변경이력 저장
			String type     = TYPE_CONTENT;
			String method   = METHOD_MODIFY;
			String filePath = params.getMenuCode() + ApplicationConstant.EXT_CONTENT;

			long now = Calendar.getInstance().getTimeInMillis();
			chdao.addHistory(userid, type, method, params.getTitle(), groupId + "/" + filePath, groupId + "/" + now + ApplicationConstant.EXT_CONTENT, "도움말 수정");
		
			return String.valueOf(item2.getId());
		}
		return null;
	}
	
	@Transactional
	public String updatePdfContent(ContentsReq params, String groupId, String userid) {
		// id가 폴더이면 childDoc, id가 파일이면 업데이트
		PkContents pk = new PkContents(groupId, params.getKey());
		// id가 폴더이면 childDoc, id가 파일이면 업데이트
		Optional<Contents> row1 = contentsrepo.findById(pk);
		logger.debug("isPresent " + row1.isPresent());
		
		String type     = TYPE_CONTENT;
		String filePath = params.getMenuCode() + ApplicationConstant.EXT_PDF;
		String method = null;
		Contents item1 = null;
		if (row1.isPresent()) {
			// 기존 파일 업데이트
			item1 = row1.get();
			if (!params.getKey().equals(params.getMenuCode())) {
				// 메뉴코드가 바뀐 경우 - 기존 노드 삭제 후 새로 인서트
				contentsrepo.delete(item1);
				boolean foundNode = ndao.updateNodeKey(groupId, params.getKey(), params.getMenuCode());
				pk = new PkContents(groupId, params.getMenuCode());
				item1 = new Contents();
				item1.setGroupId(groupId);
				item1.setId(params.getMenuCode());
				item1.setAuthor(userid);
				item1.setType(ApplicationConstant.TYPE_PDF);
			} else {
				// 메뉴코드가 동일한 경우
			}
			method = METHOD_MODIFY;
		} else {
			method = METHOD_ADD;
			// 새로 저장
			pk = new PkContents(groupId, params.getMenuCode());
			item1 = new Contents();
			item1.setGroupId(groupId);
			item1.setId(params.getMenuCode());
			item1.setAuthor(userid);
			item1.setType(ApplicationConstant.TYPE_PDF);
		}
		
		contentsrepo.save(item1);
		long now = Calendar.getInstance().getTimeInMillis();
		fdao.newPdfFile(params, item1, now);
		// 변경이력 저장
		chdao.addHistory(userid, type, method, params.getTitle(), groupId + "/" + filePath, groupId + "/" + now + ApplicationConstant.EXT_PDF, params.getComment());
		return item1.getId();
	}
	
	/**
	 * 현재 미사용
	 * @param id
	 * @return
	 */
	public Contents getContent(String id) {
		// Optional<Contents> row = contentsrepo.findById(Integer.parseInt(id));
		// return row.isPresent()?row.get():null;
		return null;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	/*
	@Transactional
	public boolean deleteContent(String id, String userid) {
		Optional<Contents> row = contentsrepo.findById(Integer.parseInt(id));
		if (row.isPresent()) {
			Contents item = row.get();
			contentsrepo.delete(item);
			// 변경이력 저장
			chdao.addHistory(userid, TYPE_CONTENT, METHOD_DELETE, null, id + ".html", null);
		}
		return true;
	}
	*/

}
