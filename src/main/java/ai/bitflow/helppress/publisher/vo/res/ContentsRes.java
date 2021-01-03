package ai.bitflow.helppress.publisher.vo.res;

import ai.bitflow.helppress.publisher.vo.res.result.ContentResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
@ToString
public class ContentsRes extends GeneralRes {

	private ContentResult result;
	
}
