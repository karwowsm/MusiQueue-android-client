package pl.com.karwowsm.musiqueue.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import pl.com.karwowsm.musiqueue.Constants;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.TokenHolder;
import pl.com.karwowsm.musiqueue.api.controller.LoginController;
import pl.com.karwowsm.musiqueue.api.request.TokenCreateRequest;

public class LoginActivity extends AbstractActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameEditText = findViewById(R.id.username_et);
        passwordEditText = findViewById(R.id.password_et);
        setBarsColor(R.color.colorPrimary);
    }

    public void onLogin(View view) {
        loginUser(TokenCreateRequest.builder()
            .username(usernameEditText.getText().toString().trim())
            .password(passwordEditText.getText().toString())
            .build());
    }

    public void onSignUp(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private void loginUser(final TokenCreateRequest request) {
        showProgressDialog(R.string.logging_in);
        LoginController.login(request, token -> {
            hideProgressDialog();
            saveToken(token.getAccess_token());
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, error -> {
            hideProgressDialog();
            showToast(error != null
                ? error.getMessage()
                : getString(R.string.server_error));
        });
    }

    private void saveToken(String token) {
        getSharedPreferences(Constants.AUTH_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(Constants.PREF_TOKEN, token)
            .apply();
        TokenHolder.setToken(token);
    }
}
