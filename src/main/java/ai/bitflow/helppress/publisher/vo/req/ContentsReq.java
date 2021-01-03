package ai.bitflow.helppress.publisher.vo.req;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ContentsReq {

	private String title;
	private String content;
	private String comment;
	private MultipartFile file1;
	
}
