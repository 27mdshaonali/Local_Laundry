package com.binarybirds.locallaundry;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    AppCompatTextView signUp;
    AppCompatButton signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeView();


    }

    public void initializeView() {

        signInButton = findViewById(R.id.signInButton);
        signUp = findViewById(R.id.signUp);
        signInButton.setOnClickListener(v -> setLogin());
        signUp.setOnClickListener(v -> setSignUp());
    }

    public void setSignUp() {

        startActivity(new Intent(this, SignUpActivity.class));

    }

    public void setLogin() {

        startActivity(new Intent(this, UserDashboard.class));


    }

}