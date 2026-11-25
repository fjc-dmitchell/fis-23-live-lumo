package gov.fjc.fis.app;

import java.time.*;

public final class FisUtilities {

    private FisUtilities() {
    }

    public static LocalDateTime getDateTime() {
        return LocalDateTime.now();
    }

    public static LocalDate getCurrentDateMinusDays(int days) {
        return LocalDate.now().minusDays(days);
    }
}
