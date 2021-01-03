package ai.bitflow.helppress.publisher.vo.res.result;

import java.util.List;

import ai.bitflow.helppress.publisher.vo.tree.Node;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContentsGroupResult {
	private String groupId;
	private List<Node> tree;
}
