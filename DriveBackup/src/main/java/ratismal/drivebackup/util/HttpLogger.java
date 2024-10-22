package ratismal.drivebackup.util;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
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
        ResponseBody responseBody = response.body();
        String responseBodyString = responseBody.string();
        MediaType responseBodyContentType = responseBody.contentType();
        responseBody.close();
        if (responseBodyContentType.equals(jsonMediaType)) {
            try {
                JSONObject responseBodyJson = new JSONObject(responseBodyString);
                if (responseBodyJson.getString("msg").equals("code_not_authenticated")) {
                    return response.newBuilder().body(ResponseBody.create(responseBodyString, responseBodyContentType)).build();
                }
                MessageUtil.Builder().text("Resp: " + responseBodyJson).toConsole(true).send();
            } catch (Exception exception) {
                MessageUtil.Builder().text("Resp: " + responseBodyString).toConsole(true).send();
            }
        } else {
            MessageUtil.Builder().text("Resp: " + responseBodyString).toConsole(true).send();
        }
        return response.newBuilder().body(ResponseBody.create(responseBodyString, responseBodyContentType)).build();
    }

    /**
     * logs the request body, if present
     */
    private static void handleRequest(@NotNull Request request) {
        try {
            if (request.body().contentType().equals(jsonMediaType)) {
                Buffer requestBody = new Buffer();
                request.body().writeTo(requestBody);
                MessageUtil.Builder().text("Req: " + requestBody.readUtf8()).toConsole(true).send();
            } else {
                MessageUtil.Builder().text("Req: Not JSON").toConsole(true).send();
            }
        } catch (Exception exception) {
            MessageUtil.Builder().text("Req: None").toConsole(true).send();
        }
    }

    /**
     * proceeds with the request, logging request url and response time
     * @return the response
     * @throws IOException when an error occurs in the Interceptor.Chain
     */
    private static @NotNull Response handleSendReceive(@NotNull Interceptor.Chain chain, @NotNull Request request) throws IOException {
        long startTime = System.nanoTime();
        MessageUtil.Builder().text(String.format("Sending request %s", request.url())).toConsole(true).send();
        Response response = chain.proceed(request);
        long endTime = System.nanoTime();
        MessageUtil.Builder().text(String.format("Received response for %s in %.1fms", response.request().url(), (endTime - startTime) / 1e6d)).toConsole(true).send();
        return response;
    }
}
