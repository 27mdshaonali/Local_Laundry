package com.binarybirds.locallaundry;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class UserDashboard extends AppCompatActivity {

    private static final String TAG = "UserDashboard";
    private static final String ORDERS_URL = "https://codecanvas.top/WashWave/get_user_orders.php";

    // Separate launchers for each permission
    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) Log.d(TAG, "Notification permission granted.");
        else Log.d(TAG, "Notification permission denied.");
    });

    private final ActivityResultLauncher<String> requestSmsPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) Log.d(TAG, "SMS permission granted.");
        else Log.d(TAG, "SMS permission denied.");
    });

    private TextView welcomeText;
    private SessionManager sessionManager;
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SessionManager first
        sessionManager = new SessionManager(getApplicationContext());

        // --- CRITICAL SESSION CHECK ---
        // If the user is not logged in, redirect them immediately and stop executing this activity.
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Methods to set up the UI and functionality
        initializeViews();
        loadUserData();
        setupNavigation();
        setupFirebaseAndPermissions();
    }

    private void initializeViews() {
        welcomeText = findViewById(R.id.welcome);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolBar);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void loadUserData() {
        // --- Get user data ONLY from SessionManager ---
        HashMap<String, String> userDetails = sessionManager.getUserDetails();
        String name = userDetails.get(SessionManager.KEY_NAME);
        String email = userDetails.get(SessionManager.KEY_EMAIL);

        if (name != null) {
            welcomeText.setText("Welcome, " + name);
        } else {
            welcomeText.setText("Welcome!"); // Fallback
        }

        if (email != null) {
            fetchUserOrders(email);
        } else {
            Log.e(TAG, "Email not found in session. Cannot fetch orders.");
        }
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        NavigationView drawerNavigationView = findViewById(R.id.drawerNavigationView);

        bottomNav.getOrCreateBadge(R.id.notification).setNumber(9); // Example badge

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                Toast.makeText(UserDashboard.this, "Home", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.cart) {
                Toast.makeText(UserDashboard.this, "Cart", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.notification) {
                bottomNav.removeBadge(R.id.notification);
                Toast.makeText(UserDashboard.this, "Notification", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.profile) {
                Toast.makeText(UserDashboard.this, "Profile", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        drawerNavigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.logout) { // Assumes you have android:id="@+id/logout" in your menu xml
                logoutUser();
            } else {
                Toast.makeText(UserDashboard.this, item.getTitle(), Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void setupFirebaseAndPermissions() {
        askNotificationPermission();
        askSmsPermission();
        initFirebaseToken();
    }

    private void fetchUserOrders(String email) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ORDERS_URL, response -> {
            Log.d(TAG, "Orders Response: " + response);
            try {
                JSONArray jsonArray = new JSONArray(response);
                // TODO: Populate a RecyclerView with the orders instead of showing multiple toasts.
                if (jsonArray.length() > 0) {
                    Toast.makeText(this, jsonArray.length() + " order(s) found.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No orders found.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON Parsing Error: " + e.getMessage());
            }
        }, error -> {
            Log.e(TAG, "Volley Error: " + error.toString());
            Toast.makeText(this, "Error fetching orders.", Toast.LENGTH_SHORT).show();
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void logoutUser() {
        sessionManager.logout();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(UserDashboard.this, MainActivity.class);
        // Clear all previous activities from the stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish the dashboard activity
    }

    public void initFirebaseToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }
            String token = task.getResult();
            Log.d(TAG, "FCM Token: " + token);
            // You can send this token to your server here if needed
        });
    }

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // You can show a rationale dialog here if needed before launching
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void askSmsPermission() {
        // This is necessary for API level >= 23 (Marshmallow)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
                    // Show a dialog explaining why you need this permission
                    new AlertDialog.Builder(this).setTitle("SMS Permission").setMessage("This permission is needed for sending order updates via SMS.").setPositiveButton("OK", (dialog, which) -> requestSmsPermissionLauncher.launch(Manifest.permission.SEND_SMS)).setNegativeButton("Cancel", null).create().show();
                } else {
                    requestSmsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
                }
            }
        }
    }
}