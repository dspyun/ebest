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
import com.example.ebest.ui.common.CHART;
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
    CHART chartapi;
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
                    chartapi.addChartInfo(chartContainer);
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
                //btMinDown.setText("MinOK");
            }
        });

        buttonCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chartapi.addChartInfo(chartContainer);
            }
        });

        btDayDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dl_dayChart(SelectedFile);
                    //btDayDown.setText("DayOK");
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
                    chartapi.addChartInfo(chartContainer);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
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
                chartapi = new CHART(getContext(), SelectedFile);
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
            chartapi.addChart(getContext(),chartContainer, day_count,price,stock);
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
            chartapi.addChart(getContext(), chartContainer, day_count,price,stock);
        }
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}