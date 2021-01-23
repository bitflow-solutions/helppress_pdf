package ai.bitflow.helppress.publisher.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
@Entity
public class ChangeHistory {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(insertable=false, updatable=false)
	private Integer id;
	@Column(length=10)
	private String type;
	@Column(length=10)
	private String method;
	@Column(length=15)
	private String userid;
	@Column(length=255)
	private String filePath;
	@Column(length=255)
	private String title;
	@Column(length=1)
	private Character released;
	@Column(length=255)
	private String comment;
	@Column(length=255)
	private String realPath;
	
	@Transient
	private String status;
	@Transient
	private Boolean del;
	@Transient
	private String fileId;
	@Transient
	private String className;
	
	@CreationTimestamp
    private LocalDateTime updDt;
	
	@Transient
	private String updDtStr;
	
	public String getUpdDtStr() {
		return updDt.format(DateTimeFormatter.ofPattern("MM.dd HH:mm"));
	}
	
	public ChangeHistory(Integer id, String filePath) {
		this.id = id;
		this.filePath = filePath;
	}
	
	public ChangeHistory(String filePath, LocalDateTime updDt) {
		this.filePath = filePath;
		this.updDt = updDt;
	}
	
}
