package ai.bitflow.helppress.publisher.vo.sess;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import ai.bitflow.helppress.publisher.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSession implements UserDetails {

	private static final long serialVersionUID = -3661880595391396749L;
	
	private String username;
	private String password;
    private boolean isEnabled;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private Collection<? extends GrantedAuthority> authorities;
    
    public UserSession(User user) {
    	this.username = user.getUsername();
    }
    
    public UserSession(String username, String password, Set<GrantedAuthority> authorities) {
    	this.username = username;
    	this.password = password;
    	this.authorities = authorities;
    }

    
}
