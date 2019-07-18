package com.authentication.system.auth;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service   // It has to be annotated with @Service.
public class UserDetailsServiceImpl implements UserDetailsService  {
	private String userId;
	private int id;
	
	@Autowired
	private BCryptPasswordEncoder encoder;

	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		String result = "";
		HttpClient client = new HttpClient();
		GetMethod getMethod = new GetMethod("http://localhost:8100/credential/user");
		CredentialUserResponse credential = new CredentialUserResponse();
		
		try {
			client.executeMethod(getMethod);
			result = getMethod.getResponseBodyAsString();
//			System.out.println(result);
		} catch (Exception e) {	
		} finally {
			getMethod.releaseConnection();
		}
		
		HashMap<String,Object> result1 = new HashMap<>();
		try {
            result1 = new ObjectMapper().readValue(result, HashMap.class);
            credential.setData(result1.get("data"));
            
            
            
//            System.out.println(credential.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		ArrayList<LinkedHashMap<String, Object>> data = credential.getData();
		final List<AppUser> user1 = new ArrayList<UserDetailsServiceImpl.AppUser>();
		
		for (int i=0; i < data.size(); i++) {
			AppUser user = new AppUser();
			user.setId(data.get(i).get("id"));
			user.setUsername(data.get(i).get("userId"));
			user.setPassword(encoder.encode(data.get(i).get("password").toString()));
			user.setRole(data.get(i).get("role"));
			user1.add(user);
		}
		System.out.println(user1);
		
		// hard coding the users. All passwords must be encoded.
//		final List<AppUser> users = Arrays.asList(
//				new AppUser(1, "omar", encoder.encode("12345"), "USER"),
//				new AppUser(2, "admin", encoder.encode("12345"), "ADMIN")
//			);


		for(AppUser appUser: user1) {
			if(appUser.getUsername().equals(username)) {
				
				// Remember that Spring needs roles to be in this format: "ROLE_" + userRole (i.e. "ROLE_ADMIN")
				// So, we need to set it to that format, so we can verify and compare roles (i.e. hasRole("ADMIN")).
				List<GrantedAuthority> grantedAuthorities = AuthorityUtils
		                	.commaSeparatedStringToAuthorityList("ROLE_" + appUser.getRole());
				
				// The "User" class is provided by Spring and represents a model class for user to be returned by UserDetailsService
				// And used by auth manager to verify and check user authentication.
				return new User(appUser.getUsername(), appUser.getPassword(), grantedAuthorities);
			}
		}
		
		// If user not found. Throw this exception.
		throw new UsernameNotFoundException("Username: " + username + " not found");
	}
	
	// A (temporary) class represent the user saved in the database.
	private static class AppUser {
		private Integer id;
	    private String username, password;
	    private String role;
	    
	    public AppUser() {
	    	
	    	
	    }
		public AppUser(Integer id, String username, String password, String role) {
	    	this.id = id;
	    	this.username = username;
	    	this.password = password;
	    	this.role = role;
	    }

		public Integer getId() {
			return id;
		}

		public void setId(Object object) {
			this.id = (Integer) object;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(Object object) {
			this.username = (String) object;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(Object object) {
			this.password = (String) object;
		}
	    public String getRole() {
			return role;
		}

		public void setRole(Object object) {
			this.role = (String) object;
		}
	}
}