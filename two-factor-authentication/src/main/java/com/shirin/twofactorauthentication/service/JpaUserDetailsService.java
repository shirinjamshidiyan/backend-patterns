package com.shirin.twofactorauthentication.service;

import com.shirin.twofactorauthentication.entity.UserEntity;
import com.shirin.twofactorauthentication.entity.UserPrincipal;
import com.shirin.twofactorauthentication.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new
                        UsernameNotFoundException("User not found: " + email));

        return new UserPrincipal(user);

    }
}
