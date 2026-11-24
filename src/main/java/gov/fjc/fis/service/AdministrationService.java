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

/**
 * Provides administrative and infrastructure-related functionality used by
 * FIS background processes and application services.
 * <p>
 * This service centralizes common operations that support application
 * administration, including:
 * <ul>
 *     <li>Sending application and job notification emails</li>
 *     <li>Resolving recipient email addresses based on security roles</li>
 *     <li>Archiving processed feed files</li>
 *     <li>Notifying administrators when archival operations fail</li>
 * </ul>
 * <p>
 * When email recipients are not explicitly specified, notifications are
 * automatically delivered to users assigned the system administrator role.
 * File archival failures generate email notifications and include the
 * affected file as an attachment when possible.
 *
 * @author Doug Mitchell
 */
@Component("fis_AdministrationService")
public class AdministrationService {
    private static final Logger log = LoggerFactory.getLogger(AdministrationService.class);

    private final Emailer emailer;

    private final UnconstrainedDataManager unconstrainedDataManager;

    public AdministrationService(Emailer emailer, UnconstrainedDataManager unconstrainedDataManager) {
        this.emailer = emailer;
        this.unconstrainedDataManager = unconstrainedDataManager;
    }

    /**
     * Returns a comma-delimited list of email addresses for users assigned
     * the specified security role.
     *
     * @param roleCode the role code used to identify recipients
     * @return a comma-delimited string of email addresses, or an empty string
     * if no matching users have email addresses configured
     */
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

    /**
     * Sends an HTML email message without attachments.
     * <p>
     * If no recipient addresses are provided, the message is sent to users
     * assigned the system administrator role.
     *
     * @param emailAddresses comma-delimited list of recipient email addresses
     * @param subject the email subject
     * @param body the email body content
     * @return {@code true} if the email was successfully submitted for
     * sending; {@code false} if an email exception occurred
     */
    public boolean sendEmail(String emailAddresses, String subject, String body) {
        return sendEmail(emailAddresses, subject, body, null);
    }

    /**
     * Sends an HTML email message with optional attachments.
     * <p>
     * If no recipient addresses are provided, the message is sent to users
     * assigned the system administrator role.
     *
     * @param emailAddresses comma-delimited list of recipient email addresses
     * @param subject the email subject
     * @param body the email body content
     * @param emailAttachment attachments to include with the email, or
     * {@code null} if no attachments are required
     * @return {@code true} if the email was successfully submitted for
     * sending; {@code false} if an email exception occurred
     */
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

    /**
     * Archives a processed file by moving it from the feed directory to the
     * archive directory and appending a timestamp to the archived file name.
     * <p>
     * If the file cannot be moved, an email notification is sent containing
     * details of the failure and, when possible, the original file as an
     * attachment to assist with troubleshooting and recovery.
     *
     * @param jobStatusEmailAddresses email recipients for archival failure
     * notifications
     * @param jobErrorEmailSubject subject used for archival failure
     * notifications
     * @param feedDirectory directory containing the source file
     * @param archiveDirectory destination directory for archived files
     * @param fileName name of the file to archive
     * @throws RuntimeException if the file cannot be archived and its contents
     * cannot be read for inclusion in the failure notification
     */
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