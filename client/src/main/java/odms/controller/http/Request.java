package odms.controller.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

public class Request {

    private String urlString;
    private int token;
    private Map<String, String> queryParams;
    private String body;

    public Request(String urlString, int token, Map<String, String> queryParams, String body) {
        this.urlString = urlString;
        this.token = token;
        this.queryParams = queryParams;
        this.body = body;
    }

    public Request(String urlString, int token, Map<String, String> queryParams) {
        this.urlString = urlString;
        this.token = token;
        this.queryParams = queryParams;
    }

    public Response get() throws IOException {
        URL url = new URL(constructUrl(this.urlString, this.queryParams));
        //Creating the connection to the server.
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        JsonElement body = execute(con);
        int status = con.getResponseCode();

        con.disconnect();
        return new Response(this.token, body, status);
    }

    public Response post() throws IOException {
        URL url = new URL(constructUrl(this.urlString, this.queryParams));
        //Creating the connection to the server.
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        JsonElement body = execute(con);
        int status = con.getResponseCode();

        con.disconnect();
        return new Response(this.token, body, status);
    }

    public Response patch() throws IOException {
        URL url = new URL(constructUrl(this.urlString, this.queryParams));
        //Creating the connection to the server.
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PATCH");

        JsonElement body = execute(con);
        int status = con.getResponseCode();

        con.disconnect();
        return new Response(this.token, body, status);
    }

    public Response delete() throws IOException {
        URL url = new URL(constructUrl(this.urlString, this.queryParams));
        //Creating the connection to the server.
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");

        JsonElement body = execute(con);
        int status = con.getResponseCode();

        con.disconnect();
        return new Response(this.token, body, status);
    }

    private JsonElement execute(HttpURLConnection con) throws IOException {
        JsonParser parser = new JsonParser();

        con.setRequestProperty("Content-Type", "application/json");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        StringBuffer responseContent = new StringBuffer();
        BufferedReader response = new BufferedReader(
                new InputStreamReader(con.getInputStream()));

        String line = response.readLine();
        while (line != null) {
            responseContent.append(line);
            line = response.readLine();
        }
        JsonElement body = parser.parse(responseContent.toString());

        response.close();
        return body;
    }

    private String constructUrl(String urlString, Map<String, String> queryParams) {
        Object[] keys = queryParams.keySet().toArray();
        for (int i = 0; i < queryParams.size(); i++) {
            String key = (String) keys[i];
            if (i == 0) {
                urlString += ("?" + key + "=" + queryParams.get(key));
            }
            else {
                urlString += ("&" + key + "=" + queryParams.get(key));
            }
        }
        return urlString;
    }
}
