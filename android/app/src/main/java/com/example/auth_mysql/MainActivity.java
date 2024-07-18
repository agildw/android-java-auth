package com.example.auth_mysql;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    ApiService apiService;
    TextView id, name, email, createdAt;
    Button btnLogout;



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

        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        id = findViewById(R.id.account_id);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        createdAt = findViewById(R.id.createdAt);
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove("token");
                editor.apply();
                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
            }
        });


        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        }

        Call<User> call = apiService.me("Bearer " + token);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    if (response.body() == null) {
                        startActivity(new Intent(MainActivity.this, Login.class));
                        finish();
                    }
                    User user = response.body();
                    id.setText("ID: " + user.getId());
                    name.setText("Name: " + user.getName());
                    email.setText("Email: " + user.getEmail());
                    createdAt.setText("Created At: " + user.getCreatedAt());
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        String error = jObjError.getString("error");
                        if (error.equals("Unauthorized")) {
                            startActivity(new Intent(MainActivity.this, Login.class));
                            finish();
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }
}