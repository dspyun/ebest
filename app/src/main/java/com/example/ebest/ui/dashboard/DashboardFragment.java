package com.example.ebest.ui.dashboard;

import android.content.Intent;
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

import com.example.ebest.api.DLOAD;
import com.example.ebest.databinding.FragmentDashboardBinding;
import com.example.ebest.ui.home.StockEdit;

import java.io.File;
import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    LinearLayout chartContainer;
    Button buttonRead,buttonDN,buttonChart,buttonCurrent,buttonMinDN, buttonMinChart;
    EditText editText;
    Spinner fileSpinner;
    String STOCKGROUP;
    int day_count;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        buttonRead = binding.buttonRead1;
        buttonDN = binding.buttonDN1;
        chartContainer = binding.chartContainer1;
        buttonChart = binding.buttonChart1;
        editText = binding.editText1;
        fileSpinner = binding.fileSpinner1;
        buttonCurrent = binding.btCurrent1;
        buttonMinDN = binding.btMinDN1;
        buttonMinChart = binding.btMinChart1;


        loadFilesIntoSpinner();

        day_count = Integer.parseInt(String.valueOf(editText.getText()));

        // edit stock item list
        Button btnEdit = binding.buttonRead1;
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), StockEdit.class);
            intent.putExtra("file_name", STOCKGROUP);
            startActivity(intent);
        });

        buttonCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DLOAD dload = new DLOAD(STOCKGROUP);
                dload.threadFulllist();
                buttonCurrent.setText("전종목OK");
            }
        });

        buttonDN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                day_count = Integer.parseInt(String.valueOf(editText.getText()));
                DLOAD dload = new DLOAD(STOCKGROUP);
                dload.threadFulllistChart(day_count);

                buttonDN.setText("전종목OK");
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
}