package com.example.ebest.api;
import static java.lang.Thread.sleep;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class EXTFILE {


    public EXTFILE() {

    }

    public String[] read_key()
    {
        String[] key = new String[2];
        File file = new File(Environment.getExternalStorageDirectory(), "/ebest/key04.txt");
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

    public ArrayList<String> read_stocklist(String filename)
    {

        ArrayList<String> resultArray = new ArrayList<String>();
        File file = new File(Environment.getExternalStorageDirectory(), "/ebest/" + filename);
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                resultArray.add(values[0]);
            }
            fis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public ArrayList<String> read_buyprice(String filename)
    {

        ArrayList<String> resultArray = new ArrayList<String>();
        File file = new File(Environment.getExternalStorageDirectory(), "/ebest/" + filename);
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if(values.length > 1) {
                    resultArray.add(values[1]);
                } else {
                    resultArray.add("0");
                }
            }
            fis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public void writeOHLCV(String stockcode, String[][] data)
    {
        String fileName = Environment.getExternalStorageDirectory() + "/ebest/data/" + stockcode +".csv";
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
        String fileName = Environment.getExternalStorageDirectory() + "/ebest/data/" + stockcode +".csv";
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
                        System.err.println(stockcode + " 숫자 파싱 오류: " + values[4]);
                        return closeValues.stream().mapToInt(i -> i).toArray();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        // ArrayList를 int[] 배열로 변환
        return closeValues.stream().mapToInt(i -> i).toArray();
    }


    public void writeCodelist(String[][] data)
    {
        String fileName = Environment.getExternalStorageDirectory() + "/ebest/codelist.csv";
        File file = new File(fileName);
        String header = "code,hname";

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


    public ArrayList<String> readCodelist()
    {
        String fileName = Environment.getExternalStorageDirectory() + "/ebest/codelist.csv";

        ArrayList<String> stocklist = new ArrayList<String>();
        File file = new File(fileName);
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line="";
            // 첫줄은 헤드라인이므로 건너뜀
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                stocklist.add(values[0]);
            }
            fis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return stocklist;
    }

    public String findStockName(String targetCode)
    {
        String fileName = Environment.getExternalStorageDirectory() + "/ebest/codelist.csv";
        String line;
        HashMap<String, String> stockMap = new HashMap<>();

            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            // 첫 줄(헤더)을 읽고 버림
            String header = br.readLine();

            while ((line = br.readLine()) != null) {
                // 각 라인을 쉼표로 분할
                String[] values = line.split(",", -1);
                if (values.length >= 2) {
                    String code = values[0].trim();
                    String name = values[1].trim();
                    stockMap.put(code, name);
                }
            }
        } catch (IOException e) {
            System.err.println("파일 읽기 오류: " + e.getMessage());
            return "";
        }

        // 검색 예시
        String resultString;
        if (stockMap.containsKey(targetCode)) {
            resultString = stockMap.get(targetCode);
            System.out.println("주식이름: " + resultString);
        } else {
            System.out.println("해당 주식코드를 찾을 수 없습니다: " + targetCode);
            resultString = "";
        }
        return resultString;
    }
}
