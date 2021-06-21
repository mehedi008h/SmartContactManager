package com.contact.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.contact.demo.model.User;
import com.contact.demo.repository.UserRepository;

public class UserDetailsServiceImpi implements UserDetailsService{
	
	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// fetching user from database
		
		User user = userRepository.getUserByUserName(username);
		if(user == null)
		{
			throw new UsernameNotFoundException("Could not found user!!");
		}
		
		CustomUserDetails customerUserDetails = new CustomUserDetails(user);
		return customerUserDetails;
	}

}
