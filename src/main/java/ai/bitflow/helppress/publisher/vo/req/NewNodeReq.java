package ai.bitflow.helppress.publisher.vo.req;

import lombok.Data;

@Data
public class NewNodeReq {

	private String groupId;
	private String parentKey;
	private Boolean folder;
	
	// later add
	private String key;
	private String title;
	
}
