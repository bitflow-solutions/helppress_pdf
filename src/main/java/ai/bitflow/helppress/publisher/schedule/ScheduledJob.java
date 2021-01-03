package ai.bitflow.helppress.publisher.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ai.bitflow.helppress.publisher.service.ReleaseService;

/**
 * 고정주기 작업
 * @author metho
 */
@Component
public class ScheduledJob {
   
	private final Logger logger = LoggerFactory.getLogger(ScheduledJob.class);

	@Autowired
	private ReleaseService rservice;
	
	@Scheduled(fixedDelay  = 6 * 60 * 60 * 1000)
	public void batchProcess() {
		logger.debug("batchProcess");
		// 불필요 히스토리 이력 제거
		rservice.deleteUnusedHistories();
	}
   
}