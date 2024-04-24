package ratismal.drivebackup.util;

import static ratismal.drivebackup.config.Localization.intl;

import java.net.UnknownHostException;

public class NetUtil {
    public static void catchException(Exception exception, String domain) {
        Logger logger = (input, placeholders) ->
                MessageUtil.Builder().mmText(input, placeholders).send();

        catchException(exception, domain, logger);
    }

    public static void catchException(Exception exception, String domain, Logger logger) {
        if (!(exception instanceof UnknownHostException)) {
            return;
        }

        logger.log(intl("connection-error"), "domain", domain);
    }
}
