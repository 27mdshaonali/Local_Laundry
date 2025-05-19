package com.binarybirds.locallaundry;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.makeramen.roundedimageview.RoundedImageView;

public class SignUpActivity extends AppCompatActivity {

    AppCompatTextView signIn;
    TextInputEditText userSignUpFullName, userSignUpEmail, userSignUpEnterPassword, userSignUpReenterPassword;
    AppCompatButton signUpButton;

    TextView mustContainsNumber, mustContainsChar, mustContainsLowerCase, mustContainsLowerUpperCase, mustContainsSpecialSymbol;

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

        initializeView();
    }

    public void initializeView() {
        signIn = findViewById(R.id.signIn);
        userSignUpFullName = findViewById(R.id.userSignUpFullName);
        userSignUpEmail = findViewById(R.id.userSignUpEmail);
        userSignUpEnterPassword = findViewById(R.id.userSignUpEnterPassword);
        userSignUpReenterPassword = findViewById(R.id.userSignUpReenterPassword);
        signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setEnabled(false);

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

        signIn.setOnClickListener(view -> signIn());
        signUpButton.setOnClickListener(view -> goToDashboard());

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
            //rePasswordField.setError("Passwords do not match");
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

    public void goToDashboard() {
        startActivity(new Intent(this, UserDashboard.class));

        userSignUpFullName.setText("");
        userSignUpEmail.setText("");
        userSignUpEnterPassword.setText("");
        userSignUpReenterPassword.setText("");


    }
}
