package ai.bitflow.helppress.publisher.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import ai.bitflow.helppress.publisher.domain.User;
import ai.bitflow.helppress.publisher.repository.UserRepository;
import ai.bitflow.helppress.publisher.vo.req.UserReq;
import ai.bitflow.helppress.publisher.vo.sess.UserSession;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class UserService implements UserDetailsService {

	private final Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private UserRepository urepo;
	
	/**
	 * 등록된 사용자가 있는지 확인
	 * @return
	 */
	public boolean hasUser(UserReq params) {
		boolean ret = false;
		Optional<User> row = urepo.findById(params.getUsername());
		if (row.isPresent()) {
			User item = row.get();
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			ret = encoder.matches(params.getPassword(), item.getPassword());
		}
		return ret;
	}

	@CacheEvict(value="users", allEntries=true)
	@Transactional
    public String addUser(UserReq params) {
        // 비밀번호 암호화
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		params.setPassword(encoder.encode(params.getPassword()));
		return urepo.save(params.toEntity()).getUsername();
    }
	
	@CacheEvict(value="users", allEntries=true)
	@Transactional
    public String updateUser(UserReq params) {
		Optional<User> row = urepo.findById(params.getUsername());
		if (row.isPresent()) {
			User item = row.get();
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			item.setPassword(encoder.encode(params.getPassword()));
			return urepo.save(item).getUsername();
		} else {
			return null;
		}
    }
	
	@CacheEvict(value="users", allEntries=true)
	@Transactional
    public void deleteUser(UserReq params) {
		Optional<User> row = urepo.findById(params.getUsername());
		if (row.isPresent()) {
			urepo.delete(row.get());
		}
    }
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = urepo.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
		logger.debug("user " + user.toString());
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return new UserSession(user.getUsername(), user.getPassword(), grantedAuthorities);
	}
	
	/**
	 * 전체 사용자 조회
	 * @return
	 */
	@Cacheable(value="users")
	public List<User> getUsers() {
		return urepo.findAll();
    }
	
}
