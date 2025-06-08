package com.binarybirds.locallaundry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    AppCompatTextView signUp;
    AppCompatButton signInButton;
    TextInputEditText userSignInEmail, userSignInPassword;
    AppCompatCheckBox rememberMeCheckbox;
    AppCompatTextView forgotPassword;
    String email, password;
    RoundedImageView logInWithFB;
    String SIGN_IN_URL = "https://codecanvas.top/WashWave/sign_in.php";
    SessionManager sessionManager;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

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

        // If already logged in
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, UserDashboard.class));
            finish();
        }

        // Load saved credentials
        boolean rememberMe = preferences.getBoolean("rememberMe", false);
        if (rememberMe) {
            userSignInEmail.setText(preferences.getString("email", ""));
            userSignInPassword.setText(preferences.getString("password", ""));
            rememberMeCheckbox.setChecked(true);
        }

        // Button Clicks
        signUp.setOnClickListener(v -> setSignUp());
        signInButton.setOnClickListener(v -> setSignInBtn());
        logInWithFB.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, UserDashboard.class));
            finish();
        });
    }

    public void initializeView() {
        signInButton = findViewById(R.id.signInButton);
        signUp = findViewById(R.id.signUp);
        userSignInEmail = findViewById(R.id.userSignInEmail);
        userSignInPassword = findViewById(R.id.userSignInPassword);
        rememberMeCheckbox = findViewById(R.id.checkBox);
        forgotPassword = findViewById(R.id.forgotPassword);
        logInWithFB = findViewById(R.id.logInWithFB);

        preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void setSignUp() {
        startActivity(new Intent(this, SignUpActivity.class));
    }

    public void setSignInBtn() {
        email = userSignInEmail.getText().toString().trim();
        password = userSignInPassword.getText().toString().trim();
        boolean rememberMe = rememberMeCheckbox.isChecked();


        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SIGN_IN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("SIGN_IN_RESPONSE", response); // For debugging

                // ... inside the StringRequest onResponse lambda
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");

                    if (status.equals("success")) {
                        String userName = jsonObject.getString("name");
                        Toast.makeText(getApplicationContext(), "Welcome " + userName, Toast.LENGTH_SHORT).show();

                        // --- FIX: Use the updated SessionManager ---
                        //sessionManager.createLoginSession(userName, email);
                        sessionManager.createLoginSession(userName, email);

                        // --- FIX: Correctly handle "Remember Me" ---
                        // Only save the email for convenience. NEVER save the password.
                        if (rememberMe) {
                            editor.putBoolean("rememberMe", true);
                            editor.putString("email", email);
                        } else {
                            editor.clear();
                        }
                        editor.apply();

                        // Navigate to Dashboard. No need for extras.
                        Intent intent = new Intent(MainActivity.this, UserDashboard.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Invalid response format", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), "Sign In failed"+error.toString(), Toast.LENGTH_SHORT).show();

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

        //==================================================================================


    }
}
