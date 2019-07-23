package com.authentication.system.auth;

import java.io.IOException;
import java.sql.Date;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.authentication.system.common.JwtConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtUsernameAndPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	// We use auth manager to validate the user credentials
	private AuthenticationManager authManager;

	@Autowired
	private final JwtConfig jwtConfig1;

	private int time;
	private String userId;

	public JwtUsernameAndPasswordAuthenticationFilter(AuthenticationManager authManager, JwtConfig jwtConfig) {
		this.authManager = authManager;
		this.jwtConfig1 = jwtConfig;

		// By default, UsernamePasswordAuthenticationFilter listens to "/login" path.
		// In our case, we use "/auth". So, we need to override the defaults.

		this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(jwtConfig1.getUri(), "POST"));
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {

		try {

			// 1. Get credentials from request
			UserCredentials creds = new ObjectMapper().readValue(request.getInputStream(), UserCredentials.class);

			// 2. Create auth object (contains credentials) which will be used by auth
			// manager
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(creds.getUsername(),
					creds.getPassword(), Collections.emptyList());

			this.userId = creds.getUsername();

			// 3. Authentication manager authenticate the user, and use
			// UserDetialsServiceImpl::loadUserByUsername() method to load the user.
			return authManager.authenticate(authToken);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// Upon successful authentication, generate a token.
	// The 'auth' passed to successfulAuthentication() is the current authenticated
	// user.
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication auth) throws IOException, ServletException {
		String channel = request.getHeader("Channel");
		System.out.println("ini adalah channel = " + channel);

		if (channel.equals("MobileBanking")) {
			time = jwtConfig1.getExpiration1();
		} else {
			time = jwtConfig1.getExpiration2();
		}
		Long now = System.currentTimeMillis();
		String token = Jwts.builder().setSubject(auth.getName())
				// Convert to list of strings.
				// This is important because it affects the way we get them back in the Gateway.
				.claim("authorities",
						auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.setIssuedAt(new Date(now)).setExpiration(new Date(now + time * 1000)) // in milliseconds
				.signWith(SignatureAlgorithm.HS512, jwtConfig1.getSecret().getBytes()).compact();
		// Add token to header
		dataCredential(userId, token);
		response.addHeader(jwtConfig1.getHeader(), jwtConfig1.getPrefix() + " " + token);
	}

	@Async("transactionPoolExecutor")
	public void dataCredential(String userId, String token) {
		String result = "";
		HttpClient client = new HttpClient();
		GetMethod getMethod = new GetMethod("http://localhost:8100/" + userId + "/" + token);
		try {
			client.executeMethod(getMethod);
			result = getMethod.getResponseBodyAsString();
			System.out.println("USER ID = " + userId);
			System.out.println("TOKEN = " + token);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			getMethod.releaseConnection();
		}
	}

	// A (temporary) class just to represent the user credentials
	private static class UserCredentials {
		private String username, password;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}
}