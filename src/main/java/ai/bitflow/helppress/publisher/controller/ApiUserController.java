package ai.bitflow.helppress.publisher.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.bitflow.helppress.publisher.service.UserService;
import ai.bitflow.helppress.publisher.vo.req.UserReq;
import ai.bitflow.helppress.publisher.vo.res.StringRes;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author method76
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ecm/user") 
public class ApiUserController {
	
	private final Logger logger = LoggerFactory.getLogger(ApiUserController.class);
	
	@Autowired
	private UserService uservice;
	
	@Autowired
	private AuthenticationSuccessHandler successHandler;
	
	/**
	 * 관리자 등록 처리
	 * @param params
	 * @return
	 */
	@PostMapping("/join") 
	public StringRes join(UserReq params) {
		StringRes ret = new StringRes();
		String id = null;
		try {
			id = uservice.addUser(params);
			ret.setResult(id);
		} catch(Exception e) {
			ret.setFailResponse();
			e.printStackTrace();
		}
		if (id==null) {
			ret.setFailResponse();
		}
		return ret;
	}
	
	/**
	 * 관리자 로그인 처리
	 * @param params
	 * @param sess
	 * @return
	 */
	@PostMapping("/login") 
	public StringRes login(UserReq params, HttpSession sess, HttpServletRequest request, HttpServletResponse response) {
		StringRes ret = new StringRes();
		if (!uservice.hasUser(params)) {
			ret.setFailResponse();
		} else {
			List<GrantedAuthority> authlist = AuthorityUtils.createAuthorityList("ROLE_ADMIN");
			Authentication auth = new UsernamePasswordAuthenticationToken(params.getUsername(), params.getPassword(), authlist);
			SecurityContextHolder.getContext().setAuthentication(auth);
			sess.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
					SecurityContextHolder.getContext());
			sess.setAttribute("userid", params.getUsername());
			String referer = request.getHeader("Referer");
			logger.debug("referer " + referer);
			try {
				successHandler.onAuthenticationSuccess(request, response, auth);
				Object redirectUrl = sess.getAttribute("redirectUrl");
				if (redirectUrl!=null && redirectUrl instanceof String) {
					ret.setResult((String) redirectUrl);
				} else {
					ret.setResult("/");
				}
				logger.debug("login redirectUrl " + ret.getResult());
				sess.removeAttribute("redirectUrl");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ServletException e) {
				e.printStackTrace();
			}
		}
		logger.debug("return");
		return ret;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 */
	@PutMapping("") 
	public StringRes updateUser(UserReq params) {
		log.debug("modifyUser");
		StringRes ret = new StringRes();
		String id = null;
		try {
			id = uservice.updateUser(params);
			ret.setResult(id);
		} catch(Exception e) {
			ret.setFailResponse();
			e.printStackTrace();
		}
		if (id==null) {
			ret.setFailResponse();
		}
		return ret;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 */
	@DeleteMapping("") 
	public StringRes deleteUser(UserReq params) {
		log.debug("deleteUser");
		StringRes ret = new StringRes();
		try {
			uservice.deleteUser(params);
		} catch(Exception e) {
			ret.setFailResponse();
			e.printStackTrace();
		}
		return ret;
	}
	
}
