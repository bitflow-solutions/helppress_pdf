package ai.bitflow.helppress.publisher.vo.res;

import ai.bitflow.helppress.publisher.vo.res.result.NodeUpdateResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
@ToString
public class NodeUpdateRes extends GeneralRes {

	private NodeUpdateResult result;
	
}
