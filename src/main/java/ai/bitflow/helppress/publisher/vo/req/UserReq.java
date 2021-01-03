package ai.bitflow.helppress.publisher.vo.req;

import ai.bitflow.helppress.publisher.domain.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Data
public class UserReq {
	
	private String username;
	private String password;
	
	public User toEntity(){
        return User.builder()
                .username(username)
                .password(password)
                .build();
    }
	
}
