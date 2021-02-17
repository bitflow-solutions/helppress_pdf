package ai.bitflow.helppress.publisher.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;

import ai.bitflow.helppress.publisher.domain.idclass.PkContents;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
@Entity
@IdClass(PkContents.class)
public class Contents {
	
	@Id
	@Column(length=20)
	private String groupId; 
	@Id
	@Column(length=5)
	private String id;
	
	// html, pdf
	private String type;
	@Lob
	private String content;
	@Column(length=50)
	@NotNull
	private String author;
	@CreationTimestamp
	@Column(updatable = false)
    private LocalDateTime regDt;
	
	private Boolean released;

	@Transient
	private String title;
}
