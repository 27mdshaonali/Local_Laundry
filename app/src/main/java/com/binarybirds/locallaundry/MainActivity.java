package com.binarybirds.locallaundry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    AppCompatTextView signUp;
    AppCompatButton signInButton;
    TextInputEditText userSignInEmail, userSignInPassword;
    AppCompatCheckBox checkBox;
    AppCompatTextView forgotPassword;
    String email, password;
    RoundedImageView logInWithFB;
    String SIGN_IN_URL = "https://codecanvas.top/WashWave/sign_in.php";
    SessionManager sessionManager;

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

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, UserDashboard.class));
            finish();
        }


    }

    public void initializeView() {
        signInButton = findViewById(R.id.signInButton);
        signUp = findViewById(R.id.signUp);
        userSignInEmail = findViewById(R.id.userSignInEmail);
        userSignInPassword = findViewById(R.id.userSignInPassword);
        checkBox = findViewById(R.id.checkBox);
        forgotPassword = findViewById(R.id.forgotPassword);
        logInWithFB = findViewById(R.id.logInWithFB);


        signUp.setOnClickListener(v -> setSignUp());
        logInWithFB.setOnClickListener(v -> {
            startActivity(new Intent(this, UserDashboard.class));

            signInButton.setOnClickListener(v1 -> {

                email = userSignInEmail.getText().toString();
                password = userSignInPassword.getText().toString();

                if (!email.isEmpty() && !password.isEmpty()) {

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, SIGN_IN_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (response.trim().equals("success")) {
                                sessionManager.createLoginSession(email);

                                SharedPreferences prefs = getSharedPreferences("" + R.string.app_name, MODE_PRIVATE);
                                prefs.edit().putString("email", email).apply();

                                Intent intent = new Intent(getApplicationContext(), UserDashboard.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("email", email);
                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(getApplicationContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Toast.makeText(getApplicationContext(), "Server Error: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Network Error: " + error, Toast.LENGTH_LONG).show();
                            }
                        }
                    }) {
                        @Nullable
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> data = new HashMap<>();
                            data.put("email", email);
                            data.put("password", password);
                            return data;
                        }
                    };

                    RequestQueue requestQueue = Volley.newRequestQueue(this);
                    requestQueue.add(stringRequest);

                } else {
                    Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                }

            });
        });
        signInButton.setOnClickListener(v -> setSignInBtn());
    }


    public void setSignUp() {
        startActivity(new Intent(this, SignUpActivity.class));
    }

    public void setSignInBtn() {

        email = userSignInEmail.getText().toString();
        password = userSignInPassword.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()) {


            StringRequest stringRequest = new StringRequest(Request.Method.POST, SIGN_IN_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.trim().equals("success")) {
                        sessionManager.createLoginSession(email);
                        SharedPreferences prefs = getSharedPreferences("" + R.string.app_name, MODE_PRIVATE);
                        prefs.edit().putString("email", email).apply();

                        Intent intent = new Intent(getApplicationContext(), UserDashboard.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (error.networkResponse != null) {
                        Toast.makeText(getApplicationContext(), "Server Error: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
                    }

                }
            }) {

                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> data = new HashMap<>();
                    data.put("email", email);
                    data.put("password", password);
                    return data;
                }

            };

            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);


        } else {
            Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
        }


    }
}
