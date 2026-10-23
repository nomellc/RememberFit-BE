package com.rememberfit.backend.global.config;

import com.rememberfit.backend.global.exception.ErrorCode;
import com.rememberfit.backend.global.security.JwtAuthenticationFilter;
import com.rememberfit.backend.global.security.JwtProperties;
import com.rememberfit.backend.global.security.SecurityErrorResponder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityErrorResponder errorResponder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 토큰 기반이고 쿠키 세션을 쓰지 않으므로 CSRF 토큰이 필요 없다.
                .csrf(AbstractHttpConfigurer::disable)
                // WebConfig 가 노출한 CorsConfigurationSource 를 사용한다.
                .cors(Customizer.withDefaults())
                // 서버에 세션을 두지 않는다. 모든 요청은 토큰만으로 판단한다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 아직 토큰이 없는 상태에서 호출해야 하는 입구들이다.
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/refresh").permitAll()
                        // 나머지 API 는 전부 인증이 필요하다. 존재하지 않는 경로도 401 로 막아
                        // 비로그인 상태에서 어떤 경로가 있는지 알 수 없게 한다.
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, exception) ->
                                errorResponder.write(request, response, ErrorCode.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, exception) ->
                                errorResponder.write(request, response, ErrorCode.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
