package gov.fjc.fis.service;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.email.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component("fis_AdministrationService")
public class AdministrationService {
    private static final Logger log = LoggerFactory.getLogger(AdministrationService.class);

    private final Emailer emailer;

    private final UnconstrainedDataManager unconstrainedDataManager;

    public AdministrationService(Emailer emailer, UnconstrainedDataManager unconstrainedDataManager) {
        this.emailer = emailer;
        this.unconstrainedDataManager = unconstrainedDataManager;
    }

    private String getEmailsByRoleAsDelimitedString(String roleCode) {
        List<String> emails = unconstrainedDataManager.loadValue(
                        "SELECT u.email FROM fis_User u " +
                                "JOIN sec_RoleAssignmentEntity r ON u.username = r.username " +
                                "WHERE u.email IS NOT NULL AND r.roleCode = :roleCode",
                        String.class
                )
                .parameter("roleCode", roleCode)
                .list();

        return String.join(",", emails);
    }

    public boolean sendEmail(String emailAddresses, String subject, String body) {
        return sendEmail(emailAddresses, subject, body, null);
    }

    public boolean sendEmail(String emailAddresses,
                             String subject,
                             String body,
                             List<EmailAttachment> emailAttachment) {
        boolean emailSent = false;
        // if addresses not configured, send email to all administrators
        if (emailAddresses == null || emailAddresses.isBlank()) {
            emailAddresses = getEmailsByRoleAsDelimitedString("system-full-access");
        }

        EmailInfo emailInfo = EmailInfoBuilder.create()
                .setAddresses(emailAddresses)
                .setSubject(subject)
                .setBody(body)
                .setAttachments(emailAttachment)
                .setBodyContentType("text/html; charset=UTF-8")
                .build();
        try {
            emailer.sendEmail(emailInfo);
            emailSent = true;
        } catch (EmailException e) {
            log.error("Unable to send email to {} with subject {}: {}", emailAddresses, subject, e.getMessage(), e);
        }
        return emailSent;
    }

    public void archiveFile(String jobStatusEmailAddresses,
                            String jobErrorEmailSubject,
                            String feedDirectory,
                            String archiveDirectory,
                            String fileName) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd_HHmm");
        String timestamp = LocalDateTime.now().format(formatter);

        String newFileName = fileName + "." + timestamp;

        Path oldPath = Paths.get(feedDirectory).resolve(fileName);
        Path newPath = Paths.get(archiveDirectory).resolve(newFileName);

        try {
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            log.info(String.format("File %s archived to %s", oldPath, newPath));

        } catch (IOException moveFailure) {
            log.info(String.format("File %s could not be archived!", oldPath));
            String error = String.format("Unable to move file %s to %s", oldPath, newPath);

            try {
                byte[] bytes = Files.readAllBytes(oldPath);
                EmailAttachment attachment = new EmailAttachment(bytes, fileName);

                sendEmail(jobStatusEmailAddresses,
                        jobErrorEmailSubject,
                        error,
                        List.of(attachment));

            } catch (IOException readFailure) {
                throw new RuntimeException(readFailure);
            }
        }
    }
}