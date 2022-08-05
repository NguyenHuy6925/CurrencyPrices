package com.example.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.currencyprices.MainActivity;
import com.example.currencyprices.R;

public class AboutFragment extends Fragment {
    Button btnOk;
    FragmentTransaction fragmentTransaction = MainActivity.fragmentTransaction;
    FragmentManager fragmentManager = MainActivity.fragmentManager;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.about_fragment,container,false);
        btnOk = view.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
                MainActivity.aboutFragmentShowing = !MainActivity.aboutFragmentShowing;
            }
        });
        return view;
    }
}
