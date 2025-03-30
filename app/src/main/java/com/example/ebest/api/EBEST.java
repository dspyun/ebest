package com.example.ebest.api;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.ebest.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
public class EBEST  {

    String ACCESS_TOKEN="";
    private static final String HOST = "https://openapi.ls-sec.co.kr:8080/";
    private static final String TOKEN_URL = HOST + "oauth2/token";
    private static final String GRANT_TYPE = "client_credentials";
    private static final String APP_KEY = "";
    private static final String APP_SECRET = "";
    private static final String SCOPE = "oob";
    public EBEST() {
        //dl_token();
        fetchAccessToken();
        //fetchAccessHoGa();
        //ACCESS_TOKEN = getAccessToken();
    }
    public void strSplit(StringBuffer sb) throws JSONException {
        String temp=sb.toString();
        JSONObject jsonObject = new JSONObject(temp);
        ACCESS_TOKEN = jsonObject.getString("access_token");
        System.out.println(ACCESS_TOKEN); // apple
    }
    private void fetchAccessToken() {
        // Create a new thread to make the network request
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Prepare the URL and request parameters
                    String tokenUrl = "https://openapi.ls-sec.co.kr:8080/oauth2/token"; // Replace with actual URL

                    // Prepare POST data
                    String postData = "appkey=" + APP_KEY +
                            "&appsecretkey=" + APP_SECRET +
                            "&grant_type=" + GRANT_TYPE +
                            "&scope=" + SCOPE;

                    // Create the URL object
                    URL url = new URL(tokenUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    // Set request properties
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    // Write POST data to output stream
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(postData.getBytes());
                    outputStream.flush();

                    // Get the response code
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read the response
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        final StringBuffer buffer = new StringBuffer();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            buffer.append(line);
                        }
                        Log.d("ok","Token Response: " + buffer.toString());
                        strSplit(buffer);

                        //get_hoga();
                    } else {
                        Log.d("error","Token Response: ");
                    }
                } catch (Exception e) {
                    Log.e("exception", "Error occurred", e);
                }

            }
        }).start();  // Start the thread
    }
    public void fetchAccessHoGa() {
        // thread로 access_token 및 hoga를 가져오기 때문에
        // token을 가져오기 전에 hoga를 실행할 수 있다
        // 그래서 token을 가져올 때까지 기다린다
        while(ACCESS_TOKEN.isBlank()){ };
        // Create a new thread to make the network request
        new Thread(new Runnable() {
            @Override
            public void run() {
                int hoga=0;
                // 주식 호가 조회 예제
                String ContentsType="application/json;charset=utf-8";
                // stock/market-data는 stock경로의 market-data db를 가르킴
                String tokenRequestUrl = HOST + "stock/market-data";

                try {
                    // Create the URL object
                    URL url = new URL(tokenRequestUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", ContentsType);
                    connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
                    connection.setRequestProperty("tr_cd", "t1101");
                    connection.setRequestProperty("tr_cont", "N");
                    connection.setRequestProperty("tr_cont_key", "");

                    JSONObject innerdata = new JSONObject();
                    innerdata.put("shcode", "078020");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("t1101InBlock", innerdata);

                    byte[] body = jsonObject.toString().getBytes();
                    connection.setFixedLengthStreamingMode(body.length);
                    connection.setDoOutput(true);

                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(body);
                    outputStream.flush();

                    int responseCode = connection.getResponseCode();
                    if (responseCode != 200) {
                        Log.e("NetworkUtils", "HTTP error code: " + responseCode);
                        return;
                    }

                    Log.d("NetworkUtils", "Request body: " + jsonObject.toString());

                    // Read the response
                    StringBuilder respStr = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            respStr.append(line);
                            Log.d("NetworkUtils", line);
                        }
                    }

                    // Parse JSON response
                    JSONObject jobj = new JSONObject(respStr.toString());
                    JSONObject bodyobj = jobj.getJSONObject("t1101OutBlock");
                    String stockName = bodyobj.getString("hname");
                    hoga = bodyobj.getInt("offerho1");

                    Log.d("NetworkUtils", stockName + " 1단계호가 " + hoga);

                } catch (Exception e) {
                    Log.e("YourTag", "Error occurred", e);
                }
            }
        }).start();  // Start the thread
    }

    String hoga()
    {
        int hoga=0;
        String hoga_str="";
        // 주식 호가 조회 예제
        String ContentsType="application/json;charset=utf-8";
        // stock/market-data는 stock경로의 market-data db를 가르킴
        String tokenRequestUrl = HOST + "stock/market-data";

        while(ACCESS_TOKEN.isBlank()){ };

        try {
            // Create the URL object
            URL url = new URL(tokenRequestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", ContentsType);
            connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            connection.setRequestProperty("tr_cd", "t1101");
            connection.setRequestProperty("tr_cont", "N");
            connection.setRequestProperty("tr_cont_key", "");

            JSONObject innerdata = new JSONObject();
            innerdata.put("shcode", "078020");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("t1101InBlock", innerdata);

            byte[] body = jsonObject.toString().getBytes();
            connection.setFixedLengthStreamingMode(body.length);
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(body);
            outputStream.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                Log.e("NetworkUtils", "HTTP error code: " + responseCode);
                return "";
            }

            Log.d("NetworkUtils", "Request body: " + jsonObject.toString());

            // Read the response
            StringBuilder respStr = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    respStr.append(line);
                    Log.d("NetworkUtils", line);
                }
            }

            // Parse JSON response
            JSONObject jobj = new JSONObject(respStr.toString());
            JSONObject bodyobj = jobj.getJSONObject("t1101OutBlock");
            String stockName = bodyobj.getString("hname");
            hoga_str = bodyobj.getString("offerho1");
            hoga = bodyobj.getInt("offerho1");

            Log.d("NetworkUtils", stockName + " 1단계호가 " + hoga);

        } catch (Exception e) {
            Log.e("YourTag", "Error occurred", e);
        }
        return hoga_str;
    }


    public String example_exec()
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String result="";
        Callable<String> task = () -> {
            String hoga_str="";
            hoga_str = hoga();
            Thread.sleep(2000); // Simulating work
            return hoga_str;
        };

        Future<String> future = executor.submit(task);

        try {
            result = future.get(); // Blocking call (waits for thread to finish)
            System.out.println(result); // Output: Data received!
        } catch (InterruptedException | ExecutionException e) {
            Log.e("YourTag", "Error occurred", e);
        }

        executor.shutdown();
        return result;
    }
}
