package com.org.group.config;

import com.org.group.repository.AnalyzerRepository;
import com.org.group.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AnalyzerRepository analyzerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .map(user -> (UserDetails) user)
                .orElseGet(() ->
                        analyzerRepository.findByEmail(username)
                                .map(analyzer -> (UserDetails) analyzer)
                                .orElseThrow(() -> new UsernameNotFoundException("User or Analyzer not found"))
                );
    }
}

