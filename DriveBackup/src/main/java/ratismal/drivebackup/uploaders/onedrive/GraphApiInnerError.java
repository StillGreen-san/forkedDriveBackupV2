package ratismal.drivebackup.uploaders.onedrive;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import static ratismal.drivebackup.util.JsonUtil.optStringIgnoreCase;
import static ratismal.drivebackup.util.JsonUtil.optJsonObjectIgnoreCase;
import static ratismal.drivebackup.util.JsonUtil.removeAllIgnoreCase;

/** microsoft graph api innererror; containing more specific service-defined error information */
public class GraphApiInnerError {
    private static final String CODE_STR_KEY = "code";
    private static final String INNERERROR_OBJ_KEY = "innererror";

    /** A more specific error code than was provided by the containing error. */
    public final @Nullable String code;
    /** An object containing more specific information than the current object about the error. */
    public final @Nullable GraphApiInnerError innerError;
    /** Service-defined contents */
    public final @NotNull JSONObject contents;

    /**
     * create the object from the given JSON string
     * 
     * @param innerError object representing the inner error
     */
    public GraphApiInnerError(@NotNull JSONObject innerError) {
        this.code = optStringIgnoreCase(innerError, CODE_STR_KEY);
        JSONObject maybeInnerError = optJsonObjectIgnoreCase(innerError, INNERERROR_OBJ_KEY);
        this.innerError = maybeInnerError != null ? new GraphApiInnerError(maybeInnerError) : null;
        removeAllIgnoreCase(innerError, CODE_STR_KEY);
        removeAllIgnoreCase(innerError, INNERERROR_OBJ_KEY);
        this.contents = innerError;
    }

    public void content(StringBuilder sb, StringBuilder prefix) {
        sb.append(prefix).append("innerError: ");
        sb.append(prefix).append('{');
        prefix.append('\t');
        sb.append(prefix).append("code: ").append(code);
        contents.keySet().forEach(key -> {
            sb.append(prefix).append(key).append(": ").append(contents.optString(key));
        });
        if (innerError != null) {
            innerError.content(sb, prefix);
        }
        prefix.deleteCharAt(prefix.length() - 1);
        sb.append(prefix).append('}');
    }
}
