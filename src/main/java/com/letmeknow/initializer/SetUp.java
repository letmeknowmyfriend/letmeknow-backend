package com.letmeknow.initializer;

import com.letmeknow.auth.service.AuthService;
import com.letmeknow.dto.member.MemberCreationDto;
import com.letmeknow.entity.Board;
import com.letmeknow.entity.College;
import com.letmeknow.entity.School;
import com.letmeknow.entity.member.Member;
import com.letmeknow.enumstorage.SpringProfile;
import com.letmeknow.exception.member.MemberSignUpValidationException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.form.auth.MemberSignUpForm;
import com.letmeknow.repository.board.BoardRepository;
import com.letmeknow.repository.college.CollegeRepository;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.repository.school.SchoolRepository;
import com.letmeknow.service.SubscriptionService;
import com.letmeknow.service.member.MemberService;
import com.letmeknow.service.member.TemporaryMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SetUp {
    private final MemberService memberService;
    private final TemporaryMemberService temporaryMemberService;
    private final SubscriptionService subscriptionService;
    private final AuthService authService;

    private final SchoolRepository schoolRepository;
    private final CollegeRepository collegeRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Transactional
    @EventListener(ApplicationReadyEvent.class) // Application이 실행되고 나서 이 메소드를 실행한다.
    public void setUp() throws MemberSignUpValidationException, MessagingException, UnsupportedEncodingException {
        Optional<School> 건국대학교 = schoolRepository.findBySchoolName("건국대학교");

        if (건국대학교.isEmpty()) {
            // 건국대학교
            School school = School.builder()
                .schoolName("건국대학교")
                .branchName("서울캠퍼스")
                .build();

            schoolRepository.save(school);

            College college = College.builder()
                .collegeName("건축대학")
                .school(school)
                .build();

            collegeRepository.save(college);

            Board 일반공지 = Board.builder()
                .boardName("일반공지")
                .boardCrawlingUrl("https://caku.konkuk.ac.kr/noticeList.do?siteId=CAKU&boardSeq=700&menuSeq=5168&searchBy=&searchValue=&categorySeq=0&curBoardDispType=LIST&curPage=60&pageNum=1")
                .boardViewUrl("https://caku.konkuk.ac.kr/noticeView.do?siteId=CAKU&boardSeq=700&menuSeq=5168&searchBy=&searchValue=&categorySeq=0&curBoardDispType=LIST&curPage=12&pageNum=1&seq=")
                .isThereNotice(true)
                .college(college)
                .build();

            Board 취업_장학 = Board.builder()
                .boardName("취업/장학")
                .boardCrawlingUrl("https://caku.konkuk.ac.kr/noticeList.do?siteId=CAKU&boardSeq=701&menuSeq=5170&searchBy=&searchValue=&categorySeq=0&curBoardDispType=LIST&curPage=60&pageNum=1")
                .boardViewUrl("https://caku.konkuk.ac.kr/noticeView.do?siteId=CAKU&boardSeq=701&menuSeq=5170&searchBy=&searchValue=&categorySeq=0&curBoardDispType=LIST&curPage=12&pageNum=1&seq=")
                .isThereNotice(false)
                .college(college)
                .build();

            Board 공모_특강 = Board.builder()
                .boardName("공모/특강")
                .boardCrawlingUrl("https://caku.konkuk.ac.kr/noticeList.do?siteId=CAKU&boardSeq=702&menuSeq=5172&searchBy=&searchValue=&categorySeq=0&curBoardDispType=LIST&curPage=60&pageNum=1")
                .boardViewUrl("https://caku.konkuk.ac.kr/noticeView.do?siteId=CAKU&boardSeq=702&menuSeq=5172&searchBy=&searchValue=&categorySeq=0&curBoardDispType=LIST&curPage=12&pageNum=1&seq=")
                .isThereNotice(false)
                .college(college)
                .build();

            Board 학사_규정 = Board.builder()
                .boardName("학사규정")
                .boardCrawlingUrl("https://caku.konkuk.ac.kr/noticeList.do?siteId=CAKU&boardSeq=703&menuSeq=5174&searchBy=&searchValue=&categorySeq=0&curBoardDispType=LIST&curPage=60&pageNum=1")
                .boardViewUrl("https://caku.konkuk.ac.kr/noticeView.do?siteId=CAKU&boardSeq=703&menuSeq=5174&searchBy=&searchValue=&categorySeq=0&curBoardDispType=LIST&curPage=12&pageNum=1&seq=")
                .isThereNotice(true)
                .college(college)
                .build();

            boardRepository.saveAll(
                List.of(일반공지, 취업_장학, 공모_특강, 학사_규정)
            );
        }

        if (activeProfile.equals(SpringProfile.LOCAL.getProfile())) {
            //        // 회원1 정보
            //        String email1 = "member1@gmail.com";
            //        String deviceToken1 = "dsYV8Lydb0J8pxS1Yc-d6A:APA91bFgSi1iFSYHPMODjDU38eThiLhInQ8y2f5P4z-oE4AWqbpRHjpzzqdgEyLF8LNBMyh71nCggRl2kHXeMjGWuLkFNK6pkHpF2aqKkiFg3gN3wutICV-H2l0Ru_5YJovjHJTwNHZE";
            //
            //        // 회원2 정보
            //        String email2 = "member2@gmail.com";
            //        String deviceToken2_1 = "fJ19mGnnb0FfgMjV4Wk0nO:APA91bHMzk5rl0SdoELNPft7Ck-ZoPxRuhb1f8mf2rMDJ37alQjumGNd7n8OX7olYdlH1BTX9tzOWZyA2lSBXrqLhOLNvpc7YAnpR05xbHa3DEC0V7Ln9WxdE8OXgWodZopKZyQRt-b5";
            //        String deviceToken2_2 = "fP2LzDpglEdXk30FR8ONhL:APA91bFdvNLeVU6h4AEnw3KWCgVoWl2QkvgSA92UOeh3KAethvi9_lat4OfN-Y2rtln2k_cna8Quf68LrZ98pWdCwdCEi2zoT9_T5w_XMHk9i_7GYiuNuBFkL83q9PqbFrNEBgRJVUjd";
            //        String deviceToken2_3 = "fIlvadP0yETqrAujDDr85B:APA91bFrvIP-VWZhcgWKmdwKHa7Lbsru6KxkuGq_lilKnoLsvd2ek83d_rxsxgJMsS8nAAARUQEqpgtHaCHsbjR1t1pErt4uH-9vKEIo2fBXxd4hd-AclgKN_dLANYSNwDUb1DYt3eXR";
            //        String deviceToken2_4 = "ctmYaAeagU3ooO3y0-Rnix:APA91bF0BKwGCHWtV5oosZTMOko93lwUXg8lNmINwPF1ys3uvPVq0SiJehD9ziNLN4il8V_Y5ABqYKP5DmJT5FfmYjZJLRsOs7OAeZ4oIt3z9hhmD0Hrhk8F-_gMxkA7t933w1DMHhaO";
            //
            //        // 회원3 정보
            //        String email3 = "member3@gmail.com";
            //        String deviceToken3_1 = "d6PLS9lcUEAGgjXdtQA3js:APA91bGioDKYiYfq0F-I1ajXPSN6YVELs8JmtXyUC7vBPYNrCYNDaWUQpJuVNkuojog4zrmIYd56Lj_cztzxVCUqwYfJytwka-M2xihIH3_C2DS2qMu4TwuIjH22u5UkUNlm-_Luu7Dt";
            //        String deviceToken3_2 = "eJFK71cym0CAt1_w5Y4kJn:APA91bEG1j3N7thfyUxuuUlTuepCY8GCjrm8V9fJZHFz-QWbjQDL7LTAEGgS1CiNt3dlS_xQo2zLISxwDDRW71dCQ6PgSYHGhjrHVyFNhKQB1QoHPdsex3_BCZFtmeiSfZCrZPsDgtkG";
            //        String deviceToken3_3 = "ciJz_aPpW0Lbr6Xu5xAFih:APA91bGWQWmYS8mDWKZ_-papyRNT2fctYUh7Ts9Vkjz0UGr1roT1GzsExttlkzEzgdMEcIDD4zMqFNwrnHCgy7lsVl5QE3UE9-epM2qNHYIdEUEZudg2rP4OAMotznBiJe2tTdTZQc5M";
            //
            //        // 회원4 정보
            //        String email4 = "member4@gmail.com";
            //
            //        // 회원5 정보
            //        String email5 = "member5@gmail.com";
            //
            // ChaCha 정보
            String chachaEmail = "cha3088@gmail.com";
            //
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //        // 회원1 생성
            //        memberRepository.save(Member.builder()
            //            .name("member1")
            //            .email(email1)
            //            .password(passwordEncoder.encode("password"))
            //            .city("city")
            //            .street("street")
            //            .zipcode("zipcode")
            //            .build());
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //        // 임시 회원1 생성
            //        temporaryMemberService.joinTemporaryMember(MemberSignUpForm.builder()
            //            .name("tempMem1")
            //            .email("temporaryMember1@gmail.com")
            //            .password("passWord1234!")
            //            .passwordAgain("passWord1234!")
            //            .city("city")
            //            .street("street")
            //            .zipcode("zipcode")
            //            .build());
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //        // 회원2 생성
            //        memberRepository.save(Member.builder()
            //            .name("member2")
            //            .email(email2)
            //            .password(passwordEncoder.encode("password"))
            //            .city("city")
            //            .street("street")
            //            .zipcode("zipcode")
            //            .build());
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //        // 회원3 생성
            //        memberRepository.save(Member.builder()
            //            .name("member3")
            //            .email(email3)
            //            .password(passwordEncoder.encode("password"))
            //            .city("city")
            //            .street("street")
            //            .zipcode("zipcode")
            //            .build());
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //        // 회원4 생성
            //        memberRepository.save(Member.builder()
            //            .name("member4")
            //            .email(email4)
            //            .password(passwordEncoder.encode("password"))
            //            .city("city")
            //            .street("street")
            //            .zipcode("zipcode")
            //            .build());
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //        // 회원5 생성
            //        memberRepository.save(Member.builder()
            //            .name("member5")
            //            .email(email5)
            //            .password(passwordEncoder.encode("password"))
            //            .city("city")
            //            .street("street")
            //            .zipcode("zipcode")
            //            .build());
            //
            //        Member member5 = memberRepository.findNotDeletedByEmail(email5)
            //            .orElseThrow(() -> new NoSuchMemberException("회원이 없습니다."));
            //
            //        member5.deleteMember();
            //
            //        memberRepository.save(member5);
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // ChaCha 생성
            memberRepository.save(Member.builder()
                .name("Cha Cha")
                .email(chachaEmail)
                .password(passwordEncoder.encode("password"))
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //        // 로그인
            //        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
            //        authService.whenMemberSignIn_IssueJwts_StoreDeviceToken_SubscribeToAllTopics(email1, deviceToken1);
            //
            //        // 알림 동의
            //        memberService.consentToNotification(email1);
            //
            //        // 게시판 구독
            //        subscriptionService.subscribeToTopic(email1, "2");
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //        // 로그인
            //        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
            //        authService.whenMemberSignIn_IssueJwts_StoreDeviceToken_SubscribeToAllTopics(email2, deviceToken2_1);
            //
            //        // 로그인
            //        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
            //        authService.whenMemberSignIn_IssueJwts_StoreDeviceToken_SubscribeToAllTopics(email2, deviceToken2_2);
            //
            //        // 로그인
            //        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
            //        authService.whenMemberSignIn_IssueJwts_StoreDeviceToken_SubscribeToAllTopics(email2, deviceToken2_3);
            //
            //        // 로그인
            //        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
            //        authService.whenMemberSignIn_IssueJwts_StoreDeviceToken_SubscribeToAllTopics(email2, deviceToken2_4);
            //
            //        // 알림 동의
            //        memberService.consentToNotification(email2);
            //
            //        // 게시판 구독
            //        subscriptionService.subscribeToTopic(email2, "1");
            //        subscriptionService.subscribeToTopic(email2, "2");
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //        // 로그인
            //        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
            //        authService.whenMemberSignIn_IssueJwts_StoreDeviceToken_SubscribeToAllTopics(email3, deviceToken3_1);
            //
            //        // 로그인
            //        authService.whenMemberSignIn_IssueJwts_StoreDeviceToken_SubscribeToAllTopics(email3, deviceToken3_2);
            //
            //        // 로그인
            //        authService.whenMemberSignIn_IssueJwts_StoreDeviceToken_SubscribeToAllTopics(email3, deviceToken3_3);
            //
            //        // 알림 동의
            //        memberService.consentToNotification(email3);
            //
            //        // 게시판 구독
            //        subscriptionService.subscribeToTopic(email3, "1");
            //        subscriptionService.subscribeToTopic(email3, "2");
            //
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //
            //        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //        // 알림 동의
            //        memberService.consentToNotification(chachaEmail);
            //
            //        // 게시판 구독
            //        subscriptionService.subscribeToTopic(chachaEmail, "2");
            //        subscriptionService.subscribeToTopic(chachaEmail, "4");
        }
    }
}
