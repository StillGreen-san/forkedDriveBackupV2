package ratismal.drivebackup;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExampleTest {

    @Test
    void exampleWithMockito() {
        @SuppressWarnings("unchecked")
        List<String> mockedList = mock();

        when(mockedList.get(0)).thenReturn("first");

        assertEquals("first", mockedList.get(0));

        assertNull(mockedList.get(999));
    }

    @Test
    void timestuff() {
        ZoneId zoneId = ZoneId.of("Europe/Berlin");
        ZoneOffset zoneOffset = ZoneOffset.of("+1");
        LocalDateTime preDst = LocalDateTime.of(2024, Month.MARCH, 30, 10, 10);
        LocalDateTime postDst = LocalDateTime.of(2024, Month.OCTOBER, 26, 10, 10);
        ZonedDateTime winterOffset = ZonedDateTime.of(preDst, zoneOffset);
        ZonedDateTime winterId = ZonedDateTime.of(preDst, zoneId);
        ZonedDateTime summerOffset = ZonedDateTime.of(postDst, zoneOffset);
        ZonedDateTime summerId = ZonedDateTime.of(postDst, zoneId);
        System.out.println(winterOffset);
        System.out.println(winterOffset.plusDays(1));
        System.out.println(adjusted(winterOffset));
        System.out.println(winterId);
        System.out.println(winterId.plusDays(1));
        System.out.println(adjusted(winterId));
        System.out.println(summerOffset);
        System.out.println(summerOffset.plusDays(1));
        System.out.println(adjusted(summerOffset));
        System.out.println(summerId);
        System.out.println(summerId.plusDays(1));
        System.out.println(adjusted(summerId));
    }

    private static @NotNull ZonedDateTime adjusted(ZonedDateTime zonedDateTime) {
        ZonedDateTime adjustedTime = zonedDateTime
                .with(TemporalAdjusters.nextOrSame(zonedDateTime.getDayOfWeek().plus(1)))
                .with(ChronoField.CLOCK_HOUR_OF_DAY, zonedDateTime.getHour())
                .with(ChronoField.MINUTE_OF_HOUR, zonedDateTime.getMinute());
        if (zonedDateTime.isAfter(adjustedTime)) {
            adjustedTime = adjustedTime.plusWeeks(1);
        }
        return adjustedTime;
    }
}