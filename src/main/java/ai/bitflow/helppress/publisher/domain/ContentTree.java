package ai.bitflow.helppress.publisher.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import ai.bitflow.helppress.publisher.domain.idclass.PkContentTree;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
@Entity
@IdClass(PkContentTree.class)
public class ContentTree {
	
	@Id
	@Column(length=50)
	private String groupId;
	@Id
	private Integer id;
	private Integer parentId;
	private String text;
	@Column(nullable = false, columnDefinition = "TINYINT(1) default 0")
	private boolean isFolder;
	
	
}
