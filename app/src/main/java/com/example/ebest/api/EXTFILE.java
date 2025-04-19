package com.example.ebest.api;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class EXTFILE {


    public EXTFILE() {

    }

    public String[] read_key()
    {
        String[] key = new String[2];
        File file = new File(Environment.getExternalStorageDirectory(), "/ebest/key.txt");
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

    public ArrayList<String> read_stocklist()
    {

        ArrayList<String> stocklist = new ArrayList<String>();
        int i =0;
        File file = new File(Environment.getExternalStorageDirectory(), "/ebest/stocklist.txt");
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                stocklist.add(line);
                i++;
            }
            fis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return stocklist;
    }

    public void writeOHLCV(String stockcode, String[][] data)
    {
        String fileName = Environment.getExternalStorageDirectory() + "/ebest/" + stockcode +".csv";
        File file = new File(fileName);
        String header = "date,open,High,low,close,volume";

        // 없으면 생성하고, 있으면 덮어쓰고
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
            writer.write(header);
            writer.newLine();
            for (String[] row : data) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public int[] readOHLCV(String stockcode)
    {
        String fileName = Environment.getExternalStorageDirectory() + "/ebest/" + stockcode +".csv";
        String line;

        ArrayList<Integer> closeValues = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            // 첫 줄은 헤더이므로 건너뜁니다
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 5) {
                    try {
                        // 문자열 → double → int (소수점 버림)
                        int close = (int) Double.parseDouble(values[4]);
                        closeValues.add(close);
                    } catch (NumberFormatException e) {
                        System.err.println("숫자 파싱 오류: " + values[4]);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        // ArrayList를 int[] 배열로 변환
        int[] closeArray = closeValues.stream().mapToInt(i -> i).toArray();

        // 결과 출력
        System.out.println("Close 값 배열:");
        for (int value : closeArray) {
            System.out.println(value);
        }
        return closeArray;
    }
}
