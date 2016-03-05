package com.seaky.hamster.admin;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

@Configuration
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	public WebSecurityConfig() {
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/**/*.js");
		web.ignoring().antMatchers("/**/*.css");
		web.ignoring().antMatchers("/**/*.png");
		web.ignoring().antMatchers("/**/*.gif");
		web.ignoring().antMatchers("/**/*.jpg");
		web.ignoring().antMatchers("/**/*.jpeg");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().anyRequest().authenticated().and().csrf()
				.disable().formLogin().loginPage("/login.html")
				.loginProcessingUrl("/dologin").passwordParameter("password")
				.usernameParameter("username").permitAll().and().logout().logoutUrl("/logout").permitAll();
		http.formLogin().defaultSuccessUrl("/index.html", true);
	}

	@Configuration
	protected static class GlobalConfig extends
			GlobalAuthenticationConfigurerAdapter {

		@Override
		public void init(AuthenticationManagerBuilder auth) throws Exception {
			auth.inMemoryAuthentication().withUser("test").password("test")
					.roles("USER");
			auth.inMemoryAuthentication().withUser("admin")
					.password("admin123456").roles("ADMIN");
		}
	}
}
