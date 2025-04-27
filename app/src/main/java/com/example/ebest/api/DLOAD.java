package com.example.ebest.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;




public class DLOAD {


    ArrayList<String> stocklist;
    EXTFILE extfile = new EXTFILE();
    EBEST ebest = new EBEST();


    public DLOAD(String stockgroup)
    {
        stocklist = extfile.read_stocklist(stockgroup);
    }

    public HashMap<String, String> CurrentPriceList()
    {

        HashMap<String, String> resultMap = new HashMap<>();
        // hashmap 초기화 : stock을 key로, value는 응답값으로
        for (String stock : stocklist) resultMap.put(stock, "");
        // 결과값이 다 채워질때까지 계속 다운로드한다
        while (!check_dload_finish(resultMap)){
            for (String stock : stocklist) {
                if (resultMap.get(stock).isBlank()) {
                    resultMap.put(stock, ebest.threadCurrent(stock));
                }
            }
        }
        return resultMap;
    }


    public HashMap<String, String> chartminList(int count) {
        HashMap<String, String> resultMap = new HashMap<>();
        // hashmap 초기화 : stock을 key로, value는 응답값으로
        for (String stock : stocklist) resultMap.put(stock, "");
        // 다 채워질때까지 계속 다운로드 한다
        while (!check_dload_finish(resultMap)) {
            for (String stock : stocklist) {
                if (resultMap.get(stock).isBlank()) {
                    try {
                        resultMap.put(stock, ebest.threadChartMin(count, stock));
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return resultMap;
    }

    public HashMap<String, String> chartList(int count) {
        HashMap<String, String> resultMap = new HashMap<>();
        // hashmap 초기화 : stock을 key로, value는 응답값으로
        for (String stock : stocklist) resultMap.put(stock, "");
        // 다 채워질때까지 계속 다운로드 한다
        while (!check_dload_finish(resultMap)) {
            for (String stock : stocklist) {
                if (resultMap.get(stock).isBlank()) {
                    try {
                        resultMap.put(stock, ebest.threadChart(count, stock));
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return resultMap;
    }
    public Boolean check_dload_finish(HashMap<String, String> stockMap)
    {
        for (String stock : stocklist) {
            if (stockMap.get(stock).isBlank()) {
                return false;
            }
        }
        return true;
    }

}
