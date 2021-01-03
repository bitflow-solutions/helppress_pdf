package ai.bitflow.helppress.publisher.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * 웹소켓 처리 컨트롤러 
 * @author method76
 */
@Controller
public class WebSocketController {
	
	private final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
	
	@Autowired 
	private SimpMessagingTemplate broker;
	
	@MessageMapping("/appendChild")
	@SendTo("/group")
	public String hello() throws Exception {
		broker.convertAndSend("/group", "world");
		return "Hello";
	}
	
}
