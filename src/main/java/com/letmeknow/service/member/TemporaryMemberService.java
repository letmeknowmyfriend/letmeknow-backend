package com.letmeknow.service.member;

import com.letmeknow.dto.member.MemberCreationDto;
import com.letmeknow.dto.temporarymember.TemporaryMemberDto;
import com.letmeknow.entity.member.Member;
import com.letmeknow.entity.member.TemporaryMember;
import com.letmeknow.enumstorage.SpringProfile;
import com.letmeknow.enumstorage.errormessage.member.temporarymember.TemporaryMemberErrorMessage;
import com.letmeknow.exception.member.MemberSignUpValidationException;
import com.letmeknow.exception.member.temporarymember.NoSuchTemporaryMemberException;
import com.letmeknow.enumstorage.EmailEnum;
import com.letmeknow.form.auth.MemberSignUpForm;
import com.letmeknow.message.cause.MemberCause;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.repository.member.temporarymember.TemporaryMemberRepository;
import com.letmeknow.service.email.EmailService;
import com.letmeknow.util.CodeGenerator;
import com.letmeknow.util.Validator;
import com.letmeknow.util.email.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.mail.MessagingException;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.letmeknow.auth.messages.MemberMessages.MEMBER;
import static com.letmeknow.auth.messages.MemberMessages.TEMPORARY_MEMBER;
import static com.letmeknow.message.messages.MemberMessage.VERIFICATION_CODE;
import static com.letmeknow.message.messages.Messages.*;

@Service
@Transactional(readOnly = true)
@Validated
@RequiredArgsConstructor
public class TemporaryMemberService {
    private final EmailService emailService;
    private final MemberRepository memberRepository;
    private final TemporaryMemberRepository temporaryMemberRepository;

    private final PasswordEncoder passwordEncoder;
    private final CodeGenerator codeGenerator;
    private final Validator validator;

    @Value("${domain}")
    private String domain;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${server.port}")
    private String port;

    public TemporaryMemberDto findTemporaryMemberByEmail(String email) throws NoSuchTemporaryMemberException {
        return temporaryMemberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchTemporaryMemberException(new StringBuffer().append(SUCH).append(TEMPORARY_MEMBER).append(NOT_EXISTS).toString()))
                .toDto();
    }

    public String findVerificationCodeByEmail(String email) throws NoSuchTemporaryMemberException {
        return temporaryMemberRepository.findVerificationCodeByEmail(email)
                .orElseThrow(() -> new NoSuchTemporaryMemberException(new StringBuffer().append(SUCH).append(TEMPORARY_MEMBER).append(NOT_EXISTS).toString()));
    }

    @Transactional
    public Long joinTemporaryMember(@Valid MemberSignUpForm memberSignUpForm) throws MemberSignUpValidationException, UnsupportedEncodingException, MessagingException, ConstraintViolationException {
        // 중복된 이메일이 존재하는지 확인한다.
        isEmailUsed(memberSignUpForm.getEmail());

        // ToDo: Validation 추가
        validator.validateNewMember(memberSignUpForm);

        // verificationCode 생성
        String verificationCode = codeGenerator.generateCode(20);

        //temporaryMember 생성
        Long temporaryMemberId = temporaryMemberRepository.save(TemporaryMember.builder()
                        .name(memberSignUpForm.getName().trim())
                        .email(memberSignUpForm.getEmail().trim())
                        .password(passwordEncoder.encode(memberSignUpForm.getPassword().trim()))
                        .city(memberSignUpForm.getCity().trim())
                        .street(memberSignUpForm.getStreet().trim())
                        .zipcode(memberSignUpForm.getZipcode().trim())
                        .verificationCode(verificationCode)
                        .build())
                .getId();

        // Profile이 prod면, domain의 http를 https로 바꾼다.
        String newDomain = domain;
        if (activeProfile.equals(SpringProfile.PROD.getProfile())) {
            // domain의 http를 https로 바꾼다.
            newDomain = domain.replace("http", "https");
        }
        // Profile이 local면, domain 끝에 포트를 붙여준다.
        else if (activeProfile.equals(SpringProfile.LOCAL.getProfile())) {
            newDomain += ":" + port;
        }

        //이메일 발송
        emailService.sendMail(Email.builder()
                .subject(EmailEnum.VERIFICATION_EMAIL_SUBJECT.getMessage())
                .receiver(memberSignUpForm.getEmail())
                .message(EmailEnum.VERIFICATION_EMAIL_MESSAGE.getMessage() +
                        EmailEnum.VERIFICATION_EMAIL_CONTENT_1.getMessage() +
                        newDomain +
                        EmailEnum.VERIFICATION_EMAIL_CONTENT_2.getMessage() +
                        URLEncoder.encode(verificationCode, "UTF-8") +
                        EmailEnum.VERIFICATION_EMAIL_CONTENT_3.getMessage())
                .build());

        return temporaryMemberId;
    }

    @Transactional
    public void resendVerificationEmailById(long id) throws NoSuchTemporaryMemberException, UnsupportedEncodingException, MessagingException {
        TemporaryMember temporaryMember = temporaryMemberRepository.findById(id)
                .orElseThrow(() -> new NoSuchTemporaryMemberException(TemporaryMemberErrorMessage.NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_ID.getMessage()));

        //verificationCode 생성
        String verificationCode = codeGenerator.generateCode(20);

        //temporaryMember의 verificationCode 변경
        temporaryMember.updateVerificationCode(verificationCode);

        // Profile이 prod면, domain의 http를 https로 바꾼다.
        String newDomain = domain;
        if (activeProfile.equals(SpringProfile.PROD.getProfile())) {
            // domain의 http를 https로 바꾼다.
            newDomain = domain.replace("http", "https");
        }
        // Profile이 local면, domain 끝에 포트를 붙여준다.
        else if (activeProfile.equals(SpringProfile.LOCAL.getProfile())) {
            newDomain += ":" + port;
        }

        //이메일 발송
        emailService.sendMail(Email.builder()
                .subject(EmailEnum.VERIFICATION_EMAIL_SUBJECT.getMessage())
                .receiver(temporaryMember.getEmail())
                .message(EmailEnum.VERIFICATION_EMAIL_MESSAGE.getMessage() +
                        EmailEnum.VERIFICATION_EMAIL_CONTENT_1.getMessage() +
                        newDomain +
                        EmailEnum.VERIFICATION_EMAIL_CONTENT_2.getMessage() +
                        URLEncoder.encode(verificationCode, "UTF-8") +
                        EmailEnum.VERIFICATION_EMAIL_CONTENT_3.getMessage())
                .build());
    }

    @Transactional
    public void resendVerificationEmailByEmail(String email) throws NoSuchTemporaryMemberException, UnsupportedEncodingException, MessagingException {
        TemporaryMember temporaryMember = temporaryMemberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchTemporaryMemberException(new StringBuffer().append(SUCH).append(TEMPORARY_MEMBER).append(NOT_EXISTS).toString()));

        //verificationCode 생성
        String verificationCode = codeGenerator.generateCode(20);

        // temporaryMember의 verificationCode 변경
        temporaryMember.updateVerificationCode(verificationCode);

        // Profile이 prod면, domain의 http를 https로 바꾼다.
        String newDomain = domain;
        if (activeProfile.equals(SpringProfile.PROD.getProfile())) {
            // domain의 http를 https로 바꾼다.
            newDomain = domain.replace("http", "https");
        }
        // Profile이 local면, domain 끝에 포트를 붙여준다.
        else if (activeProfile.equals(SpringProfile.LOCAL.getProfile())) {
            newDomain += ":" + port;
        }

        // 이메일 발송
        emailService.sendMail(Email.builder()
                .subject(EmailEnum.VERIFICATION_EMAIL_SUBJECT.getMessage())
                .receiver(temporaryMember.getEmail())
                .message(EmailEnum.VERIFICATION_EMAIL_MESSAGE.getMessage() +
                        EmailEnum.VERIFICATION_EMAIL_CONTENT_1.getMessage() +
                        newDomain +
                        EmailEnum.VERIFICATION_EMAIL_CONTENT_2.getMessage() +
                        URLEncoder.encode(verificationCode, "UTF-8") +
                        EmailEnum.VERIFICATION_EMAIL_CONTENT_3.getMessage())
                .build());
    }

    @Transactional
    public void verifyEmailAndTurnIntoMember(String verificationCode) throws NoSuchTemporaryMemberException {
        TemporaryMember temporaryMember = temporaryMemberRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new NoSuchTemporaryMemberException(
                    SUCH.getMessage()
                    + VERIFICATION_CODE.getMessage()
                    + WITH.getMessage()
                    + TEMPORARY_MEMBER.getMessage()
                    + NOT_EXISTS.getMessage()));

        // verificationCode가 일치하면
        if (verificationCode.equals(temporaryMember.getVerificationCode())) {
            // member 생성
            memberRepository.save(Member.builder()
                            .name(temporaryMember.getName())
                            .email(temporaryMember.getEmail())
                            .password(temporaryMember.getPassword())
                            .city(temporaryMember.getAddress().getCity())
                            .street(temporaryMember.getAddress().getStreet())
                            .zipcode(temporaryMember.getAddress().getZipcode())
                            .build());

            //temporaryMember 삭제
            temporaryMemberRepository.delete(temporaryMember);
        }
        else {
            throw new NoSuchTemporaryMemberException(
                    SUCH.getMessage()
                    + VERIFICATION_CODE.getMessage()
                    + WITH.getMessage()
                    + TEMPORARY_MEMBER.getMessage()
                    + NOT_EXISTS.getMessage());
        }
    }

    private void isEmailUsed(String email) throws MemberSignUpValidationException {
        if (temporaryMemberRepository.findByEmail(email).isPresent()) {
            throw new MemberSignUpValidationException(MemberCause.EMAIL,
                TEMPORARY_MEMBER.getMessage() + ALREADY.getMessage() + EXISTS.getMessage());
        }

        if (memberRepository.findIdByEmail(email).isPresent()) {
            throw new MemberSignUpValidationException(MemberCause.EMAIL,
                MEMBER.getMessage() + ALREADY.getMessage() + EXISTS.getMessage());
        }
    }
}
