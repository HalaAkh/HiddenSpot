package com.hiddenspot.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hiddenspot.app.R;
import com.hiddenspot.app.utils.FirebaseHelper;

public class AuthActivity extends AppCompatActivity {

    private boolean isLogin = true;
    private TextInputLayout tilUsername;
    private TextInputEditText etUsername, etEmail, etPassword;
    private MaterialButton btnAction;
    private TextView tvTitle, tvSubtitle, tvForgot, tvToggleLabel, tvToggleAction;
    private FirebaseHelper firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        firebase       = FirebaseHelper.getInstance();
        tilUsername    = findViewById(R.id.til_username);
        etUsername     = findViewById(R.id.et_username);
        etEmail        = findViewById(R.id.et_email);
        etPassword     = findViewById(R.id.et_password);
        btnAction      = findViewById(R.id.btn_action);
        tvTitle        = findViewById(R.id.tv_title);
        tvSubtitle     = findViewById(R.id.tv_subtitle);
        tvForgot       = findViewById(R.id.tv_forgot);
        tvToggleLabel  = findViewById(R.id.tv_toggle_label);
        tvToggleAction = findViewById(R.id.tv_toggle_action);

        btnAction.setOnClickListener(v -> {
            String email    = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            btnAction.setEnabled(false);
            if (isLogin) {
                firebase.signIn(email, password, v2 -> goToMain(), e -> showError(e.getMessage()));
            } else {
                String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "User";
                firebase.signUp(email, password, username, v2 -> goToMain(), e -> showError(e.getMessage()));
            }
        });

        tvToggleAction.setOnClickListener(v -> toggleMode());

        tvForgot.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            if (email.isEmpty()) { Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show(); return; }
            firebase.getAuth().sendPasswordResetEmail(email)
                    .addOnSuccessListener(v2 -> Toast.makeText(this, "Reset email sent!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        findViewById(R.id.btn_google).setOnClickListener(v ->
                Toast.makeText(this, "Google sign-in coming soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_apple).setOnClickListener(v ->
                Toast.makeText(this, "Apple sign-in coming soon", Toast.LENGTH_SHORT).show());
    }

    private void toggleMode() {
        isLogin = !isLogin;
        if (isLogin) {
            tvTitle.setText(R.string.welcome_back);
            tvSubtitle.setText(R.string.sign_in_subtitle);
            btnAction.setText(R.string.sign_in);
            tilUsername.setVisibility(View.GONE);
            tvForgot.setVisibility(View.VISIBLE);
            tvToggleLabel.setText(R.string.no_account);
            tvToggleAction.setText(R.string.sign_up);
        } else {
            tvTitle.setText(R.string.join_hiddenspot);
            tvSubtitle.setText(R.string.sign_up_subtitle);
            btnAction.setText(R.string.create_account);
            tilUsername.setVisibility(View.VISIBLE);
            tvForgot.setVisibility(View.GONE);
            tvToggleLabel.setText(R.string.have_account);
            tvToggleAction.setText(R.string.sign_in);
        }
        btnAction.setEnabled(true);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void showError(String msg) {
        runOnUiThread(() -> {
            btnAction.setEnabled(true);
            btnAction.setText(isLogin ? getString(R.string.sign_in) : getString(R.string.create_account));
            Toast.makeText(this, msg != null ? msg : "Authentication failed", Toast.LENGTH_LONG).show();
        });
    }
}
