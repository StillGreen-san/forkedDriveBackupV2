package ratismal.drivebackup.uploaders.onedrive;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import static ratismal.drivebackup.util.JsonUtil.optStringIgnoreCase;
import static ratismal.drivebackup.util.JsonUtil.optJsonObjectIgnoreCase;

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
        innerError.remove(CODE_STR_KEY);
        innerError.remove(INNERERROR_OBJ_KEY);
        this.contents = innerError;
    }
}
