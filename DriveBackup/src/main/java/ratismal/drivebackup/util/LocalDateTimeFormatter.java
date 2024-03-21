package ratismal.drivebackup.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ratismal.drivebackup.config.ConfigParser;

public final class LocalDateTimeFormatter {
    private static final String FORMAT_KEYWORD = "%FORMAT";
    private static final String FORMAT_REPLACEMENT = "'yyyy-M-d--HH-mm'";
    private static final Pattern VALID_FORMAT = Pattern.compile("^[\\w\\-.'% ]+$");

    private final DateTimeFormatter formatter;

    private LocalDateTimeFormatter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @NotNull
    @Contract ("_ -> new")
    public static LocalDateTimeFormatter ofPattern(String pattern) throws IllegalArgumentException {
        if (!VALID_FORMAT.matcher(pattern).find()) {
            throw new IllegalArgumentException("Format pattern contains illegal characters");
        }
        return new LocalDateTimeFormatter(DateTimeFormatter.ofPattern(replaceAndEscape(pattern)));
    }

    /**
     * assumes %FORMAT not mixed with custom pattern
     */
    private static String replaceAndEscape(String base) {
        StringBuilder builder = new StringBuilder(base.length() + FORMAT_REPLACEMENT.length());
        int startOffset = 0;
        if (base.startsWith(FORMAT_KEYWORD)) {
            builder.append(FORMAT_REPLACEMENT,1, FORMAT_REPLACEMENT.length());
            startOffset = FORMAT_KEYWORD.length();
        } else {
            builder.append('\'');
        }
        for (int i = startOffset; i < base.length(); i++) {
            if (base.charAt(i) == '%' && base.regionMatches(i, FORMAT_KEYWORD, 0, FORMAT_KEYWORD.length())) {
                builder.append(FORMAT_REPLACEMENT);
                i += FORMAT_KEYWORD.length() - 1;
            } else {
                builder.append(base.charAt(i));
            }
        }
        if (base.endsWith(FORMAT_KEYWORD)){
            builder.deleteCharAt(builder.length() - 1);
        } else {
            builder.append('\'');
        }
        return builder.toString();
    }

    public String format(@NotNull ZonedDateTime timeDate) {
        return timeDate.format(getFormatter());
    }

    public ZonedDateTime parse(String text) throws DateTimeParseException {
        return ZonedDateTime.parse(text, getFormatter());
    }

    @NotNull
    private DateTimeFormatter getFormatter() {
        return formatter.withLocale(ConfigParser.getConfig().advanced.dateLanguage).withZone(ConfigParser.getConfig().advanced.dateTimezone);
    }
}
