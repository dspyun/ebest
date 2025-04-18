package com.example.ebest.api;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.ebest.MainActivity;

import org.json.JSONArray;
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
    private String APP_KEY = "";
    private String APP_SECRET = "";
    private static final String SCOPE = "oob";

    String[] key = new String[2];
    public EBEST() {
        EXTFILE extfile = new EXTFILE();
        key = extfile.read_key();
        APP_KEY = key[0];
        APP_SECRET = key[1];
        fetchAccessToken();
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
                        Log.d("ok", "Token Response: " + buffer.toString());
                        strSplit(buffer);

                        //get_hoga();
                    } else {
                        Log.d("error", "Token Response: ");
                    }
                } catch (Exception e) {
                    Log.e("exception", "Error occurred", e);
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


    String current(String code)
    {
        int price=0;
        String price_str="";
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
            connection.setRequestProperty("tr_cd", "t1102");
            connection.setRequestProperty("tr_cont", "N");
            connection.setRequestProperty("tr_cont_key", "");

            JSONObject innerdata = new JSONObject();
            innerdata.put("shcode", code);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("t1102InBlock", innerdata);

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
            JSONObject bodyobj = jobj.getJSONObject("t1102OutBlock");

            price_str = bodyobj.getString("hname");
            price_str += " : ";
            price_str += bodyobj.getString("price");
            price = bodyobj.getInt("price");

            Log.d("NetworkUtils", " 현재가 " + price);

        } catch (Exception e) {
            Log.e("YourTag", "Error occurred", e);
        }
        return price_str;
    }
    public String fetchAccessHoGa(String code)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String result="";
        Callable<String> task = () -> {
            String hoga_str="";
            //hoga_str = hoga();
            //chart();
            hoga_str = current(code);
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

    public String fetchAccessCurrent(String code)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String result="";
        Callable<String> task = () -> {
            String current_str="";
            current_str = current(code);
            //chart();
            return current_str;
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


    public int[] fetchAccessChart(int count, String code) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        int[] result = new int[1];
        result[0] = 0;
        int day_count = count;
        Callable<int[]> task = () -> {
            int[] chart_data = new int[500];
            chart_data = chart(day_count, code);
            return chart_data;
        };

        Future<int[]> future = executor.submit(task);
        try {
            result = future.get(); // Blocking call (waits for thread to finish)
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("fail to get chart"); // Output: Data received!

        executor.shutdown();
        return result;
    }

    int[] chart(int count, String code)
    {
        int hoga=0;
        String hoga_str="";
        // 주식 차트 조회 예제
        String ContentsType="application/json;charset=utf-8";
        // stock/market-data는 stock경로의 market-data db를 가르킴
        String tokenRequestUrl = HOST + "stock/chart";
        int[] chart_data = new int[count];
        chart_data[0]=0;
        while(ACCESS_TOKEN.isBlank()){ };

        try {
            // Create the URL object
            URL url = new URL(tokenRequestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", ContentsType);
            connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            connection.setRequestProperty("tr_cd", "t8410");
            connection.setRequestProperty("tr_cont", "Y");
            connection.setRequestProperty("tr_cont_key", "");

            JSONObject innerdata = new JSONObject();
            innerdata.put("shcode", code);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("t8410InBlock", innerdata);
            innerdata.put("gubun", "2");
            jsonObject.put("t8410InBlock", innerdata);
            innerdata.put("qrycnt", count);
            jsonObject.put("t8410InBlock", innerdata);
            innerdata.put("sdate", "20250101");
            jsonObject.put("t8410InBlock", innerdata);
            innerdata.put("edate", "25250330");
            jsonObject.put("t8410InBlock", innerdata);
            innerdata.put("cts_date", "20250330");
            jsonObject.put("t8410InBlock", innerdata);
            innerdata.put("comp_yn", "N");
            jsonObject.put("t8410InBlock", innerdata);
            innerdata.put("sujung", "Y");
            jsonObject.put("t8410InBlock", innerdata);

            byte[] body = jsonObject.toString().getBytes();
            connection.setFixedLengthStreamingMode(body.length);
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(body);
            outputStream.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                Log.e("NetworkUtils", "HTTP error code: " + responseCode);
                return chart_data;
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
            //JSONObject bodyobj = jobj.getJSONObject("t8410OutBlock");
            //String stockName = bodyobj.getString("hname");
            JSONArray bodyobj = jobj.getJSONArray("t8410OutBlock1");
            String date = bodyobj.getJSONObject(0).getString("date");
            //hoga_str = bodyobj.getJSONObject(0).getString("close");
            int read_count = bodyobj.length();
            for(int i =0;i<read_count;i++) {
                chart_data[i] = bodyobj.getJSONObject(i).getInt("close");
            }

            Log.d("NetworkUtils", "날짜 : " + date + ", 종가 : " + hoga);

        } catch (Exception e) {
            Log.e("YourTag", "Error occurred", e);
        }
        return chart_data;
    }


    public int[][] fetchAccessChart3(int count, String code) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        int[][] result = new int[1][1];
        result[0][0] = 0;
        int day_count = count;
        Callable<int[][]> task = () -> {
            int[][] chart_data = new int[3][500];
            chart_data = chart3(day_count, code);
            return chart_data;
        };

        Future<int[][]> future = executor.submit(task);
        try {
            result = future.get(); // Blocking call (waits for thread to finish)
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("fail to get chart"); // Output: Data received!

        executor.shutdown();
        return result;
    }

    int[][] chart3(int count, String code)
    {
        int hoga=0;
        String hoga_str="";
        // 주식 차트 조회 예제
        String ContentsType="application/json;charset=utf-8";
        // stock/market-data는 stock경로의 market-data db를 가르킴
        String tokenRequestUrl = HOST + "stock/chart";
        int[][] chart_data = new int[3][count];
        chart_data[0][0]=0;
        while(ACCESS_TOKEN.isBlank()){ };

        try {
            // Create the URL object
            URL url = new URL(tokenRequestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", ContentsType);
            connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            connection.setRequestProperty("tr_cd", "t1665");
            connection.setRequestProperty("tr_cont", "Y");
            connection.setRequestProperty("tr_cont_key", "");

            JSONObject innerdata = new JSONObject();
            innerdata.put("market", "1");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("t1665InBlock", innerdata);
            innerdata.put("upcode", "001");
            jsonObject.put("t1665InBlock", innerdata);
            innerdata.put("gubun2", "1");
            jsonObject.put("t1665InBlock", innerdata);
            innerdata.put("gubun3", "1");
            jsonObject.put("t1665InBlock", innerdata);
            innerdata.put("from_date", "20250101");
            jsonObject.put("t1665InBlock", innerdata);
            innerdata.put("to_date", "20250330");
            jsonObject.put("t1665InBlock", innerdata);
            innerdata.put("exchgubun", "K");
            jsonObject.put("t1665InBlock", innerdata);

            byte[] body = jsonObject.toString().getBytes();
            connection.setFixedLengthStreamingMode(body.length);
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(body);
            outputStream.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                Log.e("NetworkUtils", "HTTP error code: " + responseCode);
                return chart_data;
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
            //JSONObject bodyobj = jobj.getJSONObject("t8410OutBlock");
            //String stockName = bodyobj.getString("hname");
            JSONArray bodyobj = jobj.getJSONArray("t1665OutBlock1");
            count = bodyobj.length();
            for(int i =0;i<count;i++) {
                chart_data[0][i] = bodyobj.getJSONObject(i).getInt("sv_08"); // person
                chart_data[1][i] = bodyobj.getJSONObject(i).getInt("sv_17"); // foreign
                chart_data[2][i] = bodyobj.getJSONObject(i).getInt("sv_18"); // compamy
            }

            Log.d("NetworkUtils", "날짜 : ");

        } catch (Exception e) {
            Log.e("YourTag", "Error occurred", e);
        }
        return chart_data;
    }
}

