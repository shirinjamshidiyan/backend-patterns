package com.shirin.twofactorauthentication.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.ott.JdbcOneTimeTokenService;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.config.annotation.authorization.EnableMultiFactorAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@AllArgsConstructor
@EnableMultiFactorAuthentication(authorities = {
        FactorGrantedAuthority.PASSWORD_AUTHORITY,
        FactorGrantedAuthority.OTT_AUTHORITY
})
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public OneTimeTokenService oneTimeTokenService(JdbcTemplate jdbcTemplate)
    {
        return new JdbcOneTimeTokenService(jdbcTemplate);
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder)
    {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }



   @Bean
   public SecurityFilterChain securityFilterChain(
           HttpSecurity http,
           OneTimeTokenService tokenService,
           CustomOTTGenerationSuccessHandler ottHandler,
           RoleBasedAuthenticationSuccessHandler successHandler) throws Exception
    {
      return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/",
                                "/showLoginForm",
                                "/ott/request",
                                "/verify-ott"
                        )
                        .permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/manager/**").hasRole("MANAGER")
                        .requestMatchers("/user/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .formLogin(customize -> customize
                              .loginPage("/showLoginForm")
                              .loginProcessingUrl("/loginProcess")
                               .failureUrl("/showLoginForm?error")
                              .permitAll()
              )
              .oneTimeTokenLogin(ott -> ott

                      // Password OK + OTT missing
                      .loginPage("/ott/request")

                      // POST entered token
                      .loginProcessingUrl("/login/ott")

                      // We provide our own verify page
                      .showDefaultSubmitPage(false)

                      // Wrong / expired OTT
                      .failureUrl("/verify-ott?error")

                      .tokenService(tokenService)
                      .tokenGenerationSuccessHandler(ottHandler)
                      .successHandler(successHandler)
                      .permitAll()

              )
              .logout(logout -> logout
                      .logoutSuccessUrl("/showLoginForm?logout")
                      .permitAll()
              )


              .build();
    }


}
