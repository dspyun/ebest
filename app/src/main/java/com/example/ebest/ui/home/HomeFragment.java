package com.example.ebest.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

//import com.example.ebest.Manifest;
import com.example.ebest.R;
import com.example.ebest.api.EBEST;
import com.example.ebest.api.EXTFILE;
import com.example.ebest.databinding.FragmentHomeBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.widget.LinearLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    LineChart lineChart;
    LinearLayout chartContainer;
    int index = 0;
    TextView stockview, stockinfo;
    Button buttonRead,buttonDN,buttonAll;
    EditText editText;

    EBEST ebest = new EBEST();
    EXTFILE extfile = new EXTFILE();
    ArrayList<String> stocklist;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
        }

        //WindowCompat.setDecorFitsSystemWindows(requireActivity().getWindow(), false);

        editText = binding.editTextInput;
        buttonRead = binding.buttonRead;
        buttonDN = binding.buttonDN;
        stockview = binding.stocklist;
        stockinfo = binding.stockname;
        chartContainer = binding.chartContainer;
        buttonAll = binding.buttonAll;

        stocklist = extfile.read_stocklist();
        StringBuilder slist = new StringBuilder();
        int size = stocklist.size();
        for(int i = 0;i<size;i++)
        {
            slist.append(stocklist.get(i));
            slist.append(",");
        }
        stockview.setText(slist.toString());

        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = 100;
                int[] price = new int[count];
                int[][] price3 = new int[3][count];
                String stockname;
                String userInput = editText.getText().toString();
                stockname = ebest.fetchCurrent(userInput);
                stockinfo.setText(stockname);
                price = extfile.readOHLCV(userInput);
                addChart(index,price,stockname);
                index++;
            }
        });
        buttonDN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    downloadChart();
                    buttonDN.setText("다운OK");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        buttonAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AllChart();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public void downloadChart() throws InterruptedException {
        int count = 100;
        int[][] price3 = new int[3][count];

        for (String stock : stocklist) {
            try {
                ebest.fetchChart(count, stock);
                //price3 = ebest.fetchChart3(count,userInput);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void AllChart() throws InterruptedException {
        int count = 100;
        int[][] price3 = new int[3][count];

        for (String stock : stocklist) {
            int[] price = new int[count];
            //int[][] price3 = new int[3][count];
            String stockname;
            stockname = ebest.fetchCurrent(stock);
            stockinfo.setText(stockname);
            price = extfile.readOHLCV(stock);
            addChart(index,price,stockname);
        }
    }

    private void addChart(int index, int[] prices,String stockname) {
        LineChart chart = new LineChart(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500
        );
        chart.setLayoutParams(params);

        draw_linechart(chart, prices,stockname);
        chart.invalidate(); // 차트 갱신

        chartContainer.addView(chart);
    }

    public void draw_linechart(LineChart chart, int[] prices, String stockname)
    {
        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < prices.length; i++) {
            entries.add(new Entry(i, prices[i]));  // X = index, Y = value
        }

        LineDataSet dataSet = new LineDataSet(entries, stockname);
        dataSet.setColor(getResources().getColor(R.color.teal_700));
        dataSet.setValueTextSize(12f);
        dataSet.setDrawCircles(false);
        chart.getLegend().setTextColor(Color.YELLOW); // stock prices

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setText("Sample Line Chart");
        chart.getDescription().setTextColor(Color.YELLOW);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        chart.getAxisLeft().setEnabled(true);        // 왼쪽 Y축 활성화
        chart.getAxisLeft().setDrawLabels(true);     // 값(숫자) 표시
        chart.getAxisLeft().setTextSize(12f);        // 텍스트 크기
        chart.getAxisLeft().setTextColor(Color.YELLOW); // 텍스트 색상
        chart.getAxisRight().setTextColor(Color.YELLOW); // 텍스트 색상
        chart.getXAxis().setTextColor(Color.YELLOW); // 텍스트 색상

        chart.animateX(1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}