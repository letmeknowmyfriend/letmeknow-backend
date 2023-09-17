package com.security.service.member;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.security.domain.member.Member;
import com.security.dto.member.MemberCreationDto;
import com.security.dto.member.MemberFindDto;
import com.security.dto.member.MemberPasswordUpdateDto;
import com.security.dto.member.MemberUpdateDto;
import com.security.enumstorage.errormessage.MemberErrorMessage;
import com.security.enumstorage.errormessage.auth.PasswordErrorMessage;
import com.security.enumstorage.message.EmailMessage;
import com.security.exception.auth.PasswordException;
import com.security.exception.member.NoSuchMemberException;
import com.security.repository.member.MemberRepository;
import com.security.service.email.EmailService;
import com.security.util.CodeGenerator;
import com.security.util.email.Email;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

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

    @Transactional
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

    public boolean isEmailValid(String email) {
        return !(memberRepository.findIdByEmail(email).isPresent());
    }

    public List<MemberFindDto> findAllMemberFindDto() {
        return memberRepository.findAll().stream()
                .map(Member::toMemberFindDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long joinMember(MemberCreationDto memberCreationDto) throws DataIntegrityViolationException {
        return memberRepository.save(Member.builder()
                        .name(memberCreationDto.getName())
                        .email(memberCreationDto.getEmail())
                        .password(memberCreationDto.getPassword())
                        .city(memberCreationDto.getCity())
                        .street(memberCreationDto.getStreet())
                        .zipcode(memberCreationDto.getZipcode())
                .build())
            .getId();
    }

    /**
     * 회원의 정보를 수정한다.
     * 이름과 주소 수정 가능
     * @param memberUpdateDto
     * @return
     * @throws NoSuchMemberException
     */
    @Transactional
    public Long updateMember(MemberUpdateDto memberUpdateDto) throws NoSuchMemberException {
        //업데이트하려는 회원이 있는지 검증
        Member findMemberById = findById(memberUpdateDto.getId());

        //회원의 정보 업데이트
        findMemberById.updateMemberName(memberUpdateDto.getName());
        findMemberById.updateMemberAddress(memberUpdateDto.getCity(), memberUpdateDto.getStreet(), memberUpdateDto.getZipcode());

        //저장
        Member savedMember = memberRepository.save(findMemberById);

        return savedMember.getId();
    }

    @Transactional
    public void updateMemberPassword(MemberPasswordUpdateDto memberPasswordUpdateDto) throws NoSuchMemberException, PasswordException {
        Member findMemberById = findById(memberPasswordUpdateDto.getId());

        //비밀번호 검증
        if (!findMemberById.getPassword().equals(memberPasswordUpdateDto.getPassword())) {
            throw new PasswordException(PasswordErrorMessage.PASSWORD_DOES_NOT_MATCH.getMessage());
        }

        //회원의 정보 업데이트
        findMemberById.changePassword(memberPasswordUpdateDto.getNewPassword());

        //회원의 정보 저장
        memberRepository.save(findMemberById);
    }

    @Transactional
    public void sendChangePasswordEmail(String email) throws UnsupportedEncodingException, MessagingException {
        Member member = memberRepository.findNotDeletedByEmail(email)
                //해당하는 이메일을 가진 회원이 없으면, 예외 발생
                .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage()));

        //verificationCode 생성
        String verificationCode = CodeGenerator.generateCode(20);

        //PasswordVerificationCode 업데이트
        member.updatePasswordVerificationCode(verificationCode);

        //비밀번호 재설정 이메일 발송
        emailService.sendMail(Email.builder()
                .subject(EmailMessage.CHANGE_PASSWORD_EMAIL_SUBJECT.getMessage())
                .receiver(email)
                .message(EmailMessage.CHANGE_PASSWORD_EMAIL_MESSAGE.getMessage() +
                        EmailMessage.CHANGE_PASSWORD_EMAIL_LINK.getMessage() +
                        URLEncoder.encode(verificationCode, "UTF-8"))
                .build());
    }

    /**
     * 테스트용
     * @param memberId
     */
    @Transactional
    public void switchRole(Long memberId) {
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
