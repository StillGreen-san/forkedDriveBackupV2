package ratismal.drivebackup.uploaders.onedrive;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import static ratismal.drivebackup.util.JsonUtil.getJsonObjectIgnoreCase;

/** microsoft graph api errorresponse; outer error object holding the actual error object */
public class GraphApiErrorResponse {
    private static final String ERROR_OBJ_KEY = "error";
    private static final String CODE_STR_KEY = "code";
    private static final String MESSAGE_STR_KEY = "message";

    /** The error object. */
    public final @NotNull GraphApiError error;

    /**
     * create the object from the given JSON string
     *
     * @param jsonString representing the error response
     */
    public GraphApiErrorResponse(@NotNull String jsonString) {
        this.error = parseGraphError(jsonString);
    }

    private static @NotNull GraphApiError parseGraphError(@NotNull String jsonString) {
        try {
            return new GraphApiError(getJsonObjectIgnoreCase(new JSONObject(jsonString), ERROR_OBJ_KEY));
        } catch (JSONException jsonException) {
            return new GraphApiError(new JSONObject().put(CODE_STR_KEY, "invalidErrorResponse")
                .putOpt(MESSAGE_STR_KEY, jsonException.getMessage()));
        }
    }
}
