package com.example.ebest.ui.onedepth;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ebest.R;
import com.example.ebest.api.DLOAD;

public class Inform extends AppCompatActivity {

    TextView tvInformation;
    Button btReadInfo;
    String stockcode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inform);

        tvInformation = findViewById(R.id.tvInform);
        btReadInfo = findViewById(R.id.btRaedInform);

        Intent intent = getIntent();
        stockcode = intent.getStringExtra("stock_code");

        tvInformation.setMovementMethod(new ScrollingMovementMethod());
        tvInformation.setText(stockcode);

        btReadInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DLOAD dload = new DLOAD(stockcode);
                String information = dload.threadInformation(stockcode);
                tvInformation.setText(information);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}