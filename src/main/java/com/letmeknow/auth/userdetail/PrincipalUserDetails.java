package com.letmeknow.auth.userdetail;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import com.letmeknow.entity.member.Member;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
public class PrincipalUserDetails implements UserDetails, OAuth2User {

    private Map<String, Object> attributes;

    private Member member;

    //일반 로그인 생성자
    public PrincipalUserDetails(Member member) {
        this.member = member;
    }

//    //OAuth 로그인 생성자
//    public PrincipalUserDetails(Member member, Map<String, Object> attributes) {
//        this.member = member;
//        this.attributes = attributes;
//    }

    @Override
    public String getUsername() {
        return this.member.getEmail();
    }

    @Override
    public String getPassword() {
        return this.member.getPassword();
    }

    @Override
    public String getName() {
        return this.member.getName();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes = new HashMap<>();
    }

    //해당 User의 권한을 리턴하는 곳
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add((GrantedAuthority) () -> member.getRole().toString());

        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
