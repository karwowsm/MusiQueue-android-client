package pl.com.karwowsm.musiqueue.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;

import pl.com.karwowsm.musiqueue.BuildConfig;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.controller.UserAccountController;
import pl.com.karwowsm.musiqueue.api.error.ErrorResponse;
import pl.com.karwowsm.musiqueue.api.request.UserAccountCreateRequest;

public class RegisterActivity extends AbstractActivity {

    private EditText usernameEditText;
    private TextView usernameErrorTextView;
    private EditText emailEditText;
    private TextView emailErrorTextView;
    private EditText passwordEditText;
    private TextView passwordErrorTextView;
    private EditText passwordConfirmationEditText;
    private TextView passwordConfirmationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        usernameEditText = findViewById(R.id.username_et);
        usernameErrorTextView = findViewById(R.id.username_error_tv);
        emailEditText = findViewById(R.id.email_et);
        emailErrorTextView = findViewById(R.id.email_error_tv);
        passwordEditText = findViewById(R.id.password_et);
        passwordErrorTextView = findViewById(R.id.password_error_tv);
        passwordConfirmationEditText = findViewById(R.id.password_confirmation_et);
        passwordConfirmationTextView = findViewById(R.id.password_confirmation_error_tv);
        TextView agreementTextView = findViewById(R.id.agreement_tv);
        String termsOfService = String.format("<a href=\"%s/legal/terms_of_service.html\">Terms of Service</a>", BuildConfig.BASE_URL);
        String privacyPolicy = String.format("<a href=\"%s/legal/privacy_policy.html\">Privacy Policy</a>", BuildConfig.BASE_URL);
        String agreement = getString(R.string.agreement, termsOfService, privacyPolicy);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            agreementTextView.setText(Html.fromHtml(agreement, Html.FROM_HTML_MODE_COMPACT));
        } else {
            agreementTextView.setText(Html.fromHtml(agreement));
        }
        agreementTextView.setMovementMethod(LinkMovementMethod.getInstance());
        setBarsColor(R.color.colorPrimary);
    }

    public void onRegister(View view) {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String passwordConfirmation = passwordConfirmationEditText.getText().toString();
        String email = emailEditText.getText().toString().trim();
        String emptyFieldError = getString(R.string.empty_field);

        usernameErrorTextView.setText(username.isEmpty() ? emptyFieldError : null);
        passwordErrorTextView.setText(password.isEmpty() ? emptyFieldError : null);
        passwordConfirmationTextView.setText(passwordConfirmation.isEmpty() ? emptyFieldError : null);
        emailErrorTextView.setText(email.isEmpty() ? emptyFieldError : null);

        List<String> inputValues = Arrays.asList(username, password, passwordConfirmation, email);
        if (inputValues.stream().noneMatch(String::isEmpty)) {
            register(UserAccountCreateRequest.builder()
                .username(username)
                .password(password)
                .passwordConfirmation(passwordConfirmation)
                .email(email)
                .build());
        }
    }

    private void register(UserAccountCreateRequest request) {
        showProgressDialog(R.string.registering);
        UserAccountController.register(request, response -> {
            showToast(R.string.registration_success);
            finish();
        }, errorResponse -> {
            if (errorResponse != null) {
                if (errorResponse.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {
                    for (ErrorResponse.Error error : errorResponse.getErrors()) {
                        if (error.getField().equals(UserAccountCreateRequest.Fields.username)) {
                            usernameErrorTextView.setText(error.getDefaultMessage());
                        } else if (error.getField().equals(UserAccountCreateRequest.Fields.password)) {
                            passwordErrorTextView.setText(error.getDefaultMessage());
                        } else if (error.getField().equals(UserAccountCreateRequest.Fields.passwordConfirmation)) {
                            passwordConfirmationTextView.setText(error.getDefaultMessage());
                        } else if (error.getField().equals(UserAccountCreateRequest.Fields.email)) {
                            emailErrorTextView.setText(error.getDefaultMessage());
                        } else {
                            showToast(error.getDefaultMessage());
                        }
                    }
                } else {
                    showToast(errorResponse.getMessage());
                }
            } else {
                showToast(R.string.server_error);
            }
            hideProgressDialog();
        });
    }

    public void onLogin(View view) {
        finish();
    }
}
