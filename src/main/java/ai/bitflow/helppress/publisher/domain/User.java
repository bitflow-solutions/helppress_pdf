package ai.bitflow.helppress.publisher.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@NoArgsConstructor
@ToString
public class User {
	
	@Id
	@Column(nullable=false, length=15)
	private String username;
	
	@Column(nullable=false, length=128)
	private String password;
	
	@Transient
	private String userid;
	
	@Builder
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
}
