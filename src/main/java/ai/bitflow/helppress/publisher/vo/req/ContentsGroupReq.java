package ai.bitflow.helppress.publisher.vo.req;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
public class ContentsGroupReq {
	
	private String groupId;
	private String name;
	private Short orderNo;
	private String tree;
	
}
