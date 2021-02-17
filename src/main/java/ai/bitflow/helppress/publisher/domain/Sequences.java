package ai.bitflow.helppress.publisher.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
@Entity
public class Sequences {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(insertable=false, updatable=false)
	private Integer id;
	
}
