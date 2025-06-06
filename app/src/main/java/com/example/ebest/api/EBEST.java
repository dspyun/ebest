package com.example.ebest.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


import javax.net.ssl.HttpsURLConnection;


public class EBEST  {

    String ACCESS_TOKEN="";
    private static final String HOST = "https://openapi.ls-sec.co.kr:8080/";
    private static final String TOKEN_URL = HOST + "oauth2/token";
    private static final String GRANT_TYPE = "client_credentials";
    private static final String ContentsType="application/json;charset=utf-8";
    private String APP_KEY = "";
    private String APP_SECRET = "";
    private static final String SCOPE = "oob";

    String[] key = new String[2];
    String todayDate;
    public EBEST() {
        EXTFILE extfile = new EXTFILE();
        key = extfile.read_key();
        APP_KEY = key[0];
        APP_SECRET = key[1];
        threadAccessToken();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        todayDate = today.format(formatter);
    }

    public String get_token()
    {
        return ACCESS_TOKEN;
    }
    public void strSplit(StringBuffer sb) throws JSONException {
        String temp=sb.toString();
        JSONObject jsonObject = new JSONObject(temp);
        ACCESS_TOKEN = jsonObject.getString("access_token");
        System.out.println(ACCESS_TOKEN); // apple
    }
    private void threadAccessToken() {
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

    HttpsURLConnection getConnection(URL url, String trade_code, String ContentsType)
    {
        HttpsURLConnection connection;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        connection.setRequestProperty("Content-Type", ContentsType);
        connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        connection.setRequestProperty("tr_cd", trade_code);
        connection.setRequestProperty("tr_cont", "N");
        connection.setRequestProperty("tr_cont_key", "");

        return connection;
    }


    String hoga()
    {
        // 주식 호가 조회 예제
        int hoga=0;
        String hoga_str="";

        // stock/market-data는 stock경로의 market-data db를 가르킴
        String tokenRequestUrl = HOST + "stock/market-data";

        try {
            // Create the URL object
            URL url = new URL(tokenRequestUrl);
            HttpsURLConnection connection = getConnection(url,"t1101",  ContentsType);

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
                    //Log.d("NetworkUtils", line);
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


    String codelist()
    {
        // 주식 호가 조회 예제
        int hoga=0;
        String hoga_str="";

        // stock/market-data는 stock경로의 market-data db를 가르킴
        String tokenRequestUrl = HOST + "stock/etc";

        try {
            // Create the URL object
            URL url = new URL(tokenRequestUrl);
            HttpsURLConnection connection = getConnection(url,"t8430",  ContentsType);

            JSONObject innerdata = new JSONObject();
            innerdata.put("gubun", "1");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("t8430InBlock", innerdata);

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
                    //Log.d("NetworkUtils", line);
                }
            }

            // Parse JSON response
            JSONObject jobj = new JSONObject(respStr.toString());
            JSONArray bodyobj = jobj.getJSONArray("t8430OutBlock");

            int read_count = bodyobj.length();
            String[][] chart_data = new String[read_count][2];
            for(int i =0;i<read_count;i++) {
                chart_data[i][0] = bodyobj.getJSONObject(i).getString("shcode");
                chart_data[i][1] = bodyobj.getJSONObject(i).getString("hname");
            }
            EXTFILE extfile = new EXTFILE();
            extfile.writeCodelist(chart_data);
            return "OK";
        } catch (Exception e) {
            Log.e("YourTag", "Error occurred", e);
        }
        return "";
    }

    // return string : "name, price"
    String current(String code)
    {
        // 주식 현재가 조회 예제

        // stock/market-data는 stock경로의 market-data db를 가르킴
        String tokenRequestUrl = HOST + "stock/market-data";
        String result ="";

        try {
            // Create the URL object
            URL url = new URL(tokenRequestUrl);
            HttpsURLConnection connection = getConnection(url,"t1102",  ContentsType);

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
                return result;
            }

            Log.d("NetworkUtils", "Request body: " + jsonObject.toString());

            // Read the response
            StringBuilder respStr = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    respStr.append(line);
                    //Log.d("NetworkUtils", line);
                }
            }

            // Parse JSON response
            JSONObject jobj = new JSONObject(respStr.toString());
            JSONObject bodyobj = jobj.getJSONObject("t1102OutBlock");

            result = bodyobj.getString("hname") + ",";
            result += bodyobj.getString("price");

            Log.d("NetworkUtils", "현재가 " + result);

        } catch (Exception e) {
            Log.e("YourTag", "Error occurred", e);
        }
        return result;
    }


    String chart_day(int count, String code)
    {
        // 주식 일봉차트 조회 예제

        // stock/market-data는 stock경로의 market-data db를 가르킴
        String tokenRequestUrl = HOST + "stock/chart";
        String[][] chart_data = new String[count][6];
        chart_data[0][0]="";
        //while(ACCESS_TOKEN.isBlank()){ };

        try {
            // Create the URL object
            URL url = new URL(tokenRequestUrl);
            HttpsURLConnection connection = getConnection(url,"t8410",  ContentsType);

            JSONObject innerdata = new JSONObject();
            innerdata.put("shcode", code);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("t8410InBlock", innerdata);
            innerdata.put("qrycnt", count);
            jsonObject.put("t8410InBlock", innerdata);

            String[][] inData = {{"gubun","2"},{"sdate",""},{"edate",todayDate},
                    {"cts_date",todayDate}, {"comp_yn","N"}, {"sujung","Y"}};
            int len = inData.length;
            for(int i =0;i<len;i++)
            {
                innerdata.put(inData[i][0], inData[i][1]);
                jsonObject.put("t8410InBlock", innerdata);
            }

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
                    //Log.d("NetworkUtils", line);
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
                chart_data[i][0] = bodyobj.getJSONObject(i).getString("date");
                chart_data[i][1] = bodyobj.getJSONObject(i).getString("open");
                chart_data[i][2] = bodyobj.getJSONObject(i).getString("high");
                chart_data[i][3] = bodyobj.getJSONObject(i).getString("low");
                chart_data[i][4] = bodyobj.getJSONObject(i).getString("close");
                chart_data[i][5] = bodyobj.getJSONObject(i).getString("jdiff_vol");
            }

            EXTFILE extfile = new EXTFILE();
            extfile.writeOHLCV(code,chart_data);
            Log.d("NetworkUtils", "날짜 : " + date);

        } catch (Exception e) {
            Log.e("YourTag", "Error occurred", e);
            return "";
        }
        return "ok";
    }

    public String stock_info_fng(String code)
    {
        // 주식 재무정보 조회 예제
        // stock/market-data는 stock경로의 market-data db를 가르킴
        String tokenRequestUrl = HOST + "stock/investinfo";
        String information;

        try {
            // Create the URL object
            URL url = new URL(tokenRequestUrl);
            HttpsURLConnection connection = getConnection(url,"t3320",  ContentsType);

            JSONObject innerdata = new JSONObject();
            innerdata.put("gicode", code);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("t3320InBlock", innerdata);

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
                    //Log.d("NetworkUtils", line);
                }
            }

            // Parse JSON response
            JSONObject jobj = new JSONObject(respStr.toString());
            JSONObject bodyobj00 = jobj.getJSONObject("t3320OutBlock");
            JSONObject bodyobj01 = jobj.getJSONObject("t3320OutBlock1");

            information = "회사명 : " + bodyobj00.getString("company") + ","+"\n";
            information += "배당률 : " + bodyobj00.getString("cashrate") + ","+"\n";
            information += "현재가 : " + bodyobj00.getString("price") + ","+"\n";
            information += "pbr : " + bodyobj01.getString("pbr") + ","+"\n";
            information += "roa : " + bodyobj01.getString("roa") + ","+"\n";
            information += "per : " + bodyobj01.getString("per") + ","+"\n";
            information += "eps : " + bodyobj01.getString("eps") + ","+"\n";
            information += "roe : " + bodyobj01.getString("roe") + ","+"\n";
            information += "ebitda : " + bodyobj01.getString("ebitda") + ","+"\n";
            information += "cps : " + bodyobj01.getString("cps") + ","+"\n";
            information += "bps : " + bodyobj01.getString("bps") + ","+"\n";
            //EXTFILE extfile = new EXTFILE();
            //extfile.writeOHLCV(code+"min",chart_data);
            //Log.d("NetworkUtils", "날짜 : " + date + ", 종가 : " + hoga);

        } catch (Exception e) {
            Log.e("YourTag", "Error occurred", e);
            return "";
        }
        return information;
    }


    public String stock_profit_ranking(String code)
    {
        // 주식 재무정보랭킹 조회 예제
        // stock/market-data는 stock경로의 market-data db를 가르킴
        String tokenRequestUrl = HOST + "stock/investinfo";
        String information="";

        try {
            // Create the URL object
            URL url = new URL(tokenRequestUrl);
            HttpsURLConnection connection = getConnection(url,"t3341",  ContentsType);

            JSONObject innerdata = new JSONObject();
            innerdata.put("gicode", code);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("t3341InBlock", innerdata);

            String[][] inData = {{"gubun","0"},{"gubun1","2"},{"gubun2","1"}};
            int len = inData.length;
            for(int i =0;i<len;i++)
            {
                innerdata.put(inData[i][0], inData[i][1]);
                jsonObject.put("t3341InBlock", innerdata);
            }
            innerdata.put("idx", 0);
            jsonObject.put("t3341InBlock", innerdata);

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
                    //Log.d("NetworkUtils", line);
                }
            }

            // Parse JSON response
            JSONObject jobj = new JSONObject(respStr.toString());
            JSONArray bodyobj = jobj.getJSONArray("t3341OutBlock1");

            int read_count = bodyobj.length();
            for(int i =0;i<read_count;i++) {
                information += bodyobj.getJSONObject(i).getString("rank") + " ";
                information += bodyobj.getJSONObject(i).getString("hname") + " ";
                information += bodyobj.getJSONObject(i).getString("shcode") + "\n";
                information += "영업이익증가율 " + bodyobj.getJSONObject(i).getString("operatingincomegrowt") + ", ";
                information += "매출액증가율 " +bodyobj.getJSONObject(i).getString("salesgrowth");
                information += "\n";
                information += "eps " +bodyobj.getJSONObject(i).getString("eps") + ", ";
                information += "bps " +bodyobj.getJSONObject(i).getString("bps") + ", ";
                information += "roe " +bodyobj.getJSONObject(i).getString("roe") + "\n";
                information += "per " +bodyobj.getJSONObject(i).getString("per") + ", ";
                information += "pbr " +bodyobj.getJSONObject(i).getString("pbr") + ", ";
                information += "peg " +bodyobj.getJSONObject(i).getString("peg") + "\n";
                information += "--------------------------------\n";
            }

            //EXTFILE extfile = new EXTFILE();
            //extfile.writeOHLCV(code+"min",chart_data);
            Log.d("NetworkUtils", "날짜 : " + ", 종가 : " );

        } catch (Exception e) {
            Log.e("YourTag", "Error occurred", e);
            return "";
        }
        return information;
    }


    public StringBuilder news_body(String realkey)
    {
        // 주식 뉴스 조회 예제
        String tokenRequestUrl = HOST + "stock/investinfo";
        StringBuilder htmlBuilder = new StringBuilder();
        try {
            // Create the URL object
            URL url = new URL(tokenRequestUrl);
            HttpsURLConnection connection = getConnection(url,"t3102",  ContentsType);

            JSONObject innerdata = new JSONObject();
            innerdata.put("sNewsno", realkey);
            JSONObject root = new JSONObject();
            root.put("t3102InBlock",innerdata);

            byte[] body01 = root.toString().getBytes();
            connection.setFixedLengthStreamingMode(body01.length);
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(body01);
            outputStream.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                Log.e("NetworkUtils", "HTTP error code: " + responseCode);
                return htmlBuilder;
            }

            Log.d("NetworkUtils", "Request body: " + root.toString());

            // Read the response
            StringBuilder respStr = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    respStr.append(line);
                    //Log.d("NetworkUtils", line);
                }
            }

            // Parse JSON response
            JSONObject jobj = new JSONObject(respStr.toString());
            JSONArray bodyArray = jobj.getJSONArray("t3102OutBlock1");

            htmlBuilder = new StringBuilder();
            for (int i = 0; i < bodyArray.length(); i++) {
                JSONObject obj = bodyArray.getJSONObject(i);
                htmlBuilder.append(obj.getString("sBody"));
            }


        } catch (Exception e) {
            Log.e("YourTag", "Error occurred", e);
            return htmlBuilder;
        }
        return htmlBuilder;
    }
    String chart_minute(int count, String code)
    {
        // 주식 분봉차트 조회 예제

        // stock/market-data는 stock경로의 market-data db를 가르킴
        String tokenRequestUrl = HOST + "stock/chart";
        String[][] chart_data = new String[count][6];
        chart_data[0][0]="";
        //while(ACCESS_TOKEN.isBlank()){ };

        try {
            // Create the URL object
            URL url = new URL(tokenRequestUrl);
            HttpsURLConnection connection = getConnection(url,"t8412",  ContentsType);

            JSONObject innerdata = new JSONObject();
            innerdata.put("shcode", code);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("t8412InBlock", innerdata);
            innerdata.put("ncnt", 1);
            jsonObject.put("t8412InBlock", innerdata);
            innerdata.put("qrycnt", count);
            jsonObject.put("t8412InBlock", innerdata);

            String[][] inData = {{"nday","0"},{"sdate",todayDate},{"stime","090000"},
                    {"edate",todayDate},{"etime","153000"},
                    {"cts_date",todayDate},{"cts_time","090000"},
                    {"comp_yn","N"}};
            int len = inData.length;
            for(int i =0;i<len;i++)
            {
                innerdata.put(inData[i][0], inData[i][1]);
                jsonObject.put("t8412InBlock", innerdata);
            }

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
                    //Log.d("NetworkUtils", line);
                }
            }

            // Parse JSON response
            JSONObject jobj = new JSONObject(respStr.toString());
            JSONObject bodyobj = jobj.getJSONObject("t8412OutBlock");
            int rec_count = bodyobj.getInt("rec_count");
            if(rec_count==0) return "";
            JSONArray bodyobj_1 = jobj.getJSONArray("t8412OutBlock1");
            //String date = bodyobj.getJSONObject(0).getString("date");
            //hoga_str = bodyobj.getJSONObject(0).getString("close");
            int read_count = bodyobj_1.length();
            for(int i =0;i<read_count;i++) {
                chart_data[i][0] = bodyobj_1.getJSONObject(i).getString("time");
                chart_data[i][1] = bodyobj_1.getJSONObject(i).getString("open");
                chart_data[i][2] = bodyobj_1.getJSONObject(i).getString("high");
                chart_data[i][3] = bodyobj_1.getJSONObject(i).getString("low");
                chart_data[i][4] = bodyobj_1.getJSONObject(i).getString("close");
                chart_data[i][5] = bodyobj_1.getJSONObject(i).getString("jdiff_vol");
            }

            EXTFILE extfile = new EXTFILE();
            extfile.writeOHLCV(code+"min",chart_data);
            //Log.d("NetworkUtils", "날짜 : " + date + ", 종가 : " + hoga);

        } catch (Exception e) {
            Log.e("YourTag", "Error occurred", e);
            return "";
        }
        return "ok";
    }


    int[][] chartBuyer(int count, String code)
    {
        int hoga=0;
        String hoga_str="";
        // 주식 차트 조회 예제

        // stock/market-data는 stock경로의 market-data db를 가르킴
        String tokenRequestUrl = HOST + "stock/chart";
        int[][] chart_data = new int[3][count];
        chart_data[0][0]=0;
        //while(ACCESS_TOKEN.isBlank()){ };

        try {
            // Create the URL object
            URL url = new URL(tokenRequestUrl);
            HttpsURLConnection connection = getConnection(url,"t1665",  ContentsType);

            JSONObject innerdata = new JSONObject();
            innerdata.put("market", "1");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("t1665InBlock", innerdata);

            String[][] inData = {{"upcode","001"},{"gubun2","1"},{"gubun3","1"},
                    {"from_date","20250101"},{"to_date",todayDate},
                    {"exchgubun","K"}};
            int len = inData.length;
            for(int i =0;i<len;i++)
            {
                innerdata.put(inData[i][0], inData[i][1]);
                jsonObject.put("t1665InBlock", innerdata);
            }

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
                    //Log.d("NetworkUtils", line);
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

    public String threadHoGa(String code)
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



}

