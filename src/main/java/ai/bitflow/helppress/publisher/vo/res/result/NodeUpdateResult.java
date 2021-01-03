package ai.bitflow.helppress.publisher.vo.res.result;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
public class NodeUpdateResult {

	private String groupId;
	private String method;
	private String parentKey;
	private String title;
	private String key;
	private Boolean folder;
	private String username;
	
}
