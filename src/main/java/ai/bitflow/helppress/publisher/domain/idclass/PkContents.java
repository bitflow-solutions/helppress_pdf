package ai.bitflow.helppress.publisher.domain.idclass;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
public class PkContents implements Serializable {

	private static final long serialVersionUID = -5733115169086314138L;

	@Id
	@Column(length=20)
	private String groupId;
	@Id
	@Column(length=5)
	private String id;
	
	public PkContents(String groupId, String id) {
		this.groupId = groupId;
		this.id = id;
	}
	
}
