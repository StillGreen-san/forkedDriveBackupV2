package ratismal.drivebackup.util;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

import ratismal.drivebackup.config.ConfigParser;

public class HttpLogger implements Interceptor {
    private static final MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8");
    
    @Override
    public @NotNull Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        if (!ConfigParser.getConfig().advanced.debugEnabled) {
            return chain.proceed(request);
        }
        Response response = handleSendReceive(chain, request);
        try {
            handleRequest(response.request());
            return handleResponse(response);
        } catch (Throwable throwable) {
            response.close();
            throw throwable;
        }
    }

    /**
     * logs the response body
     * @return a new equivalent response
     * @throws IOException if the response body could not be read as string
     */
    private static @NotNull Response handleResponse(@NotNull Response response) throws IOException {
        String bodyString;
        MediaType bodyContentType;
        try (ResponseBody responseBody = response.body()) {
            if (responseBody == null) {
                sendToConsole("Resp: No Body");
                return response.newBuilder().build();
            }
            bodyContentType = responseBody.contentType();
            bodyString = responseBody.string();
        }
        if (jsonMediaType.equals(bodyContentType)) {
            try {
                JSONObject bodyJson = new JSONObject(bodyString);
                if ("code_not_authenticated".equals(bodyJson.optString("msg"))) {
                    return response.newBuilder().body(ResponseBody.create(bodyString, bodyContentType)).build();
                }
                sendToConsole("Resp: " + bodyJson);
            } catch (JSONException exception) { // bodyJson construction failed
                sendToConsole("Resp: " + bodyString);
            }
        } else {
            sendToConsole("Resp: " + bodyString);
        }
        return response.newBuilder().body(ResponseBody.create(bodyString, bodyContentType)).build();
    }

    /**
     * logs the request body
     */
    private static void handleRequest(@NotNull Request request) {
        try {
            RequestBody requestBody = request.body();
            if (requestBody == null) {
                sendToConsole("Req: No Body");
            } else if (jsonMediaType.equals(requestBody.contentType())) {
                Buffer bodyBuffer = new Buffer();
                requestBody.writeTo(bodyBuffer);
                sendToConsole("Req: " + bodyBuffer.readUtf8());
            } else {
                sendToConsole("Req: unsupported content type " + requestBody.contentType());
            }
        } catch (Exception exception) {
            sendToConsole("Req: Error reading request body");
        }
    }

    /**
     * proceeds with the request, logging request url and response time
     * @return the response
     * @throws IOException when an error occurs in the Interceptor.Chain
     */
    private static @NotNull Response handleSendReceive(@NotNull Interceptor.Chain chain, @NotNull Request request) throws IOException {
        long startTime = System.nanoTime();
        sendToConsole("Sending request " + request.url());
        Response response = chain.proceed(request);
        long endTime = System.nanoTime();
        sendToConsole(String.format("Received response for %s in %.1fms", response.request().url(), (endTime - startTime) / 1e6d));
        return response;
    }
    
    private static void sendToConsole(@NotNull String message) {
        MessageUtil.Builder().text(message).toConsole(true).send();
    }
}
