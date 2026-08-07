package com.project.hugme.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendVerificationEmail(
            String email,
            String verificationUrl
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("[HUGME] 이메일 인증");

            String content = """
                    <h2>HUGME 이메일 인증</h2>
                    <p>아래 버튼을 눌러 이메일 인증을 완료해주세요.</p>

                    <a href="%s"
                       style="
                           display:inline-block;
                           padding:12px 20px;
                           background:#333;
                           color:white;
                           text-decoration:none;
                           border-radius:6px;
                       ">
                        이메일 인증하기
                    </a>
                    """.formatted(verificationUrl);

            helper.setText(content, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new IllegalStateException(
                    "이메일 발송에 실패했습니다.",
                    e
            );
        }
    }

    public void sendRefreshTokenReuseAlert(String email) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("[HUGME] 비정상적인 로그인 세션이 감지되었습니다.");

            String content = """
                <h2>HUGME 보안 알림</h2>

                <p>
                    이전 로그인 세션에서 발급된 인증 토큰의
                    재사용이 감지되었습니다.
                </p>

                <p>
                    계정 보호를 위해 현재 Refresh Token을
                    폐기하였습니다.
                </p>

                <p>
                    본인이 시도한 것이 아니라면
                    다시 로그인하고 계정 정보를 확인해주세요.
                </p>
                """;

            helper.setText(content, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new IllegalStateException(
                    "보안 알림 이메일 발송에 실패했습니다.",
                    e
            );
        }
    }
}