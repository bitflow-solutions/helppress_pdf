package ai.bitflow.helppress.publisher.vo.res;

import java.util.List;

import ai.bitflow.helppress.publisher.domain.Contents;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
@ToString
public class SearchRes extends GeneralRes {

	private List<Contents> result;
	
}
