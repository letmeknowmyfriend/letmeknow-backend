package com.letmeknow.auth.service;

import com.letmeknow.auth.userdetail.PrincipalUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.letmeknow.entity.member.Member;
import com.letmeknow.auth.entity.OAuth2;
import com.letmeknow.enumstorage.errormessage.member.MemberErrorMessage;
import com.letmeknow.enumstorage.errormessage.auth.oauth2.OAuth2ErrorMessage;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.exception.auth.oauth2.NoSuchOAuth2Exception;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.repository.oauth2.OAuth2Repository;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrincipalOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;
    private final OAuth2Repository oAuth2Repository;

    //이런 요청이 들어왔는데, 얘 혹시 회원이야?
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        //OAuth2User의 모든 attribute를 출력
        oAuth2User.getAttributes().keySet().forEach(key -> {
            System.out.println(key + " : " + oAuth2User.getAttribute(key));
        });

        //providerId는 각각의 provider에서 제공하는 유저 고유 Id
        String providerId;
        //registrationId는 우리가 등록한 provider의 이름
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes;

        //카카오는 kakao_account 안에 있음
        if (registrationId.equals("kakao")) {
            attributes = Optional.ofNullable((Map<String, Object>) oAuth2User.getAttributes().get("kakao_account"))
                    .orElseThrow(() -> new OAuth2AuthenticationException(OAuth2ErrorMessage.NO_ATTRIBUTE.getMessage()));
            providerId = Optional.ofNullable((String) attributes.get("id"))
                    .orElseThrow(() -> new OAuth2AuthenticationException(OAuth2ErrorMessage.NO_PROVIDER_ID.getMessage()));
        }
        //네이버는 response 안에 있음
        else if (registrationId.equals("naver")) {
            attributes = Optional.ofNullable((Map<String, Object>) oAuth2User.getAttributes().get("response"))
                    .orElseThrow(() -> new OAuth2AuthenticationException(OAuth2ErrorMessage.NO_ATTRIBUTE.getMessage()));
            providerId = Optional.ofNullable((String) attributes.get("id"))
                    .orElseThrow(() -> new OAuth2AuthenticationException(OAuth2ErrorMessage.NO_PROVIDER_ID.getMessage()));

        }
        //구글은 바로 있음
        else if (registrationId.equals("google")) {
            attributes = oAuth2User.getAttributes();
            providerId = Optional.ofNullable((String) attributes.get("sub"))
                    .orElseThrow(() -> new OAuth2AuthenticationException(OAuth2ErrorMessage.NO_PROVIDER_ID.getMessage()));

        //어느 것도 아니면,
        } else {
            throw new OAuth2AuthenticationException(OAuth2ErrorMessage.NO_SUCH_PROVIDER.getMessage());
        }

        String email = Optional.ofNullable((String) attributes.get("email"))
                .orElseThrow(() -> new OAuth2AuthenticationException(OAuth2ErrorMessage.NO_EMAIL.getMessage()));

        try {
            //registrationId와 providerId로 OAuth2를 찾는다.
            OAuth2 OAuth2 = oAuth2Repository.findByRegistrationIdAndProviderId(registrationId, providerId)
                    .orElseThrow(() -> new NoSuchOAuth2Exception(OAuth2ErrorMessage.NO_SUCH_OAUTH_2.getMessage()));

            //있으면, 연관된 Member를 찾는다.
            Member member = memberRepository.findNotDeletedById(OAuth2.getMember().getId())
                    .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));

            //PrincipalDetails를 리턴한다.
            return new PrincipalUserDetails(member);

        } catch (NoSuchOAuth2Exception e) {
            //OAuth2는 없지만, Member가 있을 때
            try {
                //OAuth2가 없으면, Member를 찾는다.
                Member member = memberRepository.findNotDeletedByEmail(email)
                        .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));

                // OAuth2를 생성한다.
                OAuth2 oAuth2 = OAuth2.builder()
                        .registrationId(registrationId)
                        .providerId(providerId)
                        .member(member)
                        .build();

                //Member를 저장한다.
                memberRepository.save(member);
                //OAuth2를 저장한다.
                oAuth2Repository.save(oAuth2);

                //PrincipalDetails를 리턴한다.
                return new PrincipalUserDetails(member);

            //OAuth2도 없고, Member도 없으면,
            } catch (NoSuchMemberException nsme) {
                //Member를 생성한다.
                Member member = Member.builder()
                        .email(email)
                        .build();

                // OAuth2를 생성한다.
                OAuth2 oAuth2 = OAuth2.builder()
                        .registrationId(registrationId)
                        .providerId(providerId)
                        .member(member)
                        .build();

                //Member를 저장한다.
                memberRepository.save(member);
                //OAuth2를 저장한다.
                oAuth2Repository.save(oAuth2);

                //PrincipalDetails를 리턴한다.
                return new PrincipalUserDetails(member);
            }
        }
        catch (NoSuchMemberException noSuchMemberException) {
            throw new OAuth2AuthenticationException(noSuchMemberException.getMessage());
        }
    }
}
