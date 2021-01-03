package ai.bitflow.helppress.publisher.controller;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.bitflow.helppress.publisher.domain.Contents;
import ai.bitflow.helppress.publisher.service.ContentsService;
import ai.bitflow.helppress.publisher.util.SpringUtil;
import ai.bitflow.helppress.publisher.vo.req.ContentsReq;
import ai.bitflow.helppress.publisher.vo.res.ContentsRes;
import ai.bitflow.helppress.publisher.vo.res.StringRes;
import ai.bitflow.helppress.publisher.vo.res.result.ContentResult;

/**
 * 
 * @author method76
 */
@RestController
@RequestMapping("/api/v1/ecm/content") 
public class ApiContentController {
	
	private final Logger logger = LoggerFactory.getLogger(ApiContentController.class);
	
	@Autowired
	private ContentsService cservice;
	
	/**
	 * 컨텐츠 조회
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public ContentsRes get(@PathVariable String id) {
		ContentsRes ret = new ContentsRes();
		Contents item = cservice.getContent(id);
		ContentResult result = new ContentResult();
		if (item!=null) {
			result.setTitle(item.getTitle());
			result.setContents(item.getContent());
		} else {
			ret.setFailResponse(404);
		}
		ret.setResult(result);
		return ret;
	}
	
	@GetMapping("/type/{id}")
	public StringRes getType(@PathVariable String id) {
		Contents item = cservice.getContent(id);
		StringRes ret = new StringRes();
		if (item!=null) {
			ret.setResult(item.getType());
		} else {
			ret.setFailResponse(404);
		}
		return ret;
	}
	
	/**
	 * 컨텐츠 삭제
	 * @param id
	 * @return
	 */
	@DeleteMapping("/{id}")
	public ContentsRes delete(@PathVariable String id, HttpSession sess) {
		ContentsRes ret = new ContentsRes();
		String username = SpringUtil.getSessionUserid(sess);
		if (username==null) {
			ret.setFailResponse(401);
		} else {
			boolean success = cservice.deleteContent(id, username);
		}
		return ret;
	}
	
	/**
	 * 컨텐츠 수정
	 * @param params
	 * @param id
	 * @return
	 */
	@PutMapping("/{id}")
	public ContentsRes updateContent(ContentsReq params, @PathVariable String id, HttpSession sess) {
		logger.debug("params " + params.toString());
		ContentsRes ret = new ContentsRes();
		ContentResult result = new ContentResult();
		String username = SpringUtil.getSessionUserid(sess);
		if (username==null) {
			ret.setFailResponse(401);
		} else {
			if (params.getFile1()==null) {
				cservice.updateContent(params, id, username);
			} else {
				cservice.updatePdfContent(params, id, username);
			}
			result.setKey(id);
			ret.setResult(result);
		}
		return ret;
	}
	
}
