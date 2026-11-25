package gov.fjc.fis.app;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Utility methods for generating consistent audit and history text displayed
 * throughout FIS.
 * <p>
 * This class centralizes the formatting of audit-related dates, timestamps,
 * and descriptive messages used to identify when records were created,
 * modified, synchronized, or loaded into the system. All date and time
 * values are formatted using the Eastern Time Zone to provide consistent
 * presentation regardless of the originating timestamp offset.
 * <p>
 * In addition to audit text generation, this utility provides conversion
 * methods for transforming {@link OffsetDateTime} values into Eastern
 * {@link LocalDateTime} instances for display and reporting purposes.
 *
 * @see DateUtils
 * @author Doug Mitchell
 */
public final class AuditTextUtils {

    private AuditTextUtils() {
    }

    /**
     * Eastern Time Zone used for all audit text generation and date
     * conversions to ensure consistent display throughout the application.
     */
    private static final ZoneId EASTERN = ZoneId.of("America/New_York");

    /**
     * Formatter used for abbreviated dates in audit messages where a
     * two-digit year is sufficient, such as data load history.
     */
    private static final DateTimeFormatter SHORT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("M/d/yy").withZone(EASTERN);

    /**
     * Formatter used for audit messages that display a date without a
     * time component.
     */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("M/d/yyyy").withZone(EASTERN);

    /**
     * Formatter used for audit messages that display both date and time
     * information.
     */
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("M/d/yyyy HH:mm").withZone(EASTERN);

    /**
     * Date on which the AO synchronization process changed from outbound
     * transmissions ("Sent to AO") to inbound synchronization
     * ("Received from AO").
     */
    private static final LocalDate AO_SYNC_CUTOFF = LocalDate.of(2020, 10, 1);

    /**
     * Returns a formatted date and time string for a created timestamp.
     *
     * @param createdDate the creation timestamp
     * @return the formatted date and time, or an empty string if the timestamp
     * is {@code null}
     */
    public static String getCreatedDateTimeString(OffsetDateTime createdDate) {
        return createdDate == null ? "" : DATE_TIME_FORMAT.format(createdDate);
    }

    /**
     * Returns a descriptive audit message indicating when and by whom
     * an entity was created.
     *
     * @param createdBy the user who created the entity
     * @param createdDate the creation timestamp
     * @return a formatted creation audit message, or an empty string if the
     * creation date is {@code null}
     */
    public static String getCreatedString(String createdBy, OffsetDateTime createdDate) {
        return describe("Created", createdDate, createdBy);
    }

    /**
     * Returns a combined audit message containing creation and modification
     * information when available.
     * <p>
     * If modification information is not available, only creation information
     * is returned.
     *
     * @param createdBy the user who created the entity
     * @param createdDate the creation timestamp
     * @param lastModifiedBy the user who last modified the entity
     * @param lastModifiedDate the last modification timestamp
     * @return the combined audit message, or an empty string if no audit
     * information is available
     */
    public static String getCreatedModifiedString(String createdBy, OffsetDateTime createdDate,
                                                  String lastModifiedBy, OffsetDateTime lastModifiedDate) {
        String created = describe("Created", createdDate, createdBy);
        String modified = lastModifiedBy == null ? "" : describe("Last modified", lastModifiedDate, lastModifiedBy);

        String result = created + modified;
        return result.isEmpty() ? "" : result;
    }

    /**
     * Builds a standardized audit description containing an action,
     * date, and optional user name.
     * <p>
     * Examples:
     * <ul>
     *     <li>{@code Created on 10/15/2026 by dmitchell.}</li>
     *     <li>{@code Last modified on 10/20/2026.}</li>
     * </ul>
     *
     * @param action the action being described, such as "Created" or
     *               "Last modified"
     * @param date the date associated with the action
     * @param by the user who performed the action, or {@code null}
     * @return the formatted audit description, or an empty string if
     * the date is {@code null}
     */
    private static String describe(String action, OffsetDateTime date, String by) {
        if (date == null) {
            return "";
        }
        String byClause = by != null ? " by " + by : "";
        return action + " on " + DATE_FORMAT.format(date) + byClause + ". ";
    }

    /**
     * Returns a descriptive message indicating when data was sent to or
     * received from AO based on the synchronization date.
     * <p>
     * Dates prior to the AO synchronization cutoff are labeled as
     * "Sent to AO", while dates on or after the cutoff are labeled as
     * "Received from AO".
     *
     * @param aoSendDate the AO synchronization date
     * @return the formatted AO synchronization message, or an empty string
     * if the date is {@code null}
     */
    public static String getAoSyncDateString(LocalDate aoSendDate) {
        if (aoSendDate == null) {
            return "";
        }

        String label = aoSendDate.isBefore(AO_SYNC_CUTOFF) ? "Sent to AO" : "Received from AO";
        return label + " on " + DATE_FORMAT.format(aoSendDate);
    }

    /**
     * Returns a descriptive message indicating when data was loaded into
     * the system and optionally who performed the load operation.
     *
     * @param loadedBy the user who loaded the data
     * @param loadDate the date the data was loaded
     * @return the formatted load audit message, or an empty string if the
     * load date is {@code null}
     */
    public static String getLoadedByString(String loadedBy, OffsetDateTime loadDate) {
        if (loadDate == null) {
            return "";
        }

        String byClause = loadedBy != null ? " by " + loadedBy : "";
        return "Loaded on " + SHORT_DATE_FORMAT.format(loadDate) + byClause + ".";
    }

    /**
     * Converts an {@link OffsetDateTime} to a {@link LocalDateTime}
     * in the Eastern Time Zone.
     *
     * @param offsetDateTime the timestamp to convert
     * @return the equivalent Eastern local date and time, or {@code null}
     * if the supplied timestamp is {@code null}
     */
    public static LocalDateTime toEasternLocalDateTime(OffsetDateTime offsetDateTime) {
        // Todo: review why it is necessary to convert to Eastern time
        return offsetDateTime == null
                ? null
                : offsetDateTime.atZoneSameInstant(EASTERN).toLocalDateTime();
    }
}
