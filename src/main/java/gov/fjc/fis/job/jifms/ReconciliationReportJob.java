package gov.fjc.fis.job.jifms;

import gov.fjc.fis.service.AdministrationService;
import gov.fjc.fis.service.report.ReconciliationReportService;
import io.jmix.core.security.Authenticated;
import io.jmix.email.EmailAttachment;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Quartz job that generates and distributes FIS reconciliation reports.
 *
 * <p>This job is typically executed after JIFMS document processing has
 * completed and is responsible for producing the reconciliation workbooks
 * used by the Finance Office. The generated reports summarize reconciliation
 * data for the applicable appropriations and are delivered as Excel
 * attachments in an email message.
 *
 * <p>The job retrieves reconciliation report attachments from
 * {@link ReconciliationReportService} and sends them to the configured
 * reconciliation distribution list using {@link AdministrationService}. If
 * the email cannot be sent successfully, the job fails so that the scheduling
 * framework can record the error.
 *
 * <p>{@link DisallowConcurrentExecution} prevents multiple instances of the
 * job from running simultaneously, ensuring that reconciliation reports are
 * generated and distributed only once for a given execution cycle.
 *
 * @author Doug Mitchell
 */
@DisallowConcurrentExecution
public class ReconciliationReportJob implements Job {
    @Autowired
    private ReconciliationReportService reconciliationReportService;
    @Autowired
    private AdministrationService administrationService;

    @Value("${jifms.email.reconcilation.addresses}")
    private String reconciliationEmailAddresses;

    private static final Logger log = LoggerFactory.getLogger(ReconciliationReportJob.class);

    /**
     * Executes the reconciliation reporting process.
     *
     * <p>This method generates the reconciliation report attachments, creates
     * an email listing the generated workbooks, and sends the reports to the
     * configured reconciliation recipients. A
     * {@link JobExecutionException} is thrown if email delivery fails.
     *
     * @param context the Quartz job execution context
     * @throws JobExecutionException if the reconciliation email cannot be sent
     */
    @Authenticated
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        List<EmailAttachment> attachments = reconciliationReportService.getAttachments();
        String subject = String.format("FIS Recon exports %s", LocalDate.now());
        String htmlBody = """
                Attached find the following FIS reconciliation Excel workbooks:
                <br /><br />
                %s
                <br /><br />
                Save the files to your computer before opening. Do not open directly from Outlook.
                """.formatted(
                attachments.stream()
                        .map(EmailAttachment::getName)
                        .collect(Collectors.joining("<br />")));

        if (!administrationService.sendEmail(reconciliationEmailAddresses, subject, htmlBody, attachments)) {
            throw new JobExecutionException("Reconciliation email failed: " + subject);
        }
    }
}
