package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.service.AppropriationService;
import gov.fjc.fis.service.ObligationService;
import io.jmix.email.EmailAttachment;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static gov.fjc.fis.app.FisUtilities.getDateTime;
import static gov.fjc.fis.app.DateUtils.formatFilenameDateTime;

/**
 * Service responsible for generating reconciliation reports during the third
 * step of JIFMS job processing.
 * <p>
 * JIFMS jobs are processed in three principal steps:
 * </p>
 * <ol>
 *     <li>Loading documents received from JIFMS.</li>
 *     <li>Validating and processing the loaded documents.</li>
 *     <li>Generating and emailing reconciliation reports.</li>
 * </ol>
 * <p>
 * This service contains the business logic for the third step. It identifies
 * the appropriations to be reported, retrieves their reconciliation data,
 * generates an Excel workbook for each appropriation, and packages the
 * resulting workbooks as email attachments.
 * </p>
 * <p>
 * Workbooks are initially generated from an Excel template by the Jmix report
 * engine. Because the template contains a pivot table whose source is an Excel
 * table, the generated workbook must be reopened as an Apache POI
 * {@link XSSFWorkbook}. Each Excel table is then expanded to include all rows
 * produced by the report data band. This ensures that the pivot table includes
 * the complete reconciliation data when the workbook is opened and refreshed.
 * </p>
 *
 * @author Doug Mitchell
 * @since 2.3
 */
@Component("fis_ReconciliationReportService")
public class ReconciliationReportService {

    /**
     * Service used to identify the appropriations included in the
     * reconciliation reports.
     */
    private final AppropriationService appropriationService;

    /**
     * Service used to retrieve reconciliation data for an appropriation.
     */
    private final ObligationService obligationService;

    /**
     * Jmix report runner used to generate reconciliation workbooks from the
     * configured report definition and Excel template.
     */
    private final ReportRunner reportRunner;

    /**
     * Logger used to record reconciliation report generation failures.
     */
    private static final Logger log = LoggerFactory.getLogger(ReconciliationReportService.class);

    /**
     * Creates a reconciliation report service.
     *
     * @param appropriationService the service used to retrieve the
     *                             appropriations included in reconciliation
     *                             reporting
     * @param obligationService    the service used to retrieve reconciliation
     *                             data for each appropriation
     * @param reportRunner         the Jmix report runner used to generate the
     *                             reconciliation workbooks
     */
    public ReconciliationReportService(AppropriationService appropriationService,
                                       ObligationService obligationService,
                                       ReportRunner reportRunner) {
        this.appropriationService = appropriationService;
        this.obligationService = obligationService;
        this.reportRunner = reportRunner;
    }

    /**
     * Generates a reconciliation report for the specified appropriation.
     * <p>
     * Reconciliation data is retrieved from the obligation service and passed
     * to the Jmix report named {@code reconciliation}. After the report engine
     * generates the Excel workbook, the workbook is reopened using Apache POI,
     * and its Excel tables are expanded to include all rows produced by the
     * report data band.
     * </p>
     * <p>
     * The returned content is ready to be downloaded, persisted, or included
     * as an email attachment.
     * </p>
     *
     * @param appropriation the appropriation for which to generate the
     *                      reconciliation report
     * @return the generated and post-processed XLSX workbook as a byte array
     * @throws UncheckedIOException if the generated workbook cannot be read,
     *                              modified, or written
     */
    public byte[] generateReportBytes(Appropriation appropriation) {
        var obligations = obligationService.getReconciliationDto(appropriation);

        ReportOutputDocument document = reportRunner.byReportCode("reconciliation")
                .addParam("reportData", obligations)
                .run();

        byte[] content = document.getContent();

        // Use Apache POI to expand any Excel tables to include all rows we've added to report band.
        // The report template should also set Pivot to not use cache and recalculate when workbook
        // is opened.
        try {
            content = expandTablesToFitData(content);
        } catch (IOException e) {
            log.error("Failed to expand tables to reconciliation for {} appropriation",
                    appropriation.getBudgetFiscalYear());
            throw new RuntimeException(e);
        }

        return content;
    }

    /**
     * Creates the file name for an appropriation's reconciliation report.
     * <p>
     * The file name identifies the appropriation's budget fiscal year and the
     * date and time at which the file name was created. The returned name uses
     * the {@code .xlsx} file extension.
     * </p>
     *
     * @param appropriation the appropriation represented by the report
     * @return the reconciliation report file name
     */
    public String getFilename(Appropriation appropriation) {
        LocalDateTime reportDateTime = getDateTime();
        return String.format(
                "FIS Reconciliation FY%s as of %s.xlsx",
                appropriation.getBudgetFiscalYear(),
                formatFilenameDateTime(reportDateTime)
        );
    }

    /**
     * Generates the reconciliation reports to be included as email
     * attachments.
     * <p>
     * Under normal circumstances, reports are generated for the current
     * fiscal year and the two preceding fiscal years. During October, a fourth
     * report is generated for the additional preceding fiscal year so that
     * the oldest report is not removed during the transition to the new
     * fiscal year.
     * </p>
     * <p>
     * Each attachment contains a completed Excel workbook whose tables have
     * been expanded to include all reconciliation data generated by the Jmix
     * report engine.
     * </p>
     *
     * @return the reconciliation report attachments, one for each applicable
     *         appropriation
     * @throws UncheckedIOException if a generated workbook cannot be read,
     *                              modified, or written
     */
    public List<EmailAttachment> getAttachments() {
        // don't drop off oldest fiscal year during October
        boolean isOctober = LocalDate.now().getMonth() == Month.OCTOBER;
        int numberOfReportYears = isOctober ? 4 : 3;

        List<Appropriation> appropriations = appropriationService.getReconciliationAppropriations(numberOfReportYears);
        List<EmailAttachment> attachments = new ArrayList<>();

        for (var appropriation : appropriations) {

            byte[] content = generateReportBytes(appropriation);

            EmailAttachment attachment = new EmailAttachment(content, getFilename(appropriation));
            attachments.add(attachment);
        }
        return attachments;
    }

    /**
     * Expands every Excel table in the supplied workbook to include all
     * contiguous data rows generated from the report data bands.
     * <p>
     * Jmix generates the workbook from an Excel template whose table ranges
     * initially cover only the template rows. This method reopens the generated
     * workbook, examines every table on every worksheet, and updates each
     * table's range and AutoFilter reference to cover the rows populated by the
     * report engine.
     * </p>
     *
     * @param rawXlsx the XLSX workbook generated by the Jmix report engine
     * @return the modified XLSX workbook as a byte array
     * @throws IOException if the workbook cannot be read, modified, or written
     */
    private byte[] expandTablesToFitData(byte[] rawXlsx) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(rawXlsx))) {

            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                XSSFSheet sheet = wb.getSheetAt(i);
                for (XSSFTable table : sheet.getTables()) {
                    expandTable(sheet, table);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Expands an Excel table to include all contiguous populated rows beneath
     * its header.
     * <p>
     * The table's existing column boundaries are preserved. Beginning with the
     * row immediately below the header, the method scans each row within those
     * columns until it encounters the first row containing no data. The table
     * range is then extended through the last populated row, and the table's
     * AutoFilter reference is updated to match.
     * </p>
     * <p>
     * An Excel table requires at least one row below its header. If no
     * populated data row is found, the table retains at least the data row
     * defined by the original template.
     * </p>
     *
     * @param sheet the worksheet containing the table
     * @param table the Excel table to expand
     */
    private void expandTable(XSSFSheet sheet, XSSFTable table) {
        AreaReference templateArea = table.getCellReferences();
        int headerRowIdx = templateArea.getFirstCell().getRow();
        int firstCol = templateArea.getFirstCell().getCol();
        int lastCol = templateArea.getLastCell().getCol();

        int lastDataRow = headerRowIdx;
        int rowIdx = headerRowIdx + 1;

        while (true) {
            Row row = sheet.getRow(rowIdx);
            boolean rowHasData = false;

            if (row != null) {
                for (int c = firstCol; c <= lastCol; c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null && cell.getCellType() != CellType.BLANK) {
                        rowHasData = true;
                        break;
                    }
                }
            }

            if (!rowHasData) {
                break;
            }
            lastDataRow = rowIdx;
            rowIdx++;
        }

        // A table needs at least one data row beyond the header; if scanning
        // found nothing, fall back to the template's original single row
        // rather than shrinking the table to just the header.
        if (lastDataRow == headerRowIdx) {
            lastDataRow = Math.max(headerRowIdx + 1, templateArea.getLastCell().getRow());
        }

        AreaReference newArea = new AreaReference(
                new CellReference(headerRowIdx, firstCol),
                new CellReference(lastDataRow, lastCol),
                SpreadsheetVersion.EXCEL2007);

        table.setArea(newArea);

        if (table.getCTTable().getAutoFilter() != null) {
            table.getCTTable().getAutoFilter().setRef(newArea.formatAsString());
        }
    }
}