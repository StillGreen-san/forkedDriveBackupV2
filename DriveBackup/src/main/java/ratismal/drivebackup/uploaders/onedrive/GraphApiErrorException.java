package ratismal.drivebackup.uploaders.onedrive;

import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.io.IOException;

/**
 * an exception representing a microsoft graph api error
 */
public class GraphApiErrorException extends Exception {
    /** status code of the response or -1 if not available */
    public final int statusCode;
    /** The error object. */
    public final GraphApiError error;

    /**
     * create the exception from a response
     *
     * @param response to parse error from its body
     * @throws IOException          if the body string could not be loaded
     * @throws NullPointerException if the body could not be loaded
     * @throws JSONException        if the body does not contain the expected json values
     */
    public GraphApiErrorException(@NotNull Response response) throws IOException {
        this(response.code(), response.body().string());
    }

    /**
     * create the exception from a status code and response body
     *
     * @param statusCode   of the response
     * @param responseBody of the response
     * @throws JSONException if the body does not contain the expected json values
     */
    public GraphApiErrorException(int statusCode, @NotNull String responseBody) {
        this(statusCode, new GraphApiErrorResponse(responseBody).error);
    }

    public Exception verbose() {
        return new VerboseException();
    }

    private GraphApiErrorException(int statusCode, @NotNull GraphApiError error) {
        super(error.header(statusCode));
        this.statusCode = statusCode;
        this.error = error;
    }

    private class VerboseException extends Exception {
        private @Nullable String message;

        @Override
        public String getMessage() {
            if (message == null) {
                StringBuilder sb = new StringBuilder(error.header(statusCode));
                error.content(sb, new StringBuilder("\n"));
                message = sb.toString();
            }
            return message;
        }
    }
}
