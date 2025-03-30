package com.example.ebest.ui.home;

import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ebest.api.EBEST;
import com.example.ebest.databinding.FragmentHomeBinding;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        EBEST ebest = new EBEST();
        //ebest.fetchAccessHoGa();
        String hoga = "";
        hoga = ebest.example_exec();

        final TextView textView = binding.textHome;
        textView.setText(hoga);
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}