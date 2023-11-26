package com.letmeknow.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;
import com.letmeknow.util.email.Email;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Async
    public String sendMail(Email email) throws MessagingException {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(email.getReceiver());
            mimeMessageHelper.setSubject(email.getSubject());
            mimeMessageHelper.setText(email.getMessage(), true);
            javaMailSender.send(mimeMessage);

            return "success";
        }
        catch (Exception e) {
            sendMail(email);
        }

        throw new MessagingException("메일 전송에 실패했습니다.");
    }
}
