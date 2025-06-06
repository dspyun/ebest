package com.example.ebest.ui.onedepth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ebest.R;
import com.example.ebest.api.DLOAD;
import com.example.ebest.api.EBEST;
import com.example.ebest.datamodel.NewsItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
public class news extends AppCompatActivity {

    private OkHttpClient client;
    private WebSocket webSocket;
    String NEWS_HOST = "wss://openapi.ls-sec.co.kr:9443";
    //String NEWS_HOST = " https://openapi.ls-sec.co.kr:29443";

    String tokenRequestUrl = NEWS_HOST + "/websocket";
    String access_token="";

    TextView tvNews;
    Button btNews;

    String news_contents="";
    private ListView listView;
    private ArrayList<String> items;  // 실제 데이터 리스트

    private ArrayList<NewsItem> newsList;
    private NewsAdapter adapter;
    EBEST ebest = new EBEST();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_news);

        Intent intent = getIntent();
        access_token = intent.getStringExtra("token");

        btNews = findViewById(R.id.btRaedNews);
        tvNews = findViewById(R.id.tvNews);

        startWebSocket();
        btNews.setText("실시간뉴스가져오기 On");

        listView = findViewById(R.id.listView);
        newsList = new ArrayList<>();
        adapter = new NewsAdapter(this, newsList);
        listView.setAdapter(adapter);

        // 클릭 리스너
        listView.setOnItemClickListener((parent, view, position, id) -> {
            NewsItem clickedItem = newsList.get(position);
            DLOAD dload = new DLOAD();
            StringBuilder news_body = dload.threadNewsBody(clickedItem.getkey());

            showNewsPopup(news.this, news_body);
            //Toast.makeText(this, news_body, Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, "key: " + clickedItem.getkey(), Toast.LENGTH_SHORT).show();
        });

        btNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebSocket();
                btNews.setText("실시간뉴스가져오기 On");
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void startWebSocket() {
        client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(tokenRequestUrl)  // ws:// or wss://
                .build();

        webSocket = client.newWebSocket(request, new EchoWebSocketListener());
    }

    private void addNewsItem(String title, String realkey) {
        newsList.add(new NewsItem(title, realkey));
        adapter.notifyDataSetChanged();
    }

    private final class EchoWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);

            try {
                JSONObject header = new JSONObject();
                header.put("token", access_token);
                header.put("tr_type", "3");

                JSONObject body = new JSONObject();
                body.put("tr_cd", "NWS");
                body.put("tr_key", "NWS001");

                JSONObject root = new JSONObject();
                root.put("header", header);
                root.put("body", body);

                webSocket.send(root.toString());
                System.out.println("메시지 전송: " + root.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            System.out.println("Received message: " + text);

            try {
                JSONObject response = new JSONObject(text);
                // 필요한 데이터 파싱
                JSONObject body = response.getJSONObject("body");
                //tvNews.setText(news_contents);
                String rcv_text = body.getString("title");
                String realkey = body.getString("realkey");
                runOnUiThread(() -> {
                    addNewsItem(rcv_text,realkey);
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
            System.out.println("Received bytes: " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(1000, null);
            System.out.println("Closing: " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            t.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Activity destroyed");
        }
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
    }

    public class NewsAdapter extends ArrayAdapter<NewsItem> {

        public NewsAdapter(@NonNull Context context, @NonNull List<NewsItem> items) {
            super(context, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            NewsItem item = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_news, parent, false);
            }

            TextView textView = convertView.findViewById(R.id.textTitle);
            textView.setText(item != null ? item.getTitle() : "");

            return convertView;
        }
    }

    public void showNewsPopup(Context context, StringBuilder news_body) {
        StringBuilder htmlBuilder = new StringBuilder();

        WebView webView = new WebView(context);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadDataWithBaseURL(null, news_body.toString(), "text/html", "UTF-8", null);

        new AlertDialog.Builder(context)
                .setTitle("뉴스 상세 보기")
                .setView(webView)
                .setPositiveButton("닫기", null)
                .show();
    }
}