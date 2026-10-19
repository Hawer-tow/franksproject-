package com.example.mobiledbclient;

import android.content.Context;
import android.util.Log;

import com.example.mobiledbclient.Login.CryptoUtils;
import com.example.mobiledbclient.Login.LoginRequest;
import com.example.mobiledbclient.Login.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://geomagnetic-confessedly-juanita.ngrok-free.dev/";
    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            Context appContext = context.getApplicationContext();

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(new SecurityInterceptor(appContext))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService(Context context) {
        return getClient(context).create(ApiService.class);
    }

    private static class SecurityInterceptor implements Interceptor {
        private final Context context;
        private final Gson gson = new Gson();

        SecurityInterceptor(Context context) {
            this.context = context;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            String path = request.url().encodedPath();
            String method = request.method();

            // 1. Skip encryption for key handshake
            if (path.contains("/api/public-key")) {
                return chain.proceed(request);
            }

            // 2. Ensure we have the server public key
            String serverPubKeyStr = SessionManager.getServerPublicKey(context);
            if (serverPubKeyStr == null || serverPubKeyStr.isEmpty()) {
                return chain.proceed(request);
            }

            try {
                // 3. Extract Payload (Actual JSON from body or query parameters)
                String plaintext = null;
                RequestBody body = request.body();

                if (("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))) {
                    plaintext = bodyToString(body);

                    // FALLBACK: If body is empty or just "{}", use the DTO from the request tag
                    if (plaintext == null || plaintext.isEmpty() || "{}".equals(plaintext.trim())) {
                        Object tag = request.tag(LoginRequest.class);
                        if (tag == null) {
                            tag = request.tag();
                        }
                        
                        if (tag != null && !(tag instanceof Request)) {
                            plaintext = gson.toJson(tag);
                        }
                    }
                } else {
                    // For GET/DELETE: build JSON from parameters
                    HttpUrl url = request.url();
                    if (url.querySize() > 0) {
                        JsonObject json = new JsonObject();
                        for (int i = 0; i < url.querySize(); i++) {
                            // ✅ FIXED: Using correct queryParameterName and queryParameterValue
                            json.addProperty(url.queryParameterName(i), url.queryParameterValue(i));
                        }
                        plaintext = json.toString();
                    }
                }

                // 4. Perform Encryption & Signing
                byte[] aesKey = CryptoUtils.generateAESKey();
                byte[] iv = CryptoUtils.generateIV();
                PublicKey serverPublicKey = CryptoUtils.getPublicKeyFromString(serverPubKeyStr);
                PrivateKey clientPrivateKey = CryptoUtils.getClientPrivateKey();

                Request.Builder builder = request.newBuilder()
                        .header("X-Encrypted-Key", CryptoUtils.toBase64(CryptoUtils.encryptRSA(aesKey, serverPublicKey)))
                        .header("X-IV", CryptoUtils.toBase64(iv))
                        .header("X-Device-Id", SessionManager.getDeviceId(context));

                int userId = SessionManager.getActiveUser(context);
                if (userId != -1) {
                    builder.header("X-User-Id", String.valueOf(userId));
                }

                // 5. Attach Payload Headers ONLY if data exists
                if (plaintext == null || plaintext.isEmpty()) {
                    // Fallback: send empty JSON object so server always has something to decrypt/verify
                    plaintext = "{}";
                }

                Log.d("ApiClient", "Encrypting payload: " + plaintext);
                byte[] encryptedPayload = CryptoUtils.encryptAES(plaintext, aesKey, iv);
                builder.header("X-Encrypted-Payload", CryptoUtils.toBase64(encryptedPayload));

                byte[] signature = CryptoUtils.sign(plaintext, clientPrivateKey);
                builder.header("X-Signature", CryptoUtils.toBase64(signature));


                // 6. Clean up transport data
                if ("GET".equals(method) || "DELETE".equals(method)) {
                    // Do not remove query parameters
                    builder.method(method, null); // no body for GET/DELETE
                } else {
                    builder.method(method, RequestBody.create(MediaType.parse("application/json"), "{}"));
                }


                Response response = chain.proceed(builder.build());

                // 7. Security Checks
                if (response.code() == 403 || response.code() == 401) {
                    checkSecurityWipe(response);
                    return response;
                }

                // 8. Auto Decryption
                return decryptResponse(response);

            } catch (Exception e) {
                Log.e("ApiClient", "Security processing failed", e);
                throw new IOException("Security layer error: " + e.getMessage());
            }
        }

        private void checkSecurityWipe(Response response) {
            try {
                ResponseBody responseBody = response.peekBody(2048);
                String bodyString = responseBody.string();
                JsonObject json = new Gson().fromJson(bodyString, JsonObject.class);
                if (json != null && json.has("code") && json.get("code").getAsInt() == 40301) {
                    DataWipeManager.wipeAndLogout(context);
                }
            } catch (Exception ignored) {}
        }

        private Response decryptResponse(Response response) throws IOException {
            String encKeyBase64 = response.header("X-Encrypted-Key");
            String ivBase64 = response.header("X-IV");
            if (encKeyBase64 == null || ivBase64 == null) return response;

            try {
                PrivateKey clientPrivateKey = CryptoUtils.getClientPrivateKey();
                byte[] encryptedAesKey = CryptoUtils.fromBase64(encKeyBase64);
                byte[] iv = CryptoUtils.fromBase64(ivBase64);
                byte[] aesKey = CryptoUtils.decryptRSA(encryptedAesKey, clientPrivateKey);

                ResponseBody body = response.body();
                if (body == null) return response;

                byte[] encryptedPayload = body.bytes();
                String decryptedPlaintext = CryptoUtils.decryptAES(encryptedPayload, aesKey, iv);

                return response.newBuilder()
                        .body(ResponseBody.create(body.contentType(), decryptedPlaintext))
                        .build();
            } catch (Exception e) {
                return response;
            }
        }

        private String bodyToString(RequestBody body) {
            try {
                if (body == null) return "";
                Buffer buffer = new Buffer();
                body.writeTo(buffer);
                return buffer.readUtf8();
            } catch (IOException e) {
                return "";
            }
        }
    }

    @Deprecated
    public static Retrofit getClient() {
        return retrofit;
    }
}
