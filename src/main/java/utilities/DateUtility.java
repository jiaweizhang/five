package utilities;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Created by jiaweizhang on 2/3/2017.
 */
public class DateUtility {
    private final static DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // http://stackoverflow.com/questions/17432735/convert-unix-time-stamp-to-date-in-java
    public static String getTimestampString(long unixTime) {

        return Instant.ofEpochSecond(unixTime)
                .atZone(ZoneId.of("GMT-5"))
                .format(formatter);
    }
}
