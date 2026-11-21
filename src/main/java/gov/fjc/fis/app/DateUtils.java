package gov.fjc.fis.app;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter SHORT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("M/d/yyyy");

    private static final DateTimeFormatter REPORT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("M/d/yyyy h:mm a");

    private static final DateTimeFormatter FILENAME_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd hh-mm a");

    private static final LocalDate AO_SYNC_DIRECTION_CUTOFF =
            LocalDate.of(2020, 10, 1);

    private DateUtils() {
    }

    public static String formatReportDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return REPORT_DATE_TIME_FORMATTER.format(dateTime);
    }

    public static String formatFilenameDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return FILENAME_DATE_TIME_FORMATTER.format(dateTime);
    }
}
