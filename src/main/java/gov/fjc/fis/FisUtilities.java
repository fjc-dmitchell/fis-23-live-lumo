package gov.fjc.fis;

import gov.fjc.fis.entity.OutputType;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.reports.entity.ReportOutputType;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.Objects.requireNonNullElse;

public final class FisUtilities {

    private FisUtilities() {
        // don't allow this class to be instantiated
    }

    public static String getCreatedModifiedString(String createdBy, OffsetDateTime createdDate) {
        String createdByStr = "";
        DateTimeFormatter f = DateTimeFormatter.ofPattern("M/d/yyyy");

        if (createdDate != null) {
            createdByStr += "Created on " + f.format(createdDate);
            if (createdBy != null) {
                createdByStr += " by " + createdBy;
            }
            createdByStr += ". ";
        }
        return createdByStr;
    }

    public static String getCreatedModifiedString(String createdBy, OffsetDateTime createdDate,
                                                  String lastModifiedBy, OffsetDateTime lastModifiedDate) {
        String createdByStr = "";
        DateTimeFormatter f = DateTimeFormatter.ofPattern("M/d/yyyy");

        if (createdDate != null) {
            createdByStr += "Created on " + f.format(createdDate);
            if (createdBy != null) {
                createdByStr += " by " + createdBy;
            }
            createdByStr += ". ";
        }

        if (lastModifiedDate != null && lastModifiedBy != null) {
            createdByStr += "Last modified on " + f.format(lastModifiedDate);
            createdByStr += " by " + lastModifiedBy;
            createdByStr += ". ";
        }

        if (createdByStr.isEmpty()) {
            return null;
        } else {
            return createdByStr;
        }
    }

    public static String getAoSyncDateString(Date aoSendDate) {
        SimpleDateFormat f = new SimpleDateFormat("M/d/yyyy");
        Date cutoff;
        try {
            cutoff = f.parse("10/1/2020");
        } catch (ParseException e) {

            throw new RuntimeException("Unable to parse date in getAoSendDateString");
        }
        if (aoSendDate == null) {
            return "";
        }
        if (aoSendDate.compareTo(cutoff) < 0) {
            return "Sent to AO on " + f.format(aoSendDate);
        } else {
            return "Received from AO on " + f.format(aoSendDate);
        }
    }

    // rewritten 2026-08-05 to use LocalDate due to Jmix 3.0 upgrade
    public static String getAoSyncDateString(LocalDate aoSendDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        LocalDate cutoff = LocalDate.of(2020, 10, 1);

        if (aoSendDate == null) {
            return "";
        }

        if (aoSendDate.isBefore(cutoff)) {
            return "Sent to AO on " + aoSendDate.format(formatter);
        } else {
            return "Received from AO on " + aoSendDate.format(formatter);
        }
    }


    public static String getLoadedByString(String loadedBy, OffsetDateTime loadDate) {
        String loadedByStr = "";
        SimpleDateFormat f = new SimpleDateFormat("M/d/yy");

        if (loadDate != null) {
            loadedByStr += "Loaded on " + f.format(loadDate);
            if (loadedBy != null) {
                loadedByStr += " by " + loadedBy;
            }
            loadedByStr += ".";
        }

        return loadedByStr.isEmpty() ? "" : loadedByStr;
    }

    /**
     * Jmix 2.x removed @CaseConversion annotation. Use this method until they replace it.
     *
     * @param value
     * @return String
     */
    public static String safeToUpperCase(String value) {
        return value == null ? null : value.toUpperCase(Locale.ROOT);
    }

    /**
     * Computes total even if nulls passed. Required because user can leave amounts empty in user interface.
     *
     * @param firstAmount
     * @param secondAmount
     * @return BigDecimal
     */
    public static BigDecimal getTotalNullAllowed(BigDecimal firstAmount, BigDecimal secondAmount) {
        return requireNonNullElse(firstAmount, BigDecimal.ZERO).add(requireNonNullElse(secondAmount, BigDecimal.ZERO));
    }

    // same as above but variable number of arguments. for now, for testing only 2025-01-31
//    public static BigDecimal add(BigDecimal... numbers) {
//        BigDecimal sum = BigDecimal.ZERO;
//        for (BigDecimal num : numbers) {
//            if (num != null) {
//                sum = sum.add(num);
//            }
//        }
//        return sum;
//    }
    // AI suggests the following, but neads to be tested!

    /**
     * Returns the sum of the provided BigDecimal values.
     * Null values are ignored.
     *
     * @param numbers an array of BigDecimal values to sum; may contain nulls
     * @return the total of all non-null values; BigDecimal.ZERO if none provided
     */
    public static BigDecimal add(BigDecimal... numbers) {
        return Arrays.stream(numbers)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static String getDateTimeReportString(LocalDateTime dateTime) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/d/yyyy h:mm a");
        return dtf.format(dateTime);
    }

    public static String getDateTimeFilenameString(LocalDateTime dateTime) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh-mm a");
        return dtf.format(dateTime);
    }

    public static LocalDateTime getDateTime() {
        return LocalDateTime.now();
    }

    public static ReportOutputType getReportOutputType(OutputType outputType) {
        if ((outputType == null) || outputType.equals(OutputType.PDF)) {
            return ReportOutputType.PDF;
        } else {
            return ReportOutputType.XLSX;
        }
    }

    public static String getLocation(String city, String state) {
        if (city == null) {
            city = state;
        } else {
            if (state != null) {
                city = city.concat(", ").concat(state);
            }
        }
        return city;
    }

//    public static boolean nonZero(BigDecimal... params) {
//        for (BigDecimal p : params) {
//            if (p != null && !p.equals(BigDecimal.ZERO)) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * Determine presence of non-zero, non-null BigDecimal parameter
     *
     * @param decimals one or more BigDecimal parameters, null values allowed but ignored
     * @return boolean representing presence of a negative or positive BigDecimal value
     */
    public static boolean nonZero(BigDecimal... decimals) {
        return Arrays.stream(decimals).filter(Objects::nonNull).anyMatch(bd -> bd.signum() != 0);
    }

    /**
     * calculates today less number of days, without time.
     *
     * @param days number of days to subtract from today
     * @return java.util.Date
     */
    public static Date getCurrentDateMinusDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, -days);

        return calendar.getTime();
    }

    public static LocalDateTime convertOffsetDateTimeToLocalDateTime(OffsetDateTime offsetDateTime) {
        ZonedDateTime zoned = offsetDateTime.atZoneSameInstant(ZoneId.of("America/New_York"));
        return zoned.toLocalDateTime();
    }

    public static int getNumberOfDaysFromToday(Date date) {
        if (date == null) {
            return 0;
        }
        long diffInMillies = Math.abs(getCurrentDateMinusDays(0).getTime() - date.getTime());
        return (int) TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public static String formatListWithDelimiter(List<String> items, String delimiter) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        if (items.size() == 1) {
            return items.getFirst();
        }

        String allButLast = String.join(delimiter, items.subList(0, items.size() - 1));

        return allButLast + " and " + items.getLast();
    }

    public static String safeTrim(String value) {
        return value != null ? value.trim() : null;
    }



    public static String cleanText(String str) {
        if (str == null) return null;
        String text = str.trim();
        if (text.isEmpty()) {
            return null;
        }
        // strips off all non-ASCII characters
        text = text.replaceAll("[^\\x00-\\x7F]", "");

        // erases all the ASCII control characters
        text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        // removes non-printable characters from Unicode
        text = text.replaceAll("\\p{C}", "");

        return text;
    }

    /**
     * updates values in dependent field
     * @param loader
     * @param field
     * @param keyExtractor
     * @param <T>
     */
    public static <T> void refreshField(CollectionLoader<T> loader,
                                 EntityComboBox<T> field,
                                 Function<T, ?> keyExtractor) {
        loader.load();
        field.setValue(
                Optional.ofNullable(field.getValue())
                        .map(keyExtractor)
                        .flatMap(key -> loader.getContainer().getItems().stream()
                                .filter(item -> keyExtractor.apply(item).equals(key))
                                .findFirst())
                        .orElse(null)
        );
    }
}
