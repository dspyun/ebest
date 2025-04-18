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


    public EXTFILE() {

    }

    public String[] read_key()
    {
        String[] key = new String[2];
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

    public String[] read_stocklist()
    {
        String[] stocklist = new String[30];
        int i =0;
        File file = new File(Environment.getExternalStorageDirectory(), "ebest/stocklist.txt");
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                stocklist[i] = line;
                i++;
            }
            fis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return stocklist;
    }

}
