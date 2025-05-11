package com.example.ebest.ui.common;

import static androidx.core.content.res.ResourcesCompat.getColor;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.example.ebest.R;
import com.example.ebest.api.EXTFILE;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.HashMap;

public class CHART {

    EXTFILE extfile = new EXTFILE();
    ArrayList<String> stocklist_in_selectedFile;
    String SelectedFile;
    Context context;

    public CHART(Context inputontext, String inputFile) {
        context = inputontext;
        SelectedFile = inputFile;
        stocklist_in_selectedFile = extfile.read_stocklist(SelectedFile);
    }

    public void draw_linechart(int day_count, LineChart chart, int[] prices, String stockname)
    {
        ArrayList<Entry> entries = new ArrayList<>();
        int end, start, length;
        // 타겟일수가 데이터길이보다 작으면 타겟일수를 보여준다
        // 이 때, 시작날짜는 데이터길이-타겟일수 이다
        // 타겟일수가 데이터길이보다 크면 데이터길이만큼만 보여준다
        if(day_count < prices.length) {
            start = prices.length - day_count;
            end = prices.length;
            length = end-start;
        }
        else {
            start = 0;
            end = prices.length;
            length = end;
        }
        for (int i = 0; i < length; i++) {
            entries.add(new Entry(i, prices[i+start]));  // X = index, Y = value
        }

        LineDataSet dataSet = new LineDataSet(entries, stockname);
        dataSet.setValueTextColor(Color.YELLOW);
        int tealColor = ContextCompat.getColor(context, R.color.teal_700);
        dataSet.setColor(tealColor);
        dataSet.setDrawValues(false); // hide datavalue
        //dataSet.setValueTextSize(12f);
        dataSet.setDrawCircles(false);
        chart.getLegend().setTextColor(Color.YELLOW); // stock prices

        String percent = "percent";
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setText(percent);
        chart.getDescription().setTextColor(Color.YELLOW);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        chart.getAxisLeft().setEnabled(true);        // 왼쪽 Y축 활성화
        chart.getAxisLeft().setDrawLabels(true);     // 값(숫자) 표시
        //chart.getAxisLeft().setTextSize(12f);        // 텍스트 크기
        chart.getAxisLeft().setTextColor(Color.YELLOW); // 텍스트 색상
        chart.getAxisRight().setTextColor(Color.YELLOW); // 텍스트 색상
        chart.getXAxis().setTextColor(Color.YELLOW); // 텍스트 색상

        chart.animateX(1000);
    }
    public String percent_value(float yesterday_price, float c_price)
    {
        float result = 100*yesterday_price/c_price;
        return String.valueOf((int)result)+"%";
    }


    public void addChartInfoDetail(LineChart chart, String stockcode, String stockname, String buy_price)
    {
        float max_price = Float.MIN_VALUE;

        LineData lineData = chart.getData();
        LineDataSet dataSet = (LineDataSet) lineData.getDataSetByIndex(0); // 첫 번째 데이터셋

        // get last price
        int entry_size = dataSet.getEntryCount();
        Entry entry = dataSet.getEntryForIndex(entry_size-1); // 인덱스 마지막 엔트리
        float last_price = entry.getY();

        // get max price
        if (lineData != null && lineData.getDataSetCount() > 0) {
            for (int i = 0; i < dataSet.getEntryCount(); i++) {
                float yy = dataSet.getEntryForIndex(i).getY();
                if (yy > max_price) {
                    max_price = yy;
                }
            }
        }

        String percent = percent_value(last_price,max_price);
        chart.getLegend().setTextColor(Color.YELLOW); // stock prices
        String info = stockname + " (" + stockcode + ") " + Integer.toString((int)last_price) + " " + percent;
        info += "(buy price : "+buy_price+")";
        chart.getDescription().setText(info);
        dataSet.setLabel(info);
        lineData = new LineData(dataSet);
        chart.setData(lineData);
    }

    public void addChartInfo(LinearLayout container) {
        HashMap<String, String> currentPriceMap = new HashMap<>();
        ArrayList<String> buypricelist = new ArrayList<>();
        buypricelist = extfile.read_buyprice(SelectedFile);

        for (int i =0;i<stocklist_in_selectedFile.size();i++) {
            String stockcode = stocklist_in_selectedFile.get(i);
            String stockname = extfile.findStockName(stockcode);
            LineChart chart = (LineChart) container.getChildAt(i);

            addChartInfoDetail(chart,stockcode, stockname, buypricelist.get(i));
            chart.invalidate(); // 차트 갱신
        }
        container.invalidate();
    }

    public void addChart(Context context, LinearLayout container, int day_count, int[] prices, String stockname) {
        LineChart chart = new LineChart(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500
        );
        chart.setLayoutParams(params);
        draw_linechart(day_count, chart, prices, stockname);
        chart.invalidate(); // 차트 갱신
        container.addView(chart);
    }
}
