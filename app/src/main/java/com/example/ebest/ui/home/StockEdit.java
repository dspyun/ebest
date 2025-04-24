package com.example.ebest.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ebest.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class StockEdit extends AppCompatActivity {

    //private static final String FILE_NAME = "key.txt";

    //File file = new File(Environment.getExternalStorageDirectory(), "/ebest/stocklist.txt");
    private EditText editText;
    String FILENAME;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stockedit);

        editText = findViewById(R.id.editText);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);

        Intent intent = getIntent();
        FILENAME = intent.getStringExtra("file_name");

        // 파일 내용 불러오기
        loadTextFile();

        // 저장 버튼
        btnSave.setOnClickListener(v -> {
            saveTextFile();
            finish(); // 현재 액티비티 종료
        });

        // 취소 버튼
        btnCancel.setOnClickListener(v -> {
            finish(); // 저장하지 않고 종료
        });
    }

    private void loadTextFile() {

        String FILE_NAME = Environment.getExternalStorageDirectory()+"/ebest/" + FILENAME;
        File file = new File(FILE_NAME);

        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                reader.close();
                fis.close();

                editText.setText(builder.toString());  // EditText에 내용 표시

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "파일 읽기 오류", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTextFile() {
        String FILE_NAME = Environment.getExternalStorageDirectory()+"/ebest/"+ FILENAME;
        String text = editText.getText().toString();
        File file = new File(FILE_NAME);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(text.getBytes());
            fos.close();
            Toast.makeText(this, "파일 저장 완료: " + file.getPath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "파일 저장 실패", Toast.LENGTH_SHORT).show();
        }
    }
}
