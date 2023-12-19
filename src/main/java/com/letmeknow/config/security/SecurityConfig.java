package com.letmeknow.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letmeknow.auth.filter.auth.AuthenticationProcessFilter;
import com.letmeknow.auth.filter.auth.MemberAuthenticationFilter;
import com.letmeknow.auth.handler.JwtLogoutHandler;
import com.letmeknow.auth.handler.MemberLogInFailureHandler;
import com.letmeknow.auth.handler.MemberLogInSuccessHandler;
import com.letmeknow.auth.provider.MemberAuthenticationProvider;
import com.letmeknow.auth.service.PrincipalUserDetailsService;
import com.letmeknow.enumstorage.role.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity //Spring Securty 필터가 Spring Filter Chain에 등록된다.
//@EnableGlobalMethodSecurity(securedEnabled = true) //secured 어노테이션 활성화
@RequiredArgsConstructor
public class SecurityConfig {
    private final PrincipalUserDetailsService principalUserDetailsService;
    private final MemberLogInSuccessHandler memberLogInSuccessHandler;
    private final MemberLogInFailureHandler memberLogInFailureHandler;
    private final JwtLogoutHandler jwtLogoutHandler;
    private final AuthenticationProcessFilter authenticationProcessFilter;
    private final MemberAuthenticationProvider memberAuthenticationProvider;
    private final CorsConfig corsConfig;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().configurationSource(corsConfig.corsConfigurationSource());
//            .and()
//                .addFilter(corsConfig.corsFilter());

        http
                // 기본 페이지, css, image, js 하위 폴더에 있는 자료들은 모두 접근 가능, h2-console에 접근 가능
                .authorizeHttpRequests(authorize -> authorize
//                        .antMatchers("/members/**").hasAuthority(MemberRole.ADMIN.toString())
                        .antMatchers("/","/css/**","/img/**","/js/**","/favicon.ico").permitAll()
                        .antMatchers("/api/auth/**").permitAll()
                        .antMatchers("/api/subscription/**").permitAll()
                        .antMatchers("/api/notification/**").permitAll()
                        .antMatchers("/api/school/**").permitAll()
                        .antMatchers("/api/college/**").permitAll()
                        .antMatchers("/api/board/**").permitAll()
                        .antMatchers("/api/article/**").permitAll()
                        .antMatchers("/auth/**").permitAll()
//                        .antMatchers("**/api/**").permitAll()
                        .anyRequest().authenticated()
                );

        http
                //세션
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //세션을 사용하지 않겠다.
            .and()
                .httpBasic() // http header에 username, password를 넣어서 전송하는 방법을
                .disable(); // 해제

        //Filter
        http
            .addFilterBefore(authenticationProcessFilter, LogoutFilter.class)
            .addFilterAfter(memberAuthenticationFilter(), AuthenticationProcessFilter.class);

        //로그인
        http
                .formLogin().disable()
//                .loginProcessingUrl("/auth/api/signin/v1")
//                .usernameParameter("email")
//                .passwordParameter("password")
//                .successHandler(memberLogInSuccessHandler)
//                .failureHandler(memberLogInFailureHandler)
//                .permitAll()
//            .and()
                .userDetailsService(principalUserDetailsService)
                .authenticationProvider(memberAuthenticationProvider)

                //로그아웃
                .logout(logout ->
                    logout.permitAll()
                    .logoutUrl("/api/auth/signout/v1")
                    .logoutSuccessHandler((request, response, authentication) -> {
                        response.setStatus(HttpServletResponse.SC_OK);
                    })
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .addLogoutHandler(jwtLogoutHandler)
                );

        //OAuth2 로그인
//        http
//                .oauth2Login()
//                .loginPage("/auth/login")
//                .authorizationEndpoint()
//                .baseUri("/auth/login/oauth2/authorization")
//            .and()
//                .redirectionEndpoint()
//                .baseUri("/auth/login/oauth2/code/*")
//            .and()
//                .userInfoEndpoint()
//                .userService(principalOAuth2UserService)
//            .and()
//                .successHandler(oAuth2LoginSuccessHandler) // 동의하고 계속하기를 눌렀을 때 Handler 설정(
//                .failureHandler(oAuth2LoginFailureHandler); // 소셜 로그인 실패 시 핸들러 설정

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(memberAuthenticationProvider);
    }

    @Bean
    public MemberAuthenticationFilter memberAuthenticationFilter() {
        return new MemberAuthenticationFilter(objectMapper, authenticationManager(), memberLogInSuccessHandler, memberLogInFailureHandler);
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        //이 부분에서 큰 권한 순서로 ' > ' 를 사용하여 입력해준다. 띄어쓰기도 중요하다.
        roleHierarchy.setHierarchy(MemberRole.ADMIN + " > " + MemberRole.MEMBER);

        return roleHierarchy;
    }
}
