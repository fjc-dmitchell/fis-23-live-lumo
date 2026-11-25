package gov.fjc.fis.app;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility methods and constants for formatting dates and date-time values
 * used throughout FIS.
 * <p>
 * This class centralizes common date formatting patterns to ensure
 * consistent presentation of dates in reports, exported files, and
 * other user-facing content. It also contains application-specific
 * date constants used by business logic.
 * <p>
 * @see AuditTextUtils
 *
 * @author Doug Mitchell
 */
public final class DateUtils {

    /**
     * Formatter used for displaying report date and time values using the
     * pattern {@code M/d/yyyy h:mm a}.
     */
    private static final DateTimeFormatter REPORT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("M/d/yyyy h:mm a");

    /**
     * Formatter used for generating date and time values suitable for
     * inclusion in file names using the pattern
     * {@code yyyy-MM-dd hh-mm a}.
     */
    private static final DateTimeFormatter FILENAME_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd hh-mm a");


    private DateUtils() {
    }

    /**
     * Formats a date-time value for display in reports.
     * <p>
     * Values are formatted using the pattern {@code M/d/yyyy h:mm a}.
     * If the supplied value is {@code null}, an empty string is returned.
     *
     * @param dateTime the date and time to format
     * @return the formatted date-time string, or an empty string if
     *         {@code dateTime} is {@code null}
     */
    public static String formatReportDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return REPORT_DATE_TIME_FORMATTER.format(dateTime);
    }


    /**
     * Formats a date-time value for use in generated file names.
     * <p>
     * Values are formatted using the pattern
     * {@code yyyy-MM-dd hh-mm a}. If the supplied value is {@code null},
     * an empty string is returned.
     *
     * @param dateTime the date and time to format
     * @return the formatted file name date-time string, or an empty string
     *         if {@code dateTime} is {@code null}
     */
    public static String formatFilenameDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return FILENAME_DATE_TIME_FORMATTER.format(dateTime);
    }
}
