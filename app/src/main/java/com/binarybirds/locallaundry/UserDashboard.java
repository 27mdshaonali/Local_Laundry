package com.binarybirds.locallaundry;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserDashboard extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_CAMERA = 2;
    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    });
    RoundedImageView pickedImage, picImage;
    TextView welcomeText, viewAllNotices;
    SessionManager sessionManager;
    BottomNavigationView bottomNav;
    DrawerLayout drawerLayout;
    AppBarLayout appBarLayout;
    MaterialToolbar toolbar;
    FrameLayout frameLayout;
    NavigationView drawerNavigationView;
    String ORDERS_URL = "https://codecanvas.top/WashWave/get_user_orders.php";

    //====================== Firebase Cloud Messing Methods Code Starts Here ======================

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
        askNotificationPermission();
        initFirebaseToken();
        askSMSPermission();
        initViews();
    }

    public void initViews() {
//        pickedImage = findViewById(R.id.pickedImage);
//        picImage = findViewById(R.id.picImage);
        welcomeText = findViewById(R.id.welcome);
//        viewAllNotices = findViewById(R.id.viewAllNotices);
        bottomNav = findViewById(R.id.bottomNav);
        drawerLayout = findViewById(R.id.drawerLayout);
        appBarLayout = findViewById(R.id.appBarLayout);
        toolbar = findViewById(R.id.toolBar);
        frameLayout = findViewById(R.id.frameLayout);
        drawerNavigationView = findViewById(R.id.drawerNavigationView);


        SharedPreferences preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        boolean rememberMe = preferences.getBoolean("rememberMe", false);
        String email = preferences.getString("email", null);

        // Initialize SessionManager
        sessionManager = new SessionManager(getApplicationContext());


        if (rememberMe && email != null) {
            sessionManager.isLoggedIn();
            getUserPreferences(email);
        }

//        if (rememberMe && email != null && sessionManager.isLoggedIn()) {
//            // Auto-login successful, show email or fetch data
//            welcomeText.setText("Welcome, " + email);
//            getUserPreferences(email);  // Load user data from server
//        }

        /*
        else {
            // Not remembered or session expired, redirect to login screen
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show();
            sessionManager.logout(); // Clear session
            Intent intent = new Intent(UserDashboard.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

         */


        bottomNav.getOrCreateBadge(R.id.notification).setNumber(9);


        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {


                    startActivity(new Intent(UserDashboard.this, UserDashboard.class));

                } else if (item.getItemId() == R.id.cart) {

                    Toast.makeText(UserDashboard.this, "Cart", Toast.LENGTH_SHORT).show();

                } else if (item.getItemId() == R.id.notification) {

                    bottomNav.removeBadge(R.id.notification);
                    Toast.makeText(UserDashboard.this, "Notification", Toast.LENGTH_SHORT).show();

                } else if (item.getItemId() == R.id.profile) {

                    Toast.makeText(UserDashboard.this, "Profile", Toast.LENGTH_SHORT).show();

                }


                return true;
            }
        });



/*
        welcomeText.setOnClickListener(v -> {
            sessionManager.logout();
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();  // Clear Remember Me preferences
            editor.apply();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

 */

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);

        welcomeText.setOnClickListener(v -> {

            startActivity(new Intent(this, Home.class));


        });

        drawerNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem item) {

                if (item.getItemId() == R.id.allOrders) {

                    Toast.makeText(UserDashboard.this, "All Orders", Toast.LENGTH_SHORT).show();

                } else if (item.getItemId() == R.id.pendingOrders) {
                    Toast.makeText(UserDashboard.this, "Pending Orders", Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.pickedOrder) {

                    Toast.makeText(UserDashboard.this, "Picked Orders", Toast.LENGTH_SHORT).show();

                } else if (item.getItemId() == R.id.offer) {
                    Toast.makeText(UserDashboard.this, "Offer", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

    }

    public void getUserPreferences(String email) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ORDERS_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    //result.setText("");  // Clear previous results

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject order = jsonArray.getJSONObject(i);

                        String itemName = order.getString("item_name");
                        int id = order.getInt("id");
                        int totalPrice = order.getInt("total_price");
                        int quantity = order.getInt("quantity");
                        String status = order.getString("status");

                        Toast.makeText(getApplicationContext(), "item name: " + itemName, Toast.LENGTH_SHORT).show();


                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(UserDashboard.this, "Error parsing Order Data JSon: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error parsing order data", Toast.LENGTH_SHORT).show();

            }
        }) {


            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", email); // Use the actual logged-in email
                return params;
            }


        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data, @androidx.annotation.NonNull ComponentCaller caller) {
        super.onActivityResult(requestCode, resultCode, data, caller);

        if (resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = null;

            try {
                if (requestCode == REQUEST_GALLERY) {
                    Uri selectedImage = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    pickedImage.setImageBitmap(bitmap);
                } else if (requestCode == REQUEST_CAMERA) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                    pickedImage.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }

            if (bitmap != null) {
                pickedImage.setImageBitmap(bitmap);

            }


        }


    }


 */

    public void initFirebaseToken() {

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w("firebaseToken", "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();
                Log.d("firebaseToken", token);
                //Toast.makeText(UserDashboard.this, token, Toast.LENGTH_SHORT).show();


            }
        });

    }


    //============================= Firebase Cloud Messaging Starts Here =============================

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.

                new AlertDialog.Builder(this).setTitle("Notification Permission").setMessage("This Permission is necessary to make the app more Personalize!").setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);

                    }
                }).setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //dialogInterface.dismiss();
                    }
                }).create().show();


            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    //================================ Firebase Cloud Messaging Methods Code Ends Here ======================


    //================================ Firebase Send Sms Remotely Methods Code Starts Here ======================

    private void askSMSPermission() {
        // This is only necessary for API level >= 7 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.

                new AlertDialog.Builder(this).setTitle("Notification Permission").setMessage("This Permission is necessary to make the app more Personalize!").setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);

                    }
                }).setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //dialogInterface.dismiss();
                    }
                }).create().show();


            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
            }
        }
    }

    //================================ Firebase Send Sms Remotely Methods Code Ends Here ======================

}