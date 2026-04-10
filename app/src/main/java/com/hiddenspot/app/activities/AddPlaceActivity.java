package com.hiddenspot.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.hiddenspot.app.R;
import com.hiddenspot.app.models.Place;
import com.hiddenspot.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class AddPlaceActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 100;
    private static final int REQUEST_CAMERA  = 101;
    private static final int REQUEST_PERM    = 200;

    private TextInputEditText etPlaceName, etCity, etAddress, etPhone, etDescription;
    private Spinner spinnerCategory;
    private MaterialButton btnSubmit;
    private ImageButton btnBack;
    private LinearLayout btnUploadGallery, btnOpenCamera;
    private ImageView ivPreview;
    private Uri selectedImageUri = null;
    private String selectedCategory = "Restaurant";

    private static final String[] CATEGORIES = {
            "Restaurant", "Garden", "Café", "Viewpoint",
            "Park", "Beach", "Library", "Shop", "Historical"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        bindViews();
        setupSpinner();
        setupListeners();
    }

    private void bindViews() {
        etPlaceName      = findViewById(R.id.et_place_name);
        etCity           = findViewById(R.id.et_city);
        etAddress        = findViewById(R.id.et_address);
        etPhone          = findViewById(R.id.et_phone);
        etDescription    = findViewById(R.id.et_description);
        spinnerCategory  = findViewById(R.id.spinner_category);
        btnSubmit        = findViewById(R.id.btn_submit);
        btnBack          = findViewById(R.id.btn_back);
        btnUploadGallery = findViewById(R.id.btn_upload_gallery);
        btnOpenCamera    = findViewById(R.id.btn_open_camera);
        ivPreview        = findViewById(R.id.iv_preview);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, CATEGORIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedCategory = CATEGORIES[pos];
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnUploadGallery.setOnClickListener(v -> checkPermissionAndPickGallery());
        btnOpenCamera.setOnClickListener(v -> checkPermissionAndOpenCamera());
        btnSubmit.setOnClickListener(v -> submitPlace());
    }

    private void checkPermissionAndPickGallery() {
        String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED)
            openGallery();
        else
            ActivityCompat.requestPermissions(this, new String[]{perm}, REQUEST_PERM);
    }

    private void checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) openCamera();
        else ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, REQUEST_PERM + 1);
    }

    private void openGallery() {
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_GALLERY);
    }

    private void openCamera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) startActivityForResult(i, REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Glide.with(this).load(selectedImageUri).centerCrop().into(ivPreview);
                ivPreview.setVisibility(View.VISIBLE);
            }
        }
    }

    private void submitPlace() {
        String name = getText(etPlaceName), city = getText(etCity);
        String address = getText(etAddress), phone = getText(etPhone);
        String desc = getText(etDescription);
        if (name.isEmpty() || city.isEmpty() || address.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser user = FirebaseHelper.getInstance().getCurrentUser();
        if (user == null) { Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show(); return; }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting…");

        if (selectedImageUri != null) {
            StorageReference ref = FirebaseHelper.getInstance().getImageUploadRef();
            ref.putFile(selectedImageUri)
                    .addOnSuccessListener(snap -> ref.getDownloadUrl()
                            .addOnSuccessListener(uri -> savePlace(name, city, address, phone, desc, user, uri.toString()))
                            .addOnFailureListener(e -> savePlace(name, city, address, phone, desc, user, "")))
                    .addOnFailureListener(e -> savePlace(name, city, address, phone, desc, user, ""));
        } else {
            savePlace(name, city, address, phone, desc, user, "");
        }
    }

    private void savePlace(String name, String city, String address, String phone,
                           String desc, FirebaseUser user, String imageUrl) {
        List<String> images = new ArrayList<>();
        if (!imageUrl.isEmpty()) images.add(imageUrl);
        String displayName = user.getDisplayName() != null ? user.getDisplayName() : "Anonymous";
        Place place = new Place(name, city, address, phone, desc,
                selectedCategory, images, user.getUid(), displayName);
        FirebaseHelper.getInstance().addGem(place,
                docRef -> runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.submit_success), Toast.LENGTH_LONG).show();
                    finish();
                }),
                e -> runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText(getString(R.string.submit_btn));
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }));
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(req, perms, results);
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            if (req == REQUEST_PERM)     openGallery();
            if (req == REQUEST_PERM + 1) openCamera();
        } else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
    }
}
