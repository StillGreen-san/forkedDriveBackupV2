package ratismal.drivebackup.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JsonUtil {
    /**
     * get the {@link JSONObject} value associated with a key, ignoring case differences
     *
     * @param json object to lookup in
     * @param key  string to compare against
     * @return {@link JSONObject} which is the value
     * @throws JSONException if the key is not found or if the value is not a {@link JSONObject}.
     */
    @NotNull
    public static JSONObject getJsonObjectIgnoreCase(@NotNull JSONObject json, @NotNull String key)
        throws JSONException {
        return json.getJSONObject(findKeyIgnoreCase(json, key, key));
    }

    /**
     * tries to get the {@link JSONObject} associated with a key, ignoring case differences
     *
     * @param json object to lookup in
     * @param key  string to compare against
     * @return {@link JSONObject} or null if the value was not found or is not a {@link JSONObject}
     */
    @Nullable
    public static JSONObject optJsonObjectIgnoreCase(@NotNull JSONObject json, @NotNull String key) {
        return json.optJSONObject(findKeyIgnoreCase(json, key));
    }

    /**
     * tries to get the {@link JSONObject} associated with a key, ignoring case differences
     *
     * @param json object to lookup in
     * @param key  string to compare against
     * @param alt  object to return if not found
     * @return {@link JSONObject} or alt if the value was not found or is not a {@link JSONObject}
     */
    @NotNull
    public static JSONObject optJsonObjectIgnoreCase(@NotNull JSONObject json, @NotNull String key,
        @NotNull JSONObject alt) {
        return json.optJSONObject(findKeyIgnoreCase(json, key), alt);
    }

    /**
     * tries to get the {@link JSONArray} associated with a key, ignoring case differences
     *
     * @param json object to lookup in
     * @param key  string to compare against
     * @return {@link JSONArray} or null if the value was not found or is not a {@link JSONArray}
     */
    @Nullable
    public static JSONArray optJsonArrayIgnoreCase(@NotNull JSONObject json, @NotNull String key) {
        return json.optJSONArray(findKeyIgnoreCase(json, key));
    }

    /**
     * tries to get the {@link JSONArray} associated with a key, ignoring case differences, or alt if the value was not
     * found or is not a {@link JSONArray}
     *
     * @param json object to lookup in
     * @param key  string to compare against
     * @param alt  array to return if not found
     * @return {@link JSONArray} or alt if the value was not found or is not a {@link JSONArray}
     */
    @NotNull
    public static JSONArray optJsonArrayIgnoreCase(@NotNull JSONObject json, @NotNull String key,
        @NotNull JSONArray alt) {
        return json.optJSONArray(findKeyIgnoreCase(json, key), alt);
    }

    /**
     * tries to get the {@link String} associated with a key, ignoring case differences. if the value is not a string
     * and is not null, then it is converted to a string
     *
     * @param json object to lookup in
     * @param key  string to compare against
     * @return {@link String} or null if the value was not found
     */
    @Nullable
    public static String optStringIgnoreCase(@NotNull JSONObject json, @NotNull String key) {
        return json.optString(findKeyIgnoreCase(json, key), null);
    }

    /**
     * tries to get the {@link String} associated with a key, ignoring case differences. if the value is not a string
     * and is not null, then it is converted to a string
     *
     * @param json object to lookup in
     * @param key  string to compare against
     * @param alt  string to return if not found
     * @return {@link String} or alt if the value was not found
     */
    @NotNull
    public static String optStringIgnoreCase(@NotNull JSONObject json, @NotNull String key, @NotNull String alt) {
        return json.optString(findKeyIgnoreCase(json, key), alt);
    }

    /**
     * tries to find a key in json that matches the given key, ignoring case differences. an exact match is preferred.
     * otherwise the first case-insensitive match is returned, or alt if nothing is found.
     *
     * @param json object to search in
     * @param key  string to search for
     * @param alt  string to return if nothing is found
     * @return the matching key if found, or alt
     */
    @NotNull
    public static String findKeyIgnoreCase(@NotNull JSONObject json, @NotNull String key, @NotNull String alt) {
        String match = findKeyIgnoreCase(json, key);
        return match != null ? match : alt;
    }

    /**
     * tries to find a key in json that matches the given key, ignoring case differences. an exact match is preferred.
     * otherwise the first case-insensitive match is returned, or null if nothing is found.
     *
     * @param json object to search in
     * @param key  string to search for
     * @return the matching key if found, or null
     */
    @Nullable
    public static String findKeyIgnoreCase(@NotNull JSONObject json, @NotNull String key) {
        if (json.has(key)) {
            return key;
        }
        Iterator<String> keyIt = json.keys();
        while (keyIt.hasNext()) {
            String member = keyIt.next();
            if (member.equalsIgnoreCase(key)) {
                return member;
            }
        }
        return null;
    }
}
