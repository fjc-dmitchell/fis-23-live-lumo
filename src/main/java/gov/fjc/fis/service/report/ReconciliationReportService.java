package gov.fjc.fis.service.report;

import gov.fjc.fis.entity.Appropriation;
import gov.fjc.fis.job.LoadEmployeesJob;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static gov.fjc.fis.FisUtilities.getDateTime;
import static gov.fjc.fis.FisUtilities.getDateTimeFilenameString;

@Component("fis_ReconciliationReportService")
public class ReconciliationReportService {
    private final AppropriationService appropriationService;
    private final ObligationService obligationService;
    private final ReportRunner reportRunner;

    private static final Logger log = LoggerFactory.getLogger(LoadEmployeesJob.class);

    public ReconciliationReportService(AppropriationService appropriationService,
                                       ObligationService obligationService,
                                       ReportRunner reportRunner) {
        this.appropriationService = appropriationService;
        this.obligationService = obligationService;
        this.reportRunner = reportRunner;
    }

    /**
     * generate a reconciliation report that has been modified by Apache POI. The report
     * can be used as an email attachment or
     *
     * @param appropriation
     * @return
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

    public String getFilename(Appropriation appropriation) {
        LocalDateTime reportDateTime = getDateTime();
        return String.format(
                "FIS Reconciliation FY%s as of %s.xlsx",
                appropriation.getBudgetFiscalYear(),
                getDateTimeFilenameString(reportDateTime)
        );
    }

    /**
     * creates reconciliation reports ready to be attached to email. reports are
     * current fiscal year, prior fiscal year, two years ago, and (during October)
     * three years ago.
     *
     * @return list of attachments (Excel workbooks)
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
     * Expands every Excel Table (ListObject) on every sheet of a Jmix-generated
     * XLSX so that its range covers the header row plus all rows the detail
     * band actually produced, instead of just the single template row.
     *
     * @param rawXlsx the bytes from reportOutputDocument.getContent()
     * @return the same workbook with each table's range/autofilter corrected
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
     * Rewrites a single table's area to span from its header row down to the
     * last row (within the table's original column span) that actually
     * contains data, then updates its autofilter to match.
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