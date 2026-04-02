package kz.perpavbek.collab.documentservice.service;

import kz.perpavbek.collab.documentservice.dto.client.User;
import kz.perpavbek.collab.documentservice.entity.Document;
import kz.perpavbek.collab.documentservice.entity.DocumentInvitation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentInvitationEmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final MailProperties mailProperties;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.mail.from}")
    private String mailFrom;

    public void sendInvitationEmail(Document document, DocumentInvitation invitation, User inviter) {

        String invitationUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .pathSegment("invitations", invitation.getToken())
                .build()
                .toUriString();

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null || !StringUtils.hasText(mailProperties.getHost())) {
            log.info(
                    "SMTP is not configured. Invitation link for document {} and user {}: {}",
                    document.getId(),
                    invitation.getInvitedUserId(),
                    invitationUrl
            );
            return;
        }

        String inviterDisplayName = StringUtils.hasText(inviter.getName()) ? inviter.getName() : inviter.getEmail();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(invitation.getInvitedEmail());
        message.setSubject("Invitation to collaborate on \"" + document.getTitle() + "\"");
        message.setText(
                """
                %s invited you to collaborate on the document "%s".

                Open the link below to accept the invitation:
                %s

                After you accept it, the document will appear in your workspace and you will be able to edit it.
                """.formatted(inviterDisplayName, document.getTitle(), invitationUrl).trim()
        );

        mailSender.send(message);
    }
}
