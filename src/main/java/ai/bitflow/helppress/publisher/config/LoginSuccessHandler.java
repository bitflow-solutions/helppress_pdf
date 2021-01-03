package ai.bitflow.helppress.publisher.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * ref) https://www.baeldung.com/spring-security-redirect-login
 * ref) https://granger.tistory.com/55
 *
 * @author metho
 */
@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	
	private final Logger logger = LoggerFactory.getLogger(LoginSuccessHandler.class);

	private RequestCache requestCache = new HttpSessionRequestCache();
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		SavedRequest savedRequest = requestCache.getRequest(request, response);
		
		if (savedRequest==null) {
			return;
		}
		
		String targetUrlParameter = getTargetUrlParameter();
		if (isAlwaysUseDefaultTargetUrl()
				|| (targetUrlParameter!=null && StringUtils.hasText(request.getParameter(targetUrlParameter)))) {
			return;
		}
		
		clearAuthenticationAttributes(request);
		String targetUrl = savedRequest.getRedirectUrl()==null ? "/" : savedRequest.getRedirectUrl();
		request.getSession(true).setAttribute("redirectUrl", targetUrl);
		
	}
    
}
