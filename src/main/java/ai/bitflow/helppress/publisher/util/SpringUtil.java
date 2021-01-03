package ai.bitflow.helppress.publisher.util;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

public class SpringUtil {

	/**
	 * 
	 * @param sess
	 * @return
	 */
	public static String getSessionUserid(HttpSession sess) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); 
		SecurityContextHolder.getContext().setAuthentication(authentication);
		SecurityContextImpl sessobj = (SecurityContextImpl) sess.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
		if (sessobj==null || sessobj.getAuthentication()==null || sessobj.getAuthentication().getName()==null) {
			return null;
		}
		return sessobj.getAuthentication().getName();
	}
	
}
