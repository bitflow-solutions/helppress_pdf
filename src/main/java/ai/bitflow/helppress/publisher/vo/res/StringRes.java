package ai.bitflow.helppress.publisher.vo.res;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
@ToString
public class StringRes extends GeneralRes {

	private String result;
	
}
