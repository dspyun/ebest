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

    LinearLayout chartContainer;
    int index = 0;
    Button btEditFile,btDayDown,btDayChart,buttonCurrent,btMinDown, btMinChart,btBuyPrice;
    EditText editText;
    Spinner fileSpinner;

    String SelectedFile;

    EBEST ebest = new EBEST();
    EXTFILE extfile = new EXTFILE();
    ArrayList<String> stocklist_in_selectedFile;
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
        fileSpinner = binding.fileSpinner;
        editText = binding.homeEditPeriod;
        btEditFile = binding.homeEditTarget;
        btMinDown = binding.btMinDN;
        btMinChart = binding.btMinChart;
        btDayDown = binding.homeBtDayDN;
        btDayChart = binding.homeBtDayChart;
        buttonCurrent = binding.btCurrent;
        chartContainer = binding.chartContainer;
        loadFilesIntoSpinner();

        day_count = Integer.parseInt(String.valueOf(editText.getText()));

        // edit stock item list
        btEditFile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), StockEdit.class);
            intent.putExtra("file_name", SelectedFile);
            startActivity(intent);
        });

        btMinChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ShowMinChart();
                    addChartInfo();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        btMinDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dl_minChart(SelectedFile);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                btMinDown.setText("MinOK");

            }
        });

        buttonCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addChartInfo();
            }
        });

        btDayDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dl_dayChart(SelectedFile);
                    btDayDown.setText("DayOK");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        btDayChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ShowChart();
                    addChartInfo();
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
                    if(!fname.equals("key.txt") && !fname.equals("codelist.csv") )
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
                SelectedFile = fileNames.get(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
    }
    public void dl_dayChart(String stocklist) throws InterruptedException {

        day_count = Integer.parseInt(String.valueOf(editText.getText()));
        DLOAD dload = new DLOAD(SelectedFile);
        dload.threadChartDayList(day_count);
    }
    public void dl_minChart(String stocklist) throws InterruptedException {

        day_count = Integer.parseInt(String.valueOf(editText.getText()));
        DLOAD dload = new DLOAD(SelectedFile);
        dload.threadChartMinList(day_count);
    }
    public void ShowChart() throws InterruptedException {
        day_count = Integer.parseInt(String.valueOf(editText.getText()));

        stocklist_in_selectedFile = extfile.read_stocklist(SelectedFile);
        // before add, remove all old chart
        chartContainer.removeAllViews();

        for (String stock : stocklist_in_selectedFile) {
            int[][] price3 = new int[3][day_count];
            int[] price  = extfile.readOHLCV(stock);
            addChart(day_count,price,stock);
        }
    }
    public void ShowMinChart() throws InterruptedException {
        day_count = Integer.parseInt(String.valueOf(editText.getText()));

        stocklist_in_selectedFile = extfile.read_stocklist(SelectedFile);
        // before add, remove all old chart
        chartContainer.removeAllViews();

        for (String stock : stocklist_in_selectedFile) {
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

    public void addChartInfoDetail(LineChart chart, String stockname, String current_price, String buy_price)
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
        String info = "(" + stockname + ")" + current_price + " " + percent;
        info += "(buy price : "+buy_price+")";
        chart.getDescription().setText(info);
        dataSet.setLabel(info);
        lineData = new LineData(dataSet);
        chart.setData(lineData);
    }
    private void addChartInfo() {
        HashMap<String, String> currentPriceMap = new HashMap<>();
        ArrayList<String> buypricelist = new ArrayList<>();
        buypricelist = extfile.read_buyprice(SelectedFile);

        for (int i =0;i<stocklist_in_selectedFile.size();i++) {
            String stock = stocklist_in_selectedFile.get(i);
            ArrayList<String> info = new ArrayList<>();
            info.add(stock);
            info.add(extfile.findStockName(stock));
            LineChart chart = (LineChart) chartContainer.getChildAt(i);

            addChartInfoDetail(chart,stock, info.get(1), buypricelist.get(i));
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
    public String percent_value(float yesterday_price, float c_price)
    {
        float result = 100*yesterday_price/c_price;
        return String.valueOf((int)result)+"%";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}