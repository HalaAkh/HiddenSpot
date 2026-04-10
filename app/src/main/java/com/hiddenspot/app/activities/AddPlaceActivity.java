package com.hiddenspot.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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
import com.hiddenspot.app.R;
import com.hiddenspot.app.models.Place;
import com.hiddenspot.app.utils.FirebaseHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
    private Bitmap capturedBitmap = null;
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
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY && data != null) {
                selectedImageUri = data.getData();
                capturedBitmap = null;
                if (selectedImageUri != null) {
                    Glide.with(this).load(selectedImageUri).centerCrop().into(ivPreview);
                    ivPreview.setVisibility(View.VISIBLE);
                }
            } else if (requestCode == REQUEST_CAMERA && data != null && data.getExtras() != null) {
                capturedBitmap = (Bitmap) data.getExtras().get("data");
                selectedImageUri = null;
                if (capturedBitmap != null) {
                    ivPreview.setImageBitmap(capturedBitmap);
                    ivPreview.setVisibility(View.VISIBLE);
                }
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
        if (user == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Processing Image...");

        String base64Image = "";
        if (selectedImageUri != null) {
            base64Image = encodeImageToBase64(selectedImageUri);
        } else if (capturedBitmap != null) {
            base64Image = encodeBitmapToBase64(capturedBitmap);
        }

        savePlace(name, city, address, phone, desc, user, base64Image);
    }

    private String encodeImageToBase64(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();
            return encodeBitmapToBase64(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String encodeBitmapToBase64(Bitmap bitmap) {
        try {
            // Resize to max 600px to keep Firestore document size small
            int maxDimension = 600;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);
            
            int finalWidth = Math.round(ratio * width);
            int finalHeight = Math.round(ratio * height);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 70, baos); // 70% quality
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void savePlace(String name, String city, String address, String phone,
                           String desc, FirebaseUser user, String imageData) {
        btnSubmit.setText("Saving to Cloud...");
        List<String> images = new ArrayList<>();
        if (!imageData.isEmpty()) images.add(imageData);
        String displayName = user.getDisplayName() != null ? user.getDisplayName() : "Anonymous";
        
        Place place = new Place(name, city, address, phone, desc,
                selectedCategory, images, user.getUid(), displayName);
                
        FirebaseHelper.getInstance().addGem(place,
                docRef -> runOnUiThread(() -> {
                    Toast.makeText(this, "Gem added successfully!", Toast.LENGTH_LONG).show();
                    finish();
                }),
                e -> runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Gem");
                    Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
