package com.letmeknow.service.member;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.letmeknow.domain.member.Member;
import com.letmeknow.domain.member.TemporaryMember;
import com.letmeknow.dto.member.MemberCreationDto;
import com.letmeknow.enumstorage.errormessage.member.temporarymember.TemporaryMemberErrorMessage;
import com.letmeknow.enumstorage.message.EmailMessage;
import com.letmeknow.exception.member.temporarymember.NoSuchTemporaryMemberException;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.repository.member.temporarymember.TemporaryMemberRepository;
import com.letmeknow.service.email.EmailService;
import com.letmeknow.util.CodeGenerator;
import com.letmeknow.util.email.Email;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TemporaryMemberService {
    private final EmailService emailService;
    private final MemberRepository memberRepository;
    private final TemporaryMemberRepository temporaryMemberRepository;
    private final PasswordEncoder passwordEncoder;

    public Long findIdByEmail(String email) throws NoSuchTemporaryMemberException {
        return temporaryMemberRepository.findIdByEmail(email)
                .orElseThrow(() -> new NoSuchTemporaryMemberException(TemporaryMemberErrorMessage.NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_EMAIL.getMessage()));
    }

    public String findVerificationCodeByEmail(String email) throws NoSuchTemporaryMemberException {
        return temporaryMemberRepository.findVerificationCodeByEmail(email)
                .orElseThrow(() -> new NoSuchTemporaryMemberException(TemporaryMemberErrorMessage.NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_EMAIL.getMessage()));
    }

    @Transactional
    public Long joinTemporaryMember(MemberCreationDto memberCreationDto) throws DataIntegrityViolationException, UnsupportedEncodingException, MessagingException {
        //verificationCode 생성
        String verificationCode = CodeGenerator.generateCode(20);

        //temporaryMember 생성
        Long temporaryMemberId = temporaryMemberRepository.save(TemporaryMember.builder()
                        .name(memberCreationDto.getName())
                        .email(memberCreationDto.getEmail())
                        .password(passwordEncoder.encode(memberCreationDto.getPassword()))
                        .city(memberCreationDto.getCity())
                        .street(memberCreationDto.getStreet())
                        .zipcode(memberCreationDto.getZipcode())
                        .verificationCode(verificationCode)
                        .build())
                .getId();

        //이메일 발송
        emailService.sendMail(Email.builder()
                .subject(EmailMessage.VERIFICATION_EMAIL_SUBJECT.getMessage())
                .receiver(memberCreationDto.getEmail())
                .message(EmailMessage.VERIFICATION_EMAIL_MESSAGE.getMessage() +
                        EmailMessage.VERIFICATION_EMAIL_LINK1.getMessage() +
                        URLEncoder.encode(verificationCode, "UTF-8") +
                        EmailMessage.VERIFICATION_EMAIL_LINK2.getMessage())
                .build());

        return temporaryMemberId;
    }

    @Transactional
    public void resendVerificationEmailById(Long id) throws NoSuchTemporaryMemberException, UnsupportedEncodingException, MessagingException {
        TemporaryMember temporaryMember = temporaryMemberRepository.findById(id)
                .orElseThrow(() -> new NoSuchTemporaryMemberException(TemporaryMemberErrorMessage.NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_ID.getMessage()));

        //verificationCode 생성
        String verificationCode = CodeGenerator.generateCode(20);

        //temporaryMember의 verificationCode 변경
        temporaryMember.updateVerificationCode(verificationCode);

        //이메일 발송
        emailService.sendMail(Email.builder()
                .subject(EmailMessage.VERIFICATION_EMAIL_SUBJECT.getMessage())
                .receiver(temporaryMember.getEmail())
                .message(EmailMessage.VERIFICATION_EMAIL_MESSAGE.getMessage() +
                        EmailMessage.VERIFICATION_EMAIL_LINK1.getMessage() +
                        URLEncoder.encode(verificationCode, "UTF-8") +
                        EmailMessage.VERIFICATION_EMAIL_LINK2.getMessage())
                .build());
    }

    @Transactional
    public void resendVerificationEmailByEmail(String email) throws NoSuchTemporaryMemberException, UnsupportedEncodingException, MessagingException {
        TemporaryMember temporaryMember = temporaryMemberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchTemporaryMemberException(TemporaryMemberErrorMessage.NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_EMAIL.getMessage()));

        //verificationCode 생성
        String verificationCode = CodeGenerator.generateCode(20);

        //temporaryMember의 verificationCode 변경
        temporaryMember.updateVerificationCode(verificationCode);

        //이메일 발송
        emailService.sendMail(Email.builder()
                .subject(EmailMessage.VERIFICATION_EMAIL_SUBJECT.getMessage())
                .receiver(temporaryMember.getEmail())
                .message(EmailMessage.VERIFICATION_EMAIL_MESSAGE.getMessage() +
                        EmailMessage.VERIFICATION_EMAIL_LINK1.getMessage() +
                        URLEncoder.encode(verificationCode, "UTF-8") +
                        EmailMessage.VERIFICATION_EMAIL_LINK2.getMessage())
                .build());
    }

    @Transactional
    public void verifyEmail(String verificationCode) throws NoSuchTemporaryMemberException {
        TemporaryMember temporaryMember = temporaryMemberRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new NoSuchTemporaryMemberException(TemporaryMemberErrorMessage.NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_VERIFICATION_CODE.getMessage()));

        //verificationCode가 일치하면
        if (verificationCode.equals(temporaryMember.getVerificationCode())) {
            //member 생성
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
        } else {
            throw new NoSuchTemporaryMemberException(TemporaryMemberErrorMessage.NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_VERIFICATION_CODE.getMessage());
        }
    }

}
