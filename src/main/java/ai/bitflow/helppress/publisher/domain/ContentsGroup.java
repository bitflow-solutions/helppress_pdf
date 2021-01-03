package ai.bitflow.helppress.publisher.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
@Entity
public class ContentsGroup {
	
	@Id
	@Column(length=50)
	private String groupId;
	private short orderNo;
	@Column(length=255)
	private String name;
	@Column(length=20000000)
	private String tree;
	
	@Transient
	private String className;
	
	@Builder
	public ContentsGroup(String groupId, String name, short orderNo) {
		this.groupId = groupId;
		this.name = name;
		this.orderNo = orderNo;
	}
	
}
