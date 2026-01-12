package com.hcy.ai_ticket.service.userinfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hcy.ai_ticket.service.userinfo.model.rdb.User;
import com.hcy.ai_ticket.service.userinfo.model.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("找不到使用者: " + username));


		return org.springframework.security.core.userdetails.User
				.withUsername(user.getUsername())
				.password(user.getPassword()) 
				.authorities(user.getRole()) 
				.build();
	}
}
