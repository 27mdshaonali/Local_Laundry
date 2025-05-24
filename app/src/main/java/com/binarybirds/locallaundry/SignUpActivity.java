package com.binarybirds.locallaundry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private static final int REQUEST_GALLERY = 1;
    AppCompatTextView signIn;
    TextInputEditText userSignUpFullName, userSignUpEmail, userSignUpEnterPassword, userSignUpReenterPassword;
    AppCompatButton signUpButton;
    RoundedImageView appImage;
    TextView mustContainsNumber, mustContainsChar, mustContainsLowerCase, mustContainsLowerUpperCase, mustContainsSpecialSymbol;
    String SIGN_UP_URL = "https://codecanvas.top/WashWave/sign_up.php";

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


        signIn = findViewById(R.id.signIn);
        userSignUpFullName = findViewById(R.id.userSignUpFullName);
        userSignUpEmail = findViewById(R.id.userSignUpEmail);
        userSignUpEnterPassword = findViewById(R.id.userSignUpEnterPassword);
        userSignUpReenterPassword = findViewById(R.id.userSignUpReenterPassword);
        signUpButton = findViewById(R.id.signUpButton);
        appImage = findViewById(R.id.appImage);
        signUpButton.setEnabled(false);





        signIn.setOnClickListener(view -> signIn());


        signUpButton.setOnClickListener(v -> {
            String name = userSignUpFullName.getText().toString().trim();
            String email = userSignUpEmail.getText().toString().trim();
            String pass = userSignUpEnterPassword.getText().toString().trim();
            String rePass = userSignUpReenterPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || rePass.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(rePass)) {
                Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }


            StringRequest stringRequest = new StringRequest(Request.Method.POST, SIGN_UP_URL, response -> {
                switch (response.trim()) {
                    case "success":


                        Toast.makeText(getApplicationContext(), "Sign Up Successful", Toast.LENGTH_SHORT).show();

                        // ✅ Create session after successful sign-up
                        SessionManager sessionManager = new SessionManager(this);
                        sessionManager.createLoginSession(email);


                        SharedPreferences prefs = getSharedPreferences(""+R.string.app_name, MODE_PRIVATE);
                        prefs.edit().putString("email", email).apply();


                        // Navigate to Dashboard
                        Intent intent = new Intent(getApplicationContext(), UserDashboard.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                        break;

                    case "exists":
                        Toast.makeText(getApplicationContext(), "Email already exists", Toast.LENGTH_SHORT).show();
                        break;
                    case "image_upload_failed":
                        Toast.makeText(getApplicationContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Failed: " + response, Toast.LENGTH_SHORT).show();
                        break;
                }
            }, error -> {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    Log.e("VolleyError", "Status Code: " + networkResponse.statusCode);
                    Log.e("VolleyError", "Response Data: " + new String(networkResponse.data));
                }
                Toast.makeText(getApplicationContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> data = new HashMap<>();
                    data.put("name", name);
                    data.put("email", email);
                    data.put("password", pass);
                    return data;
                }

                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        });




        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFields(userSignUpFullName, userSignUpEmail, userSignUpEnterPassword, userSignUpReenterPassword, signUpButton);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        userSignUpFullName.addTextChangedListener(inputWatcher);
        userSignUpEmail.addTextChangedListener(inputWatcher);
        userSignUpEnterPassword.addTextChangedListener(inputWatcher);
        userSignUpReenterPassword.addTextChangedListener(inputWatcher);

        initializeView();
    }

    public void initializeView() {

    }

    private void validateFields(TextInputEditText fullNameField, TextInputEditText emailField, TextInputEditText passwordField, TextInputEditText rePasswordField, AppCompatButton signUpButton) {
        String fullName = fullNameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString();
        String rePassword = rePasswordField.getText().toString();

        boolean allFilled = !fullName.isEmpty() && !email.isEmpty() && !password.isEmpty() && !rePassword.isEmpty();
        boolean passwordsMatch = password.equals(rePassword);

        boolean isLengthValid = password.length() >= 8;
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+=<>?/{}~`|\\[\\]\\-].*");

        boolean allPasswordConditionsMet = isLengthValid && hasNumber && hasLower && hasUpper && hasSpecial;

        mustContainsNumber = findViewById(R.id.mustContainsNumber);
        mustContainsChar = findViewById(R.id.mustContainsChar);
        mustContainsLowerCase = findViewById(R.id.mustContainsLowerCase);
        mustContainsLowerUpperCase = findViewById(R.id.mustContainsLowerUpperCase);
        mustContainsSpecialSymbol = findViewById(R.id.mustContainsSpecialSymbol);

        int defaultColor = ContextCompat.getColor(this, R.color.textSecondary);
        int successColor = ContextCompat.getColor(this, R.color.colorSuccess);

        mustContainsChar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dot, 0, 0, 0);
        mustContainsChar.setTextColor(defaultColor);

        mustContainsNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dot, 0, 0, 0);
        mustContainsNumber.setTextColor(defaultColor);

        mustContainsLowerCase.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dot, 0, 0, 0);
        mustContainsLowerCase.setTextColor(defaultColor);

        mustContainsLowerUpperCase.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dot, 0, 0, 0);
        mustContainsLowerUpperCase.setTextColor(defaultColor);

        mustContainsSpecialSymbol.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dot, 0, 0, 0);
        mustContainsSpecialSymbol.setTextColor(defaultColor);

        if (!passwordsMatch && !rePassword.isEmpty()) {
            Log.d("SignUpActivity", "Passwords do not match");
        } else {
            rePasswordField.setError(null);
        }

        if (isLengthValid) {
            mustContainsChar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_mark, 0, 0, 0);
            mustContainsChar.setTextColor(successColor);
        }
        if (hasNumber) {
            mustContainsNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_mark, 0, 0, 0);
            mustContainsNumber.setTextColor(successColor);
        }
        if (hasLower) {
            mustContainsLowerCase.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_mark, 0, 0, 0);
            mustContainsLowerCase.setTextColor(successColor);
        }
        if (hasUpper) {
            mustContainsLowerUpperCase.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_mark, 0, 0, 0);
            mustContainsLowerUpperCase.setTextColor(successColor);
        }
        if (hasSpecial) {
            mustContainsSpecialSymbol.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_mark, 0, 0, 0);
            mustContainsSpecialSymbol.setTextColor(successColor);
        }

        signUpButton.setEnabled(allFilled && passwordsMatch && allPasswordConditionsMet);
    }

    public void signIn() {
        startActivity(new Intent(this, MainActivity.class));
    }

}
