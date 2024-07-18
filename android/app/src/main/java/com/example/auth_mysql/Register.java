package com.example.auth_mysql;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;


public class Register extends AppCompatActivity {
    TextInputEditText textName, textEmail, textPassword,textConfirmPassword;
    Button btnRegister;
    TextView loginRedirectButton;
    ApiService apiService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        textName = findViewById(R.id.name);
        textEmail = findViewById(R.id.email);
        textPassword = findViewById(R.id.password);
        textConfirmPassword = findViewById(R.id.confirm_password);
        btnRegister = findViewById(R.id.register);
        loginRedirectButton = findViewById(R.id.login);

        loginRedirectButton.setOnClickListener(v -> {
            startActivity(new Intent(Register.this, Login.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> {
            String email = textEmail.getText().toString();
            String password = textPassword.getText().toString();
            String confirmPassword = textConfirmPassword.getText().toString();
            String name = textName.getText().toString();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                textEmail.setError("Email is required");
                textPassword.setError("Password is required");
                textConfirmPassword.setError("Confirm Password is required");
                return;
            }

            if (!password.equals(confirmPassword)) {
                textPassword.setError("Password does not match");
                textConfirmPassword.setError("Password does not match");
                return;
            }

            register(name,email, password);

        });
    }

    private void register(String name, String email, String password) {
        Call<RegisterResponse> call = apiService.register(name, email, password);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    RegisterResponse registerResponse = response.body();
                    if (registerResponse == null) {
                        Toast.makeText(Register.this, "An error occurred", Toast.LENGTH_LONG).show();
                        return;
                    }
                    SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    String token = registerResponse.getToken();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("token", token);
                    editor.apply();
                    startActivity(new Intent(Register.this, MainActivity.class));
                    finish();
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(Register.this, jObjError.getString("error"), Toast.LENGTH_LONG).show();
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                Toast.makeText(Register.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


}