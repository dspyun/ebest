package com.example.ebest.api;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class DLOAD {


    ArrayList<String> stocklist;
    EXTFILE extfile = new EXTFILE();
    EBEST ebest = new EBEST();


    public DLOAD( )
    {

    }
    public DLOAD(String stockgroup)
    {
        stocklist = extfile.read_stocklist(stockgroup);
    }
    public Boolean check_dload_finish(HashMap<String, String> stockMap, ArrayList<String> codelist)
    {
        for (String stock : codelist) {
            if (stockMap.get(stock).isBlank()) {
                return false;
            }
        }
        return true;
    }

    public HashMap<String, String> threadCurrentPriceList() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        HashMap<String, String> resultMap1 = new HashMap<>();

        Callable<HashMap<String, String>> task = () -> {
            HashMap<String, String> resultMap = new HashMap<>();
            // hashmap 초기화 : stock을 key로, value는 응답값으로
            for (String stock : stocklist) resultMap.put(stock, "");
            int size = stocklist.size();
            int down_count=0;

            while (!check_dload_finish(resultMap, stocklist)) {
                for (String stock : stocklist) {
                    if (resultMap.get(stock).isBlank()) {
                        // return value : "stockname,current price"
                        String result1 = ebest.current(stock);
                        if(result1.isBlank()) {
                            Thread.sleep(1000);
                            continue;
                        } else {
                            resultMap.put(stock, result1);
                            Log.d("dload", "download " + down_count + "/" + size);
                            down_count++;
                        }
                    }
                }
            }
            return resultMap;
        };

        Future<HashMap<String, String>> future = executor.submit(task);
        try {
            resultMap1 = future.get(); // Blocking call (waits for thread to finish)
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("fail to get chart"); // Output: Data received!

        executor.shutdown();
        return resultMap1;
    }


    public String threadChartMinList(int count) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String result="";
        Callable<String> task = () -> {
            HashMap<String, String> resultMap = new HashMap<>();
            // hashmap 초기화 : stock을 key로, value는 응답값으로
            for (String stock : stocklist) resultMap.put(stock, "");
            int size = stocklist.size();
            int down_count=0;

            while (!check_dload_finish(resultMap, stocklist)) {
                for (String stock : stocklist) {
                    if (resultMap.get(stock).isBlank()) {
                        String result1 = ebest.chart_minute(count,stock);;
                        if(result1.isBlank()) {
                            Thread.sleep(1000);
                            continue;
                        } else {
                            resultMap.put(stock, result1);
                            Log.d("dload", "download " + down_count + "/" + size);
                            down_count++;
                        }
                    }
                }
            }
            return "ok";
        };

        Future<String> future = executor.submit(task);
        try {
            result = future.get(); // Blocking call (waits for thread to finish)
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("fail to get chart"); // Output: Data received!

        executor.shutdown();
        return result;
    }

    public String threadChartDayList(int count) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String result="";
        Callable<String> task = () -> {
            HashMap<String, String> resultMap = new HashMap<>();
            // hashmap 초기화 : stock을 key로, value는 응답값으로
            for (String stock : stocklist) resultMap.put(stock, "");
            int size = stocklist.size();
            int down_count=0;

            while (!check_dload_finish(resultMap, stocklist)) {
                for (String stock : stocklist) {
                    if (resultMap.get(stock).isBlank()) {
                        String result1 = ebest.chart_day(count, stock);
                        if(result1.isBlank()) {
                            Thread.sleep(1000);
                            continue;
                        } else {
                            resultMap.put(stock, result1);
                            Log.d("dload", "download " + down_count + "/" + size);
                            down_count++;
                        }
                    }
                }
            }
            return "ok";
        };

        Future<String> future = executor.submit(task);
        try {
            result = future.get(); // Blocking call (waits for thread to finish)
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("fail to get chart"); // Output: Data received!

        executor.shutdown();
        return result;
    }

    public String threadFulllist() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String result = "";
        Callable<String> task = () -> {
            for (;;) {
                String result1 = ebest.codelist();
                if (result1.isBlank()) continue;
                else break;
            }
            return "ok";
        };

        Future<String> future = executor.submit(task);
        try {
            result = future.get(); // Blocking call (waits for thread to finish)
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("fail to get chart"); // Output: Data received!
        executor.shutdown();
        return "";
    }


    public String threadFulllistChart(int count) {
        ArrayList<String> codelist = extfile.readCodelist();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String result="";
        Callable<String> task = () -> {
            HashMap<String, String> resultMap = new HashMap<>();
            // hashmap 초기화 : stock을 key로, value는 응답값으로
            for (String stock : codelist) resultMap.put(stock, "");
            int size = codelist.size();
            int down_count=0;

            while (!check_dload_finish(resultMap, codelist)) {
                for (String stock : codelist) {
                    if (resultMap.get(stock).isBlank()) {
                        String result1 = ebest.chart_day(count, stock);
                        if(result1.isBlank()) {
                            Thread.sleep(1000);
                            continue;
                        } else {
                            resultMap.put(stock, result1);
                            Log.d("dload", "download " + down_count + "/" + size);
                            down_count++;
                        }
                    }
                }
            }
            return "ok";
        };

        Future<String> future = executor.submit(task);
        try {
            result = future.get(); // Blocking call (waits for thread to finish)
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("fail to get chart"); // Output: Data received!

        executor.shutdown();
        return result;
    }

    public String threadInformation(String stockcode) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String result = "";
        Callable<String> task = () -> {
            String inform="";
            for(int i =0;i<10;i++) {
                inform = ebest.stock_info_fng(stockcode);
                if(!inform.isEmpty()) break;
            }
            return inform;
        };

        Future<String> future = executor.submit(task);
        try {
            result = future.get(); // Blocking call (waits for thread to finish)
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("fail to get chart"); // Output: Data received!
        executor.shutdown();
        return result;
    }


    public String threadProfitRanking(String stockcode) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String result = "";
        Callable<String> task = () -> {
            String fin_info="";
            for(int i =0;i<10;i++) {
                fin_info = ebest.stock_profit_ranking(stockcode);
                if(!fin_info.isEmpty()) break;
            }
            return fin_info;
        };

        Future<String> future = executor.submit(task);
        try {
            result = future.get(); // Blocking call (waits for thread to finish)
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("fail to get chart"); // Output: Data received!
        executor.shutdown();
        return result;
    }


    public StringBuilder threadNewsBody(String keycode)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        StringBuilder result= new StringBuilder();
        Callable<StringBuilder> task = () -> {
            StringBuilder body = new StringBuilder();;
            for(int i =0;i<10;i++) {
                body = ebest.news_body(keycode);
                if(body.length()>0) break;
            }
            return body;
        };

        Future<StringBuilder> future = executor.submit(task);

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
