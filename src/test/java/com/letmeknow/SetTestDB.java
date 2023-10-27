//package com.letmeknow;
//
//import com.letmeknow.domain.Board;
//import com.letmeknow.domain.member.Member;
//import com.letmeknow.dto.member.MemberCreationDto;
//import com.letmeknow.exception.auth.jwt.NoSuchDeviceTokenException;
//import com.letmeknow.exception.member.NoSuchMemberException;
//import com.letmeknow.exception.notification.NotificationException;
//import com.letmeknow.repository.member.MemberRepository;
//import com.letmeknow.service.BoardService;
//import com.letmeknow.service.auth.jwt.JwtService;
//import com.letmeknow.service.member.MemberService;
//import com.letmeknow.service.member.TemporaryMemberService;
//import com.letmeknow.service.notification.NotificationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import javax.mail.MessagingException;
//import java.io.UnsupportedEncodingException;
//
//@SpringBootTest
//@Component
//public class SetTestDB {
//    @Autowired
//    private MemberRepository memberRepository;
//    @Autowired
//    private MemberService memberService;
//    @Autowired
//    private TemporaryMemberService temporaryMemberService;
//    @Autowired
//    private JwtService jwtService;
//    @Autowired
//    private NotificationService notificationService;
//    @Autowired
//    private BoardService boardService;
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @PostConstruct
//    public void setUp() throws MessagingException, UnsupportedEncodingException, NoSuchMemberException, NoSuchDeviceTokenException, NotificationException {
//        // 게시판 생성
//        Board newBoard1 = Board.builder()
//                        .boardName("일반공지")
//                        .boardSeq(700l)
//                        .menuSeq(5168l)
//                        .isThereNotice(true)
//                        .build();
//        boardService.save(newBoard1);
//        Board newBoard2 = Board.builder()
//            .boardName("취업/장학")
//            .boardSeq(701l)
//            .menuSeq(5170l)
//            .isThereNotice(false)
//            .build();
//        boardService.save(newBoard2);
//
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // 회원1 정보
//        String email1 = "member1@gmail.com";
//        String deviceToken1 = "deviceToken1";
//
//        // 회원1 생성
//        memberService.joinMember(MemberCreationDto.builder()
//                .name("member1")
//                .email(email1)
//                .password("password")
//                .city("city")
//                .street("street")
//                .zipcode("zipcode")
//            .build());
//
//        // 로그인
//        jwtService.issueTokens(email1, deviceToken1);
//        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
//        notificationService.whenMemberLogIn_AddFCMSubscription(email1, deviceToken1);
//
//        // 게시판 구독
//        notificationService.subscribe(email1, 2l);
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // 임시 회원1 생성
//        temporaryMemberService.joinTemporaryMember(MemberCreationDto.builder()
//                .name("temporaryMember1")
//                .email("temporaryMember1@gmail.com")
//                .password("password")
//                .city("city")
//                .street("street")
//                .zipcode("zipcode")
//            .build());
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // 회원2 정보
//        String email2 = "member2@gmail.com";
//        String deviceToken2_1 = "deviceToken2_1";
//        String deviceToken2_2 = "deviceToken2_2";
//        String deviceToken2_3 = "deviceToken2_3";
//        String deviceToken2_4 = "deviceToken2_4";
//
//        // 회원2 생성
//        memberService.joinMember(MemberCreationDto.builder()
//                .name("member2")
//                .email(email2)
//                .password("password")
//                .city("city")
//                .street("street")
//                .zipcode("zipcode")
//            .build());
//
//        // 로그인
//        jwtService.issueTokens(email2, deviceToken2_1);
//        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
//        notificationService.whenMemberLogIn_AddFCMSubscription(email2, deviceToken2_1);
//
//        // 로그인
//        jwtService.issueTokens(email2, deviceToken2_2);
//        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
//        notificationService.whenMemberLogIn_AddFCMSubscription(email2, deviceToken2_2);
//
//        // 로그인
//        jwtService.issueTokens(email2, deviceToken2_3);
//        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
//        notificationService.whenMemberLogIn_AddFCMSubscription(email2, deviceToken2_3);
//
//        // 로그인
//        jwtService.issueTokens(email2, deviceToken2_4);
//        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
//        notificationService.whenMemberLogIn_AddFCMSubscription(email2, deviceToken2_4);
//
//        // 게시판 구독
//        notificationService.subscribe(email2, 1l);
//        notificationService.subscribe(email2, 2l);
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // 회원3 정보
//        String email3 = "member3@gmail.com";
//        String deviceToken3_1 = "deviceToken3_1";
//        String deviceToken3_2 = "deviceToken3_2";
//        String deviceToken3_3 = "deviceToken3_3";
//
//        // 회원3 생성
//        memberService.joinMember(MemberCreationDto.builder()
//            .name("member3")
//            .email(email3)
//            .password("password")
//            .city("city")
//            .street("street")
//            .zipcode("zipcode")
//            .build());
//
//        // 로그인
//        jwtService.issueTokens(email3, deviceToken3_1);
//        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
//        notificationService.whenMemberLogIn_AddFCMSubscription(email3, deviceToken3_1);
//
//        // 로그인
//        jwtService.issueTokens(email3, deviceToken3_2);
//        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
//        notificationService.whenMemberLogIn_AddFCMSubscription(email3, deviceToken3_2);
//
//        // 로그인
//        jwtService.issueTokens(email3, deviceToken3_3);
//        // 회원의 기기 토큰을 찾고, FCM 구독을 추가한다.
//        notificationService.whenMemberLogIn_AddFCMSubscription(email3, deviceToken3_3);
//
//        // 게시판 구독
//        notificationService.subscribe(email3, 1l);
//        notificationService.subscribe(email3, 2l);
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // 회원4 정보
//        String email4 = "member4@gmail.com";
//
//        // 회원4 생성
//        memberService.joinMember(MemberCreationDto.builder()
//            .name("member4")
//            .email(email4)
//            .password("password")
//            .city("city")
//            .street("street")
//            .zipcode("zipcode")
//            .build());
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//        // 회원5 정보
//        String email5 = "member5@gmail.com";
//
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
//    }
//}
