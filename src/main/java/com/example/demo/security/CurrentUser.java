package com.example.demo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    
	public int userId(Authentication auth) {
		if(auth instanceof JwtAuthenticationToken jwtAuth) {
			String sub = jwtAuth.getToken().getSubject();
			return Integer.parseInt(sub);
		} else {
			throw new IllegalStateException("Missing JWT authentication");
		}
	}
	
	public boolean isAdmin(Authentication auth) {
		return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	}
}
