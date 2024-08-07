package com.tcc.aplicacao.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.tcc.aplicacao.services.UsuarioDetailService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private UsuarioDetailService usuarioDetailService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(request -> {
                    request.requestMatchers("/css/*", "/scripts/**", "/cadastrarDocente", "/cadastroDocente", "/img/*")
                            .permitAll();
                    request.requestMatchers(HttpMethod.POST, "/cadastroUsuario", "/cadastrarDocente",
                            "/relatorio/pdf/relatorio-docente", "/relatorio/pdf/relatorio-horas/*",
                            "/relatorio/excel/relatorio-horas/*", "/listarAjustes")
                            .hasRole("ADMIN");
                    request.requestMatchers(HttpMethod.DELETE, "/deletarDocente/{id}").hasRole("ADMIN");
                    request.requestMatchers(HttpMethod.GET, "/deletarDocente/{id}",
                            "/relatorio/pdf/relatorio-docente", "/relatorio/pdf/relatorio-horas/*",
                            "/relatorio/excel/relatorio-horas/*", "/listarAjustes", "/cadastroUsuario")
                            .hasRole("ADMIN");
                    request.anyRequest().authenticated();
                })
                .formLogin(httpFormLogin -> {
                    httpFormLogin.loginPage("/login").permitAll();
                    httpFormLogin.successHandler(authenticationSuccessHandler()).permitAll();
                })
                .logout(logout -> {
                    logout.logoutUrl("/logout");
                    logout.logoutSuccessUrl("/login?logout");
                    logout.invalidateHttpSession(true);
                    logout.deleteCookies("JSESSIONID");
                    logout.permitAll();
                })
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return usuarioDetailService;
    }
}
