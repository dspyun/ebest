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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ebest.api.DLOAD;
import com.example.ebest.api.EBEST;
import com.example.ebest.databinding.FragmentDashboardBinding;
import com.example.ebest.ui.onedepth.Inform;
import com.example.ebest.ui.onedepth.StockEdit;
import com.example.ebest.ui.onedepth.news;

import java.io.File;
import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    LinearLayout chartContainer;
    Button buttonNews,buttonRanking;
    EditText editText;
    Spinner fileSpinner;
    String STOCKGROUP;
    int day_count;
    EBEST ebest = new EBEST();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        buttonNews = binding.btNews;
        buttonRanking = binding.btRanking;

        buttonNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), news.class);
                intent.putExtra("token", ebest.get_token());
                startActivity(intent);

            }
        });

        buttonRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DLOAD dload = new DLOAD("");
                String information = dload.threadProfitRanking("");
                Intent intent = new Intent(requireContext(), Inform.class);
                intent.putExtra("stock_code", information);
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}