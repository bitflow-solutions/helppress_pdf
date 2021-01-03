package ai.bitflow.helppress.publisher.domain.idclass;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;

public class PkContentTree implements Serializable {

	private static final long serialVersionUID = -5733115169086314738L;

	@Id
	@Column(length=50)
	private String groupId;
	@Id
	private Integer id;
	
}
