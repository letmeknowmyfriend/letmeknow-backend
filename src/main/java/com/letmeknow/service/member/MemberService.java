package com.letmeknow.service.member;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.letmeknow.auth.entity.DeviceToken;
import com.letmeknow.auth.entity.RefreshToken;
import com.letmeknow.auth.repository.devicetoken.DeviceTokenRepository;
import com.letmeknow.auth.repository.refreshtoken.RefreshTokenRepository;
import com.letmeknow.auth.service.JwtService;
import com.letmeknow.dto.member.MemberFindDto;
import com.letmeknow.dto.member.MemberPasswordUpdateDto;
import com.letmeknow.entity.member.Member;
import com.letmeknow.entity.notification.Subscription;
import com.letmeknow.enumstorage.errormessage.member.MemberErrorMessage;
import com.letmeknow.exception.auth.jwt.NoSuchDeviceTokenException;
import com.letmeknow.exception.member.MemberSignUpValidationException;
import com.letmeknow.exception.member.NewPasswordNotMatchException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.exception.member.PasswordIncorrectException;
import com.letmeknow.form.MemberAddressUpdateForm;
import com.letmeknow.message.messages.EmailMessage;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.repository.notification.SubscriptionRepository;
import com.letmeknow.service.DeviceTokenService;
import com.letmeknow.service.SubscriptionService;
import com.letmeknow.service.email.EmailService;
import com.letmeknow.util.CodeGenerator;
import com.letmeknow.util.Validator;
import com.letmeknow.util.email.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

import static com.letmeknow.auth.messages.MemberMessages.*;
import static com.letmeknow.auth.service.JwtService.ACCESS_TOKEN_HEADER;
import static com.letmeknow.auth.service.JwtService.REFRESH_TOKEN_HEADER;
import static com.letmeknow.message.messages.MemberMessage.CONSENT_TO_PUSH_NOTIFICATION;
import static com.letmeknow.message.messages.Messages.*;
import static com.letmeknow.message.messages.NotificationMessages.DEVICE_TOKEN;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final SubscriptionService subscriptionService;
    private final DeviceTokenService deviceTokenService;

    private final MemberRepository memberRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SubscriptionRepository subscriptionRepository;

    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final CodeGenerator codeGenerator;
    private final Validator validator;

    public MemberFindDto findMemberFindDtoById(long memberId) throws NoSuchMemberException {
        return memberRepository.findNotDeletedById(memberId)
                //회원이 없으면 예외 발생
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()))
                //Dto로 변환
                .toMemberFindDto();
    }

    public MemberFindDto findMemberFindDtoByEmail(String email) throws NoSuchMemberException {
        return memberRepository.findNotDeletedByEmail(email)
                //해당하는 이메일을 가진 회원이 없으면, 예외 발생
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage()))
                //Dto로 변환
                .toMemberFindDto();
    }

    public long verifyPasswordVerificationCode(String passwordVerificationCode) throws NoSuchMemberException {
        Member member = memberRepository.findNotDeletedByPasswordVerificationCode(passwordVerificationCode)
                //해당하는 비밀번호 변경 코드를 가진 회원이 없으면, 예외 발생
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_PASSWORD_VERIFICATION_CODE.getMessage()));

        return member.getId();
    }

    /**
     * 비밀번호 변경, 상태 변경, verificationCode로 인증, verificationCode 삭제
     * @param passwordVerificationCode
     * @param newPassword
     * @throws NoSuchMemberException
     */
    @Transactional
    public void changePassword(String passwordVerificationCode, String newPassword) throws NoSuchMemberException {
        Member member = memberRepository.findNotDeletedByPasswordVerificationCode(passwordVerificationCode)
                //해당하는 비밀번호 변경 코드를 가진 회원이 없으면, 예외 발생
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_PASSWORD_VERIFICATION_CODE.getMessage()));

        //verificationCode로 인증
        if (!member.getPasswordVerificationCode().equals(passwordVerificationCode)) {
            throw new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_PASSWORD_VERIFICATION_CODE.getMessage());
        }

        //비밀번호 변경
        member.changePassword(passwordEncoder.encode(newPassword));

        //상태 변경
        member.unlock();

        //logInAttempt 초기화
        member.resetLogInAttempt();

        //verificationCode 삭제
        member.deletePasswordVerificationCode();

        //저장
        memberRepository.save(member);
    }

    public List<MemberFindDto> findAllMemberFindDto() {
        return memberRepository.findAll().stream()
                .map(Member::toMemberFindDto)
                .collect(Collectors.toList());
    }

    /**
     * 회원 주소 수정
     * @param memberAddressUpdateForm
     * @return 회원 id
     * @throws NoSuchMemberException
     */
    @Transactional
    public void updateMemberAddress(@Valid MemberAddressUpdateForm memberAddressUpdateForm, String email) throws NoSuchMemberException {
        // 업데이트하려는 회원 조회
        Member member = memberRepository.findNotDeletedByEmail(email)
            // 해당하는 이메일을 가진 회원이 없으면, 예외 발생
            .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        // 회원 주소 업데이트
        member.updateMemberAddress(memberAddressUpdateForm.getCity(), memberAddressUpdateForm.getStreet(), memberAddressUpdateForm.getZipcode());

        // 저장
        memberRepository.save(member);
    }

    @Transactional
    public void updateMemberPassword(@Valid MemberPasswordUpdateDto memberPasswordUpdateDto, String email) throws NoSuchMemberException, PasswordIncorrectException, MemberSignUpValidationException, NewPasswordNotMatchException {
        // 새 비밀번호와 새 비밀번호 확인이 일치하지 않으면, 예외 발생
        if (!memberPasswordUpdateDto.getNewPassword().equals(memberPasswordUpdateDto.getNewPasswordAgain())) {
            throw new NewPasswordNotMatchException(new StringBuffer().append(NEW_PASSWORD.getMessage()).append(NOT_EQUAL.getMessage()).toString());
        }

        // 새 비밀번호가 규칙에 맞지 않으면, 예외 발생
        validator.isValidPassword(email, memberPasswordUpdateDto.getNewPassword(), memberPasswordUpdateDto.getNewPasswordAgain());

        Member member = memberRepository.findNotDeletedByEmail(email)
            // 해당하는 이메일을 가진 회원이 없으면, 예외 발생
            .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(EMAIL.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        // 회원의 비밀번호가 일치하지 않으면, 예외 발생
        if (!passwordEncoder.matches(memberPasswordUpdateDto.getPassword(), member.getPassword())) {
            throw new PasswordIncorrectException(new StringBuffer().append(INCORRECT.getMessage()).append(PASSWORD.getMessage()).toString());
        }

        // 회원의 정보 업데이트
        member.changePassword(memberPasswordUpdateDto.getNewPassword());

        // 회원의 정보 저장
        memberRepository.save(member);
    }

    @Transactional
    public void sendPasswordChangeVerificationEmail(String email) throws UnsupportedEncodingException, MessagingException, NoSuchMemberException {
        Member member = memberRepository.findNotDeletedByEmail(email)
                // 해당하는 이메일을 가진 회원이 없으면, 예외 발생
                .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        // verificationCode 생성
        String verificationCode = codeGenerator.generateCode(20);

        // PasswordVerificationCode 업데이트
        member.updatePasswordVerificationCode(verificationCode);

        // 비밀번호 재설정 이메일 발송
        emailService.sendMail(Email.builder()
                .subject(EmailMessage.CHANGE_PASSWORD_EMAIL_SUBJECT.getMessage())
                .receiver(email)
                .message(EmailMessage.CHANGE_PASSWORD_EMAIL_MESSAGE.getMessage() +
                        EmailMessage.CHANGE_PASSWORD_EMAIL_LINK1.getMessage() +
                        URLEncoder.encode(verificationCode, "UTF-8") +
                        EmailMessage.CHANGE_PASSWORD_EMAIL_LINK2.getMessage())
                .build());
    }

    // 알림 수신 동의
    @Transactional
    public void consentToNotification(String email, String deviceToken, HttpServletResponse response) throws NoSuchDeviceTokenException, NoSuchMemberException {
        // ToDo: 테스트 할 때만 끔 // 추출한 deviceToken이 유효한지 확인한다.
//        deviceTokenService.validateAndExtractDeviceToken(deviceToken);

        Member member = memberRepository.findNotDeletedByEmailWithSubscriptionAndDeviceToken(email)
                // 해당하는 이메일을 가진 회원이 없으면, 예외 발생
                .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        // 이미 알림 수신 동의를 한 회원이면, 예외 발생
        if (member.getConsentToReceivePushNotification()) {
            throw new IllegalArgumentException(new StringBuffer().append(ALREADY.getMessage()).append(CONSENT_TO_PUSH_NOTIFICATION.getMessage()).toString());
        }

        // 회원의 DeviceToken이 DB에 있는지 확인하고
        if (member.getDeviceTokens().stream().noneMatch(deviceToken1 -> deviceToken1.getDeviceToken().equals(deviceToken))) {
            // ToDo: null 해도 되는지 테스트
            // 없으면, 꺼져, 다시 로그인 해라
            response.setHeader(ACCESS_TOKEN_HEADER, null);
            response.setHeader(REFRESH_TOKEN_HEADER, null);
            throw new NoSuchDeviceTokenException(new StringBuffer().append(SUCH.getMessage()).append(DEVICE_TOKEN.getMessage()).append(NOT_EXISTS.getMessage()).toString());
        }

        // 알림 수신 동의
        member.consentToReceivePushNotification();

        // 구독
        subscriptionService.subscribeToAllTopics(deviceToken, member);

        // 저장
        memberRepository.save(member);
    }

    // 알림 수신 거부
    @Transactional
    public void refuseToNotification(String email, String deviceToken, HttpServletResponse response) throws NoSuchDeviceTokenException, NoSuchMemberException {
        // ToDo: 테스트 할 때만 끔 // 추출한 deviceToken이 유효한지 확인한다.
//        deviceTokenService.validateAndExtractDeviceToken(deviceToken);

        Member member = memberRepository.findNotDeletedByEmailWithSubscriptionAndDeviceToken(email)
                // 해당하는 이메일을 가진 회원이 없으면, 예외 발생
                .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        // 이미 알림 수신 동의 하지 않은 회원이면, 예외 발생
        if (!member.getConsentToReceivePushNotification()) {
            throw new IllegalArgumentException(new StringBuffer().append(ALREADY.getMessage()).append(CONSENT_TO_PUSH_NOTIFICATION.getMessage()).append(DID_NOT.getMessage()).toString());
        }

        // 회원의 DeviceToken이 DB에 있는지 확인하고
        if (member.getDeviceTokens().stream().noneMatch(deviceToken1 -> deviceToken1.getDeviceToken().equals(deviceToken))) {
            // ToDo: null 해도 되는지 테스트
            // 없으면, 꺼져, 다시 로그인 해라
            response.setHeader(ACCESS_TOKEN_HEADER, null);
            response.setHeader(REFRESH_TOKEN_HEADER, null);
            throw new NoSuchDeviceTokenException(new StringBuffer().append(SUCH.getMessage()).append(DEVICE_TOKEN.getMessage()).append(NOT_EXISTS.getMessage()).toString());
        }

        // 알림 수신 거부
        member.refuseToReceivePushNotification();

        // 구독 취소
        subscriptionService.unsubscribeFromAllTopics(deviceToken, member);

        // 저장
        memberRepository.save(member);
    }


    // 회원 탈퇴
    @Transactional
    public void deleteMember(String email, HttpServletResponse response) throws NoSuchMemberException {
        Member member = memberRepository.findNotDeletedByEmailWithRefreshTokenAndSubscriptionAndDeviceToken(email)
                // 해당하는 이메일을 가진 회원이 없으면, 예외 발생
                .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        List<String> deviceTokens = member.getDeviceTokens().stream().map(DeviceToken::getDeviceToken).collect(Collectors.toList());

        // 구독 삭제
        for (Subscription subscription : member.getSubscriptions()) {
            // 구독 해제
            try {
                String boardId = String.valueOf(subscription.getBoard().getId());
                FirebaseMessaging.getInstance().unsubscribeFromTopic(deviceTokens, boardId);
            }
            // 구독 해제 실패해도, 계속 진행
            catch (FirebaseMessagingException e) {}

            subscriptionRepository.delete(subscription);
        }

        // RefreshToken 삭제
        for (RefreshToken refreshToken : member.getRefreshTokens()) {
            refreshTokenRepository.delete(refreshToken);
        }

        // 회원 삭제
        memberRepository.delete(member);

        // AccessToken 삭제
        response.setHeader(JwtService.ACCESS_TOKEN_HEADER, "");
    }

    private Member findById(long memberId) throws NoSuchMemberException {
        return memberRepository.findNotDeletedById(memberId)
                //해당하는 Id를 가진 회원이 없으면, 예외 발생
                .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));
    }
}
