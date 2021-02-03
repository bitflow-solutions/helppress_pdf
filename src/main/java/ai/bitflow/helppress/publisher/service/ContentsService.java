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
import ai.bitflow.helppress.publisher.domain.Contents;
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
	private FileDao fdao;
	

	/**
	 * 컨텐츠 수정
	 * @param params
	 * @param key
	 * @return
	 */
	@Transactional
	public String updateContent(ContentsReq params, String key, String userid) {
		// id가 폴더이면 childDoc, id가 파일이면 업데이트
		Optional<Contents> row1 = contentsrepo.findById(Integer.parseInt(key));
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
			String filePath = key + ApplicationConstant.EXT_CONTENT;

			long now = Calendar.getInstance().getTimeInMillis();
			chdao.addHistory(userid, type, method, params.getTitle(), filePath, now + ApplicationConstant.EXT_CONTENT, "도움말 수정");
		
			return String.valueOf(item2.getId());
		}
		return null;
	}
	
	@Transactional
	public String updatePdfContent(ContentsReq params, String key, String userid) {
		// id가 폴더이면 childDoc, id가 파일이면 업데이트
		Optional<Contents> row1 = contentsrepo.findById(Integer.parseInt(key));
		if (row1.isPresent()) {
			// 기존 파일 업데이트
			Contents item1 = row1.get();
			item1.setType(ApplicationConstant.TYPE_PDF);
			Contents item2 = contentsrepo.save(item1);
			
			long now = Calendar.getInstance().getTimeInMillis();
			fdao.newPdfFile(params, item2, now);
			// 변경이력 저장
			String type     = TYPE_CONTENT;
			String method   = METHOD_MODIFY;
			String filePath = key + ApplicationConstant.EXT_PDF;
			
			chdao.addHistory(userid, type, method, params.getTitle(), filePath, now + ApplicationConstant.EXT_PDF, params.getComment());
			return String.valueOf(item2.getId());
		}
		return null;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public Contents getContent(String id) {
		Optional<Contents> row = contentsrepo.findById(Integer.parseInt(id));
		return row.isPresent()?row.get():null;
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
