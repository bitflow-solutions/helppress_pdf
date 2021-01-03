package ai.bitflow.helppress.publisher.vo.req;

import java.util.List;

import lombok.Data;

@Data
public class DeleteNodeReq {

	private String groupId;
	private String key;
	private String title;
	private Boolean folder;
	private List<String> child;
	
}
