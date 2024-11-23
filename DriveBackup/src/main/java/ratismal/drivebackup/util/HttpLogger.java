package ratismal.drivebackup.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
import okio.ByteString;

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
     * @throws IOException if the response body could not be loaded into memory
     */
    private static @NotNull Response handleResponse(@NotNull Response response) throws IOException {
        ByteString bodyBytes;
        MediaType bodyContentType;
        try (ResponseBody responseBody = response.body()) {
            if (responseBody == null) {
                sendToConsole("Resp: No Body");
                return response.newBuilder().build();
            }
            bodyContentType = responseBody.contentType();
            bodyBytes = responseBody.byteString();
            responseBody.source();
            ResponseBody.create(bodyBytes, bodyContentType).string();
        }
        if (jsonMediaType.equals(bodyContentType)) {
            String bodyString;
            try {
                bodyString = bodyBytes.string(getCharset(bodyContentType));
            } catch (Exception e) {
                sendToConsole("Resp: Error reading response body as string");
                return response.newBuilder().body(ResponseBody.create(bodyBytes, bodyContentType)).build();
            }
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
            sendToConsole("Resp: " + bodyBytes);
        }
        return response.newBuilder().body(ResponseBody.create(bodyBytes, bodyContentType)).build();
    }

    /**
     * logs the request body
     */
    private static void handleRequest(@NotNull Request request) {
        try {
            RequestBody requestBody = request.body();
            if (requestBody == null) {
                sendToConsole("Req: No Body");
                return;
            }
            MediaType bodyContentType = requestBody.contentType();
            Charset charset = bodyContentType != null ? bodyContentType.charset(null) : null;
            if (charset == null) {
                sendToConsole("Req: ");
            }
            Buffer bodyBuffer = new Buffer();
            requestBody.writeTo(bodyBuffer);
            sendToConsole("Req: " + bodyBuffer.readString(getCharset(requestBody.contentType())));
        } catch (Exception exception) {
            sendToConsole("Req: Error reading request body as string");
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

    /**
     * @return the charset for the body content type or UTF8
     */
    private static @NotNull Charset getCharset(MediaType bodyContentType) {
        Charset charset = bodyContentType != null ? bodyContentType.charset(null) : null;
        return charset != null ? charset : StandardCharsets.UTF_8;
    }
}
