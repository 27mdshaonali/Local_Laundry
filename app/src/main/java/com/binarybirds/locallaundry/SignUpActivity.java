package com.binarybirds.locallaundry;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private static final String SIGN_UP_URL = "https://codecanvas.top/WashWave/sign_up.php";

    private AppCompatTextView signIn;
    private TextInputEditText userSignUpFullName, userSignUpEmail, userSignUpEnterPassword, userSignUpReenterPassword;
    private AppCompatButton signUpButton;
    private TextView mustContainsNumber, mustContainsChar, mustContainsLowerCase, mustContainsLowerUpperCase, mustContainsSpecialSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        signIn = findViewById(R.id.signIn);
        userSignUpFullName = findViewById(R.id.userSignUpFullName);
        userSignUpEmail = findViewById(R.id.userSignUpEmail);
        userSignUpEnterPassword = findViewById(R.id.userSignUpEnterPassword);
        userSignUpReenterPassword = findViewById(R.id.userSignUpReenterPassword);
        signUpButton = findViewById(R.id.signUpButton);

        mustContainsNumber = findViewById(R.id.mustContainsNumber);
        mustContainsChar = findViewById(R.id.mustContainsChar);
        mustContainsLowerCase = findViewById(R.id.mustContainsLowerCase);
        mustContainsLowerUpperCase = findViewById(R.id.mustContainsLowerUpperCase);
        mustContainsSpecialSymbol = findViewById(R.id.mustContainsSpecialSymbol);

        signUpButton.setEnabled(false); // Disable button initially
    }

    private void setupListeners() {
        signIn.setOnClickListener(view ->
                startActivity(new Intent(SignUpActivity.this, MainActivity.class))
        );

        signUpButton.setOnClickListener(v -> performSignUp());

        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        userSignUpFullName.addTextChangedListener(validationWatcher);
        userSignUpEmail.addTextChangedListener(validationWatcher);
        userSignUpEnterPassword.addTextChangedListener(validationWatcher);
        userSignUpReenterPassword.addTextChangedListener(validationWatcher);
    }

    private void performSignUp() {
        String signUpUserName = userSignUpFullName.getText().toString().trim();
        String email = userSignUpEmail.getText().toString().trim();
        String pass = userSignUpEnterPassword.getText().toString().trim();

        // The validation logic already ensures fields are not empty and passwords match.
        // This is a final safeguard.
        if (signUpUserName.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SIGN_UP_URL, response -> {
            switch (response.trim()) {
                case "success":
                    Toast.makeText(getApplicationContext(), "Sign Up Successful", Toast.LENGTH_SHORT).show();

                    // --- CORRECTED SESSION HANDLING ---
                    // 1. Create a session using ONLY the SessionManager.
                    SessionManager sessionManager = new SessionManager(this);
                    sessionManager.createLoginSession(signUpUserName, email);

                    // 2. Navigate to the dashboard, clearing previous activities.
                    Intent intent = new Intent(getApplicationContext(), UserDashboard.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    break;

                case "exists":
                    Toast.makeText(getApplicationContext(), "This email is already registered.", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.e(TAG, "Sign up failed with response: " + response);
                    Toast.makeText(getApplicationContext(), "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }, error -> {
            Log.e(TAG, "Volley Error: " + error.toString());
            Toast.makeText(getApplicationContext(), "Network error. Please try again.", Toast.LENGTH_LONG).show();
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
                data.put("name", signUpUserName);
                data.put("email", email);
                data.put("password", pass);
                return data;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void validateFields() {
        String fullName = userSignUpFullName.getText().toString().trim();
        String email = userSignUpEmail.getText().toString().trim();
        String password = userSignUpEnterPassword.getText().toString();
        String rePassword = userSignUpReenterPassword.getText().toString();

        // Field and password match validation
        boolean allFilled = !fullName.isEmpty() && !email.isEmpty() && !password.isEmpty() && !rePassword.isEmpty();
        boolean passwordsMatch = password.equals(rePassword);

        // Password complexity validation
        boolean isLengthValid = password.length() >= 8;
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+=<>?/{}~`|\\[\\]\\-].*");
        boolean allPasswordConditionsMet = isLengthValid && hasNumber && hasLower && hasUpper && hasSpecial;

        // Update UI for password validation hints
        updateValidationUI(isLengthValid, hasNumber, hasLower, hasUpper, hasSpecial);

        // Enable button only if all conditions are met
        signUpButton.setEnabled(allFilled && passwordsMatch && allPasswordConditionsMet);
    }

    private void updateValidationUI(boolean isLengthValid, boolean hasNumber, boolean hasLower, boolean hasUpper, boolean hasSpecial) {
        int defaultColor = ContextCompat.getColor(this, R.color.textSecondary);
        int successColor = ContextCompat.getColor(this, R.color.colorSuccess);

        updateTextViewValidation(mustContainsChar, isLengthValid, successColor, defaultColor);
        updateTextViewValidation(mustContainsNumber, hasNumber, successColor, defaultColor);
        updateTextViewValidation(mustContainsLowerCase, hasLower, successColor, defaultColor);
        updateTextViewValidation(mustContainsLowerUpperCase, hasUpper, successColor, defaultColor);
        updateTextViewValidation(mustContainsSpecialSymbol, hasSpecial, successColor, defaultColor);
    }

    private void updateTextViewValidation(TextView textView, boolean isValid, int successColor, int defaultColor) {
        if (isValid) {
            textView.setTextColor(successColor);
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_mark, 0, 0, 0);
        } else {
            textView.setTextColor(defaultColor);
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dot, 0, 0, 0);
        }
    }
}