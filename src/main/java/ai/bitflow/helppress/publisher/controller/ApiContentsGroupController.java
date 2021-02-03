package ai.bitflow.helppress.publisher.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ai.bitflow.helppress.publisher.domain.ContentsGroup;
import ai.bitflow.helppress.publisher.service.ContentsGroupService;
import ai.bitflow.helppress.publisher.util.SpringUtil;
import ai.bitflow.helppress.publisher.vo.req.ContentsGroupReq;
import ai.bitflow.helppress.publisher.vo.res.ContentsGroupRes;
import ai.bitflow.helppress.publisher.vo.res.GeneralRes;
import ai.bitflow.helppress.publisher.vo.res.result.ContentsGroupResult;
import ai.bitflow.helppress.publisher.vo.tree.Node;

/**
 * 도움말그룹 관련 API
 * @author 김성준
 */
@RestController
@RequestMapping("/api/v1/ecm/group") 
public class ApiContentsGroupController {
	
	private final Logger logger = LoggerFactory.getLogger(ApiContentsGroupController.class);
	
	@Autowired
	private ContentsGroupService gservice;
	
	@Autowired 
	private SimpMessagingTemplate broker;
	
	
	/**
	 * 도움말그룹 저장
	 * @param params
	 * @return
	 */
	@PostMapping("/{groupId}")
	public GeneralRes newGroup(@PathVariable String groupId, ContentsGroupReq params, HttpSession sess) {
		logger.debug("params " + params.toString());
		params.setGroupId(groupId);
		String username = SpringUtil.getSessionUserid(sess);
		GeneralRes ret = new GeneralRes();
		if (username==null) {
			ret.setFailResponse(401);
		} else {
			try {
				String res = gservice.newGroup(params, username);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	/**
	 * 도움말그룹 조회
	 * @param groupId
	 * @return
	 */
	@GetMapping("/{groupId}")
	public ContentsGroupRes getGroup(@PathVariable String groupId) {
		ContentsGroup res = gservice.getGroup(groupId);
		ContentsGroupRes ret = new ContentsGroupRes();
		if (res!=null) {
			ContentsGroupResult rst = new ContentsGroupResult();
			rst.setGroupId(res.getGroupId());
			logger.debug("tree " + res.getTree());
			rst.setTree(new Gson().fromJson(res.getTree(), new TypeToken<List<Node>>(){}.getType()));
			ret.setResult(rst);
		}
		return ret;
	}
	
	/**
	 * 도움말그룹 수정
	 * @param groupId
	 * @param params
	 * @return
	 */
	@PutMapping("/{groupId}")
	public ContentsGroupRes putGroup(@PathVariable String groupId, ContentsGroupReq params, HttpSession sess) {
		params.setGroupId(groupId);
		String username = SpringUtil.getSessionUserid(sess);
		ContentsGroupRes ret = new ContentsGroupRes();
		if (username==null) {
			ret.setFailResponse(401);
		} else {
			try {
				ContentsGroup res = gservice.updateGroup(params, username);
				ContentsGroupResult rst = new ContentsGroupResult();
				rst.setGroupId(res.getGroupId());
				rst.setTree(new Gson().fromJson(res.getTree(), new TypeToken<List<Node>>(){}.getType()));
				ret.setResult(rst);
				broker.convertAndSend("/group", rst);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	/**
	 * 도움말그룹 삭제
	 * @param groupId
	 * @return
	 */
	@DeleteMapping("/{groupId}")
	public GeneralRes deleteGroup(@PathVariable String groupId, HttpSession sess) {
		logger.debug("params " + groupId);
		GeneralRes ret = new GeneralRes();
		String username = SpringUtil.getSessionUserid(sess);
		if (username==null) {
			ret.setFailResponse(401);
		} else {
			gservice.deleteGroup(groupId, username);
		}
		return ret;
	}
	
	/**
	 * 도움말그룹 목록 조회
	 * @param params
	 * @return
	 */
	@GetMapping("")
	public List<ContentsGroup> getGroups() {
		return gservice.getGroups();
	}
	
}
