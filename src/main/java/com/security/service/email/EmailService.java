package com.security.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;
import com.security.util.email.Email;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Transactional
    @Async
    public String sendMail(Email email) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        mimeMessageHelper.setTo(email.getReceiver());
        mimeMessageHelper.setSubject(email.getSubject());
        mimeMessageHelper.setText(email.getMessage(), true);
        javaMailSender.send(mimeMessage);

        return "success";
    }
}
