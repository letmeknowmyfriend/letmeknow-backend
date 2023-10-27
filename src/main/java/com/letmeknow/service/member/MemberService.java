package com.letmeknow.service.member;

import com.letmeknow.exception.member.InvalidPasswordException;
import com.letmeknow.exception.member.NewPasswordNotMatchException;
import com.letmeknow.exception.member.PasswordIncorrectException;
import com.letmeknow.form.MemberAddressUpdateForm;
import com.letmeknow.message.MessageMaker;
import com.letmeknow.util.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.letmeknow.domain.member.Member;
import com.letmeknow.dto.member.MemberFindDto;
import com.letmeknow.dto.member.MemberPasswordUpdateDto;
import com.letmeknow.enumstorage.errormessage.member.MemberErrorMessage;
import com.letmeknow.message.EmailMessage;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.service.email.EmailService;
import com.letmeknow.util.CodeGenerator;
import com.letmeknow.util.email.Email;
import org.springframework.validation.annotation.Validated;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

import static com.letmeknow.message.Message.*;
import static com.letmeknow.message.reason.MemberReason.*;

@Service
@Transactional(readOnly = true)
@Validated
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final MessageMaker messageMaker;
    private final CodeGenerator codeGenerator;
    private final Validator validator;

    public MemberFindDto findMemberFindDtoById(Long memberId) throws NoSuchMemberException {
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

    public Long verifyPasswordVerificationCode(String passwordVerificationCode) throws NoSuchMemberException {
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

    public boolean isEmailUsed(String email) {
        return memberRepository.findIdByEmail(email).isPresent();
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
            .orElseThrow(() -> new NoSuchMemberException(messageMaker.add(SUCH).add(EMAIL).add(MEMBER).add(NOT_EXISTS).toString()));

        // 회원 주소 업데이트
        member.updateMemberAddress(memberAddressUpdateForm.getCity(), memberAddressUpdateForm.getStreet(), memberAddressUpdateForm.getZipcode());

        // 저장
        memberRepository.save(member);
    }

    @Transactional
    public void updateMemberPassword(@Valid MemberPasswordUpdateDto memberPasswordUpdateDto, String email) throws NoSuchMemberException, PasswordIncorrectException, InvalidPasswordException, NewPasswordNotMatchException {
        // 새 비밀번호와 새 비밀번호 확인이 일치하지 않으면, 예외 발생
        if (!memberPasswordUpdateDto.getNewPassword().equals(memberPasswordUpdateDto.getNewPasswordAgain())) {
            throw new NewPasswordNotMatchException(messageMaker.add(NEW_PASSWORD).add(NOT_EQUAL).toString());
        }

        // 새 비밀번호가 규칙에 맞지 않으면, 예외 발생
        validator.isValidPassword(email, memberPasswordUpdateDto.getNewPassword());

        Member member = memberRepository.findNotDeletedByEmail(email)
            // 해당하는 이메일을 가진 회원이 없으면, 예외 발생
            .orElseThrow(() -> new NoSuchMemberException(messageMaker.add(SUCH).add(EMAIL).add(MEMBER).add(NOT_EXISTS).toString()));

        // 회원의 비밀번호가 일치하지 않으면, 예외 발생
        if (!passwordEncoder.matches(memberPasswordUpdateDto.getPassword(), member.getPassword())) {
            throw new PasswordIncorrectException(messageMaker.add(INCORRECT).add(PASSWORD).toString());
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
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage()));

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

    /**
     * 테스트용
     * @param memberId
     */
    @Transactional
    public void switchRole(Long memberId) throws NoSuchMemberException {
        Member member = memberRepository.findNotDeletedById(memberId)
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));

        member.switchRole();

        memberRepository.save(member);
    }

    private Member findById(Long memberId) throws NoSuchMemberException {
        return memberRepository.findNotDeletedById(memberId)
                //해당하는 Id를 가진 회원이 없으면, 예외 발생
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));
    }
}
