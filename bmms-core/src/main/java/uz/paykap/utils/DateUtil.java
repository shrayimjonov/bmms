package uz.paykap.utils;

import lombok.experimental.UtilityClass;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@UtilityClass
public final class DateUtil {

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'");

    public static Instant dayStart(LocalDate localDate) {
        return localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public static Instant dayEnd(LocalDate localDate) {
        return localDate.atStartOfDay().plusDays(1).minusSeconds(1).toInstant(ZoneOffset.UTC);
    }

    public static Instant todayStart() {
        return LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public static Instant todayEnd() {
        return todayStart().plus(1, ChronoUnit.DAYS).minusSeconds(1);
    }

    public static LocalDate toLocalDate(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).toLocalDate();
    }

    public static Instant getPrevDayBegin() {

        LocalDateTime prevDay = LocalDateTime.now().minusDays(1);

        return Instant.parse(prevDay.format(DATE_TIME_FORMATTER) + "00:00:00.000Z");
    }

    public static Instant getPrevDayEnd() {

        LocalDateTime prevDay = LocalDateTime.now().minusDays(1);

        return Instant.parse(prevDay.format(DATE_TIME_FORMATTER) + "23:59:59.999Z");
    }

    public static Instant getPrevHourBegin() {
        LocalDateTime prevHour = LocalDateTime.now(ZoneId.of("+5")).minusHours(1).truncatedTo(ChronoUnit.HOURS);

        return prevHour.toInstant(ZoneOffset.ofHours(0));
    }

    public static Instant getPrevHourEnd() {
        LocalDateTime prevHour = LocalDateTime.now(ZoneId.of("+5")).minusHours(1).truncatedTo(ChronoUnit.HOURS);
        LocalDateTime prefHourEnd = prevHour.plusMinutes(59).plusSeconds(59);

        return prefHourEnd.toInstant(ZoneOffset.ofHours(0));
    }

    public static LocalDateTime toLocalDateTime(Instant dateTime) {
        return LocalDateTime.ofInstant(dateTime, ZoneOffset.ofHours(+5));
    }

}
