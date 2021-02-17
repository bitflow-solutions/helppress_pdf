package ai.bitflow.helppress.publisher.controller;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.bitflow.helppress.publisher.service.NodeService;
import ai.bitflow.helppress.publisher.util.SpringUtil;
import ai.bitflow.helppress.publisher.vo.req.DeleteNodeReq;
import ai.bitflow.helppress.publisher.vo.req.NewNodeReq;
import ai.bitflow.helppress.publisher.vo.req.UpdateNodeReq;
import ai.bitflow.helppress.publisher.vo.res.NodeUpdateRes;
import ai.bitflow.helppress.publisher.vo.res.result.NodeUpdateResult;

/**
 * 도움말 메뉴 트리 조작
 * @author method76
 */
@RestController
@RequestMapping("/api/v1/ecm/node") 
public class ApiNodeController {
	
	private final Logger logger = LoggerFactory.getLogger(ApiNodeController.class);
	
	@Autowired
	private NodeService nservice;
	
	@Autowired 
	private SimpMessagingTemplate broker;
	
	/**
	 * 새노드 추가
	 * @param params
	 * @return
	 */
	@PostMapping("")
	public NodeUpdateRes newNode(NewNodeReq params, HttpSession sess) {

		NodeUpdateRes ret = new NodeUpdateRes();
		String username = SpringUtil.getSessionUserid(sess);
		if (username==null) {
			ret.setFailResponse(401);
		} else {
			NodeUpdateResult res = nservice.newNode(params, username);
			res.setUsername(username);
			ret.setResult(res);
			broker.convertAndSend("/node", res);
		}
		
		return ret;
	}
	
	/**
	 * 노드 삭제
	 * @param key
	 * @param params
	 * @return
	 */
	@DeleteMapping("")
	public NodeUpdateRes deleteNode(DeleteNodeReq params, HttpSession sess) {
		
		NodeUpdateRes ret = new NodeUpdateRes();
		String username = SpringUtil.getSessionUserid(sess);
		if (username==null) {
			ret.setFailResponse(401);
		} else {
			NodeUpdateResult res = nservice.deleteNode(params, username);
			res.setUsername(username);
			ret.setResult(res);
			broker.convertAndSend("/node", res);			
		}
		
		return ret;
	}
	
	/**
	 * 노드 제목 또는 순서 변경
	 * @param key
	 * @param params
	 * @return
	 */
	@PutMapping("")
	public NodeUpdateRes updateNode(UpdateNodeReq params, HttpSession sess) {

		NodeUpdateRes ret = new NodeUpdateRes();
		String username = SpringUtil.getSessionUserid(sess);
		if (username==null) {
			ret.setFailResponse(401);
		} else {
			NodeUpdateResult res = nservice.updateNode(params, username);
			ret.setResult(res);
			broker.convertAndSend("/node", res);
		}

		return ret;
	}
	
}
