package ratismal.drivebackup.uploaders.onedrive;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static ratismal.drivebackup.util.JsonUtil.optStringIgnoreCase;
import static ratismal.drivebackup.util.JsonUtil.optJsonObjectIgnoreCase;
import static ratismal.drivebackup.util.JsonUtil.optJsonArrayIgnoreCase;

/** microsoft graph api error; the actual error object containing error information */
public class GraphApiError {
    private static final String CODE_STR_KEY = "code";
    private static final String MESSAGE_STR_KEY = "message";
    private static final String TARGET_STR_KEY = "target";
    private static final String DETAILS_ARR_KEY = "details";
    private static final String INNERERROR_OBJ_KEY = "innererror";

    /** One of a server-defined set of error codes. */
    public final @NotNull String code;
    /** A human-readable representation of the error. */
    public final @NotNull String message;
    /** The target of the error. */
    public final @Nullable String target;
    /** An array of details about specific errors that led to this reported error. */
    public final @NotNull List<GraphApiError> details;
    /** An object containing more specific information than the current object about the error. */
    public final @Nullable GraphApiInnerError innerError;

    /**
     * create the object from the given JSON string
     *
     * @param error object representing the error
     */
    public GraphApiError(@NotNull JSONObject error) {
        this.code = optStringIgnoreCase(error, CODE_STR_KEY, "invalid/missing member 'code'");
        this.message = optStringIgnoreCase(error, MESSAGE_STR_KEY, "invalid/missing member 'message'");
        this.target = optStringIgnoreCase(error, TARGET_STR_KEY);
        this.details = new ArrayList<>();
        JSONArray details = optJsonArrayIgnoreCase(error, DETAILS_ARR_KEY, new JSONArray());
        for (Object detail : details) {
            if (detail instanceof JSONObject) {
                this.details.add(new GraphApiError((JSONObject) detail));
            } else {
                this.details.add(
                    new GraphApiError("invalid 'details' entry", "'details' entries must be a JSONObject"));
            }
        }
        JSONObject maybeInnerError = optJsonObjectIgnoreCase(error, INNERERROR_OBJ_KEY);
        this.innerError = maybeInnerError != null ? new GraphApiInnerError(maybeInnerError) : null;
    }

    public String header(int statusCode) {
        return String.format("%d %s : \"%s\"", statusCode, this.code, this.message);
    }

    public void content(StringBuilder sb, StringBuilder prefix) {
        sb.append(prefix).append('{');
        prefix.append('\t');
        sb.append(prefix).append("code: ").append(code);
        sb.append(prefix).append("message: ").append(message);
        if (target != null) {
            sb.append(prefix).append("target: ").append(target);
        }
        if (!details.isEmpty()) {
            sb.append(prefix).append("details: [");
            details.forEach(detail -> {
                detail.content(sb, prefix);
            });
            sb.append(prefix).append(']');
        }
        if (innerError != null) {
            innerError.content(sb, prefix);
        }
        prefix.deleteCharAt(prefix.length() - 1);
        sb.append(prefix).append('}');
    }

    private GraphApiError(@NotNull String code, @NotNull String message) {
        this.code = code;
        this.message = message;
        this.target = null;
        this.details = new ArrayList<>();
        this.innerError = null;
    }
}
