package com.example.ebest.api;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class EXTFILE {

    String[] key = new String[2];
    public EXTFILE() {

    }

    public String[] read_key()
    {
        File file = new File(Environment.getExternalStorageDirectory(), "ebest/key.txt");
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder builder = new StringBuilder();
            String line;
            key[0] = reader.readLine();
            key[1] = reader.readLine();

            fis.close();
            Log.d("FileRead", builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return key;
    }

    public String[] readKey(String filename) {
        File ebestDir = new File(Environment.getExternalStorageDirectory(), "ebest");
        File file = new File(ebestDir, filename);

        StringBuilder text = new StringBuilder();
        if (file.exists()) {
            try {
                //BufferedReader br = new BufferedReader(new FileReader(file));
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                key[0] = br.readLine();
                key[1] = br.readLine();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
                //return "Error reading file: " + e.getMessage();
            }
        }
        return key;
    }

    public void request_permission()
    {

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                    != PackageManager.PERMISSION_GRANTED) {
//
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
//            }
//        }
    }

}
