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

        EditText editText = binding.editTextInput;
        Button buttonRead = binding.buttonRead;
        TextView stockview = binding.stocklist;
        TextView stockinfo = binding.stockname;
        chartContainer = binding.chartContainer;

        EBEST ebest = new EBEST();
        EXTFILE extfile = new EXTFILE();
        String[] stocklist = extfile.read_stocklist();
        StringBuilder slist = new StringBuilder();
        int size = stocklist.length;
        for(int i = 0;i<size;i++)
        {
            slist.append(stocklist[i]);
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
                try {
                    stockname = ebest.fetchAccessCurrent(userInput);
                    price = ebest.fetchAccessChart(count,userInput);
                    price3 = ebest.fetchAccessChart3(count,userInput);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                //final TextView textView = binding.textHome;
                //textView.setText("현재가 : " + price);
                stockinfo.setText(stockname);
                //lineChart = binding.lineChart;
                //draw_linechart(lineChart,price);
                addChart(index,price,stockname);
                index++;
            }
        });

        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
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