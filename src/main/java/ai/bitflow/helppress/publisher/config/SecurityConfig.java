package ai.bitflow.helppress.publisher.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ai.bitflow.helppress.publisher.service.UserService;
import lombok.AllArgsConstructor;


/**
 * Spring Security 설정
 * @author ted@bitflow.ai
 */
@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private UserService userDetailsService;
	
	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
    @Override
    public void configure(WebSecurity web) throws Exception {
    	web.ignoring().antMatchers("/SynapEditor/**", "/js/**", "/css/**", "/img/**", "/api/v1/**");
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
    
    /**
     * 
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        	.headers().frameOptions().disable()
        	.and()
		        .csrf().disable()
		        .httpBasic()
	        .and()
	            .authorizeRequests()
	            .antMatchers("/group/**").hasRole("ADMIN")
	            .antMatchers("/content/**").hasRole("ADMIN")
	            .antMatchers("/release/**").hasRole("ADMIN")
	            .antMatchers("/user/**").hasRole("ADMIN")
	            .antMatchers("/**").permitAll()
	        .and()
	        	.formLogin()
	        	.loginPage("/login")
                // .defaultSuccessUrl("/content")
            .and()
            	.logout()
            	.logoutUrl("/logout")
            	.logoutSuccessUrl("/login")
            .permitAll();
//        http.sessionManagement().maximumSessions(1).expiredUrl("/user/login?expired");
    }
    
//    @Bean
//    public AuthenticationSuccessHandler successHandler() {
//        return new LoginSuccessHandler();
//    }
    

}
