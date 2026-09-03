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

@DisallowConcurrentExecution
public class ReconciliationReportJob implements Job {
    @Autowired
    private ReconciliationReportService reconciliationReportService;
    @Autowired
    private AdministrationService administrationService;

    @Value("${jifms.email.reconcilation.addresses}")
    private String reconciliationEmailAddresses;

    private static final Logger log = LoggerFactory.getLogger(ReconciliationReportJob.class);

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
