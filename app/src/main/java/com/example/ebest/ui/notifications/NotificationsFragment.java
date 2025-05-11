package com.example.ebest.ui.notifications;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ebest.R;
import com.example.ebest.api.DLOAD;
import com.example.ebest.api.EXTFILE;
import com.example.ebest.databinding.FragmentNotificationsBinding;
import com.example.ebest.ui.common.CHART;
import com.example.ebest.ui.home.StockEdit;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    Button btEditTarget,btMinDown,btDayDown,btMinChart,btDayChart,btBuyPrice;
    EditText editText;
    Spinner fileSpinner;
    LinearLayout chartContainer;
    int day_count;
    String SelectedFile;

    EXTFILE extfile = new EXTFILE();
    CHART chartapi;
    ArrayList<String> stocklist_in_selectedFile;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fileSpinner = binding.notiFileSpinner;
        editText = binding.notiEditText;
        btEditTarget = binding.notiEditTarget;
        btMinDown = binding.notiMinDN;
        btMinChart = binding.notiMinchart;
        btDayDown = binding.notiDayDN;
        btDayChart = binding.notiDaychart;
        btBuyPrice = binding.notiBuyprice;
        chartContainer = binding.notiContainer;

        stocklist_in_selectedFile = extfile.read_stocklist(SelectedFile);

        loadFilesIntoSpinner();

        day_count = Integer.parseInt(String.valueOf(editText.getText()));

        btEditTarget.setOnClickListener(v -> {
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
        btBuyPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chartapi.addChartInfo(chartContainer);
            }
        });
        //final TextView textView = binding.textNotifications;
        //notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public void ShowMinChart() throws InterruptedException {
        day_count = Integer.parseInt(String.valueOf(editText.getText()));


        // before add, remove all old chart
        chartContainer.removeAllViews();

        for (String stock : stocklist_in_selectedFile) {
            int[][] price3 = new int[3][day_count];
            int[] price  = extfile.readOHLCV(stock+"min");
            chartapi.addChart(getContext(), chartContainer, day_count,price,stock);
        }
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
        // before add, remove all old chart
        chartContainer.removeAllViews();

        for (String stock : stocklist_in_selectedFile) {
            int[][] price3 = new int[3][day_count];
            int[] price  = extfile.readOHLCV(stock);
            chartapi.addChart(getContext(),chartContainer, day_count,price,stock);
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

        // key.txt는 제외하고 보여준다
//        if (files != null) {
//            for (File file : files) {
//                if (file.isFile()) {
//                    String fname = file.getName();
//                    if(!fname.equals("key.txt") && !fname.equals("codelist.csv") )
//                        fileNames.add(file.getName());
//                }
//            }
//        }

        fileNames.add("배당성장.txt");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}