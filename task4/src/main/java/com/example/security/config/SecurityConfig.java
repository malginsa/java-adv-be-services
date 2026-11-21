package com.example.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
public class SecurityConfig {

    /** BCrypt generates a random salt per password and embeds it in the hash. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, ex) -> {
            boolean locked = ex instanceof LockedException
                    || (ex.getCause() instanceof LockedException);
            response.sendRedirect(request.getContextPath() + (locked ? "/login?blocked" : "/login?error"));
        };
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, AuthenticationFailureHandler failureHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/about", "/login", "/css/**", "/error").permitAll()
                .requestMatchers("/info").hasAuthority("VIEW_INFO")
                .requestMatchers("/admin", "/admin/**", "/blocked-users").hasAuthority("VIEW_ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", false)
                .failureHandler(failureHandler)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll());
        return http.build();
    }
}
