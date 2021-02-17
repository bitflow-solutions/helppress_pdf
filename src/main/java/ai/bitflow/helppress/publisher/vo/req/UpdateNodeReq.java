package ai.bitflow.helppress.publisher.vo.req;

import lombok.Data;

@Data
public class UpdateNodeReq {

	private String groupId;
	private String key;
	private String title;
	private Boolean folder;
	private Boolean rename; 
	
	// Drag Reorder
	private String parentKey;
	private Integer index;

	// To change menuCode
	private String menuCode;
}
