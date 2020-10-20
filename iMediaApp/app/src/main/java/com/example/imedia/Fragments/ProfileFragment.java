package com.example.imedia.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.imedia.Activities.LogIn;
import com.example.imedia.Firebase.FirebaseService;
import com.example.imedia.R;

public class ProfileFragment extends Fragment {

    private Button btn_logout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inicializarComponentes(view);

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseService.logOut();
                Intent logIn = new Intent(getContext(), LogIn.class);
                startActivity(logIn);
                getActivity().finish();
            }
        });
    }

    private void inicializarComponentes(View view) {
        btn_logout = (Button) view.findViewById(R.id.btn_logout);
    }
}