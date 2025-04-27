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
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

//import com.example.ebest.Manifest;
import com.example.ebest.MainActivity;
import com.example.ebest.R;
import com.example.ebest.api.DLOAD;
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
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    LineChart lineChart;
    LinearLayout chartContainer;
    int index = 0;
    TextView stockview, stockinfo;
    Button buttonRead,buttonDN,buttonChart,buttonDummy1,buttonMinDN, buttonMinChart;
    EditText editText;
    Spinner fileSpinner;

    String STOCKGROUP;

    EBEST ebest = new EBEST();
    EXTFILE extfile = new EXTFILE();
    ArrayList<String> stocklist;
    HashMap<String, String> currentPriceMap = new HashMap<>();
    int day_count;

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

        buttonRead = binding.buttonRead;
        buttonDN = binding.buttonDN;
        chartContainer = binding.chartContainer;
        buttonChart = binding.buttonChart;
        editText = binding.editText;
        fileSpinner = binding.fileSpinner;
        buttonDummy1 = binding.btDummy1;
        buttonMinDN = binding.btMinDN;
        buttonMinChart = binding.btMinChart;

        loadFilesIntoSpinner();

        day_count = Integer.parseInt(String.valueOf(editText.getText()));

        Button btnEdit = binding.buttonRead;
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), StockEdit.class);
            intent.putExtra("file_name", STOCKGROUP);
            startActivity(intent);
        });


        buttonMinChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ShowMinChart();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        buttonMinDN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    downloadChartMin(STOCKGROUP);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                buttonMinDN.setText("MinOK");

            }
        });

        buttonDummy1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addChartInfo();
            }
        });

        buttonDN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    downloadChart(STOCKGROUP);
                    buttonDN.setText("DayOK");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        buttonChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ShowChart();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().hide();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().hide();
        }
    }


    private void loadFilesIntoSpinner() {
        File ebestDir = new File(Environment.getExternalStorageDirectory(), "ebest");

        if (!ebestDir.exists() || !ebestDir.isDirectory()) {
            Toast.makeText(getContext(), "ebest 폴더가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        File[] files = ebestDir.listFiles();
        ArrayList<String> fileNames = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String fname = file.getName();
                    if(!fname.equals("key.txt"))
                        fileNames.add(file.getName());
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, fileNames);

        fileSpinner.setAdapter(adapter);

        // 선택 시 TextView에 파일 이름 표시
        fileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                STOCKGROUP = fileNames.get(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
    }
    public void downloadChart(String stocklist) throws InterruptedException {

        day_count = Integer.parseInt(String.valueOf(editText.getText()));
        DLOAD dload = new DLOAD(STOCKGROUP);
        dload.chartList(day_count);
    }
    public void downloadChartMin(String stocklist) throws InterruptedException {

        day_count = Integer.parseInt(String.valueOf(editText.getText()));
        DLOAD dload = new DLOAD(STOCKGROUP);
        dload.chartminList(day_count);
    }
    public void ShowChart() throws InterruptedException {
        day_count = Integer.parseInt(String.valueOf(editText.getText()));

        stocklist = extfile.read_stocklist(STOCKGROUP);
        // before add, remove all old chart
        chartContainer.removeAllViews();

        for (String stock : stocklist) {
            int[][] price3 = new int[3][day_count];
            int[] price  = extfile.readOHLCV(stock);
            addChart(day_count,price,stock);
        }
    }
    public void ShowMinChart() throws InterruptedException {
        day_count = Integer.parseInt(String.valueOf(editText.getText()));

        stocklist = extfile.read_stocklist(STOCKGROUP);
        // before add, remove all old chart
        chartContainer.removeAllViews();

        for (String stock : stocklist) {
            int[][] price3 = new int[3][day_count];
            int[] price  = extfile.readOHLCV(stock+"min");
            addChart(day_count,price,stock);
        }
    }

    private void addChart(int day_count, int[] prices,String stockname) {
        LineChart chart = new LineChart(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500
        );
        chart.setLayoutParams(params);
        draw_linechart(day_count, chart, prices, stockname);
        chart.invalidate(); // 차트 갱신
        chartContainer.addView(chart);
    }

    private void addChartInfo() {
        DLOAD dload = new DLOAD(STOCKGROUP);
        currentPriceMap = dload.CurrentPriceList();

        for (int i =0;i<stocklist.size();i++) {
            String stock = stocklist.get(i);
            String current_info = currentPriceMap.get(stock);
            String[] info = current_info.split(",");
            LineChart chart = (LineChart) chartContainer.getChildAt(i);
            info_linechart(chart,stock, info[0], info[1]);
            chart.invalidate(); // 차트 갱신
        }
        chartContainer.invalidate();

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
        dataSet.setColor(getResources().getColor(R.color.teal_700));
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

    public void info_linechart(LineChart chart, String stock_no, String stockname, String current_price)
    {
        LineData lineData = chart.getData();
        LineDataSet dataSet = (LineDataSet) lineData.getDataSetByIndex(0); // 첫 번째 데이터셋
        int last_close = dataSet.getEntryCount();
        Entry entry = dataSet.getEntryForIndex(last_close-1); // 인덱스 마지막 엔트리
        int x = (int)entry.getX();
        int y = (int)entry.getY();

        String percent = percent_value(y,current_price);
        chart.getLegend().setTextColor(Color.YELLOW); // stock prices
        String info = stockname + " (" + stock_no + ") " + current_price + " " + percent;
        chart.getDescription().setText(info);
        dataSet.setLabel(info);
        lineData = new LineData(dataSet);
        chart.setData(lineData);
    }

    public String percent_value(int yesterday_price, String c_price)
    {
        int current_price = Integer.parseInt(c_price);
        int result = 100*current_price/yesterday_price;
        return String.valueOf(current_price-yesterday_price) + " " + String.valueOf(result-100) +"%";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}