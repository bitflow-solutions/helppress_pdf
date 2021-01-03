package ai.bitflow.helppress.publisher.vo.res;

import ai.bitflow.helppress.publisher.vo.res.result.ContentsGroupResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
@ToString
public class ContentsGroupRes extends GeneralRes {
	
	private ContentsGroupResult result;
	
}
