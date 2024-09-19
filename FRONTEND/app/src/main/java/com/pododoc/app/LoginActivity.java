package com.pododoc.app;

import static com.pododoc.app.RemoteService.BASE_URL;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    EditText email, pass;
    Button login,join;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("LOGIN");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email = findViewById(R.id.email);
        pass = findViewById(R.id.pass);
        join = findViewById(R.id.join);

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strEmail = email.getText().toString();
                String strPass = pass.getText().toString();

                mAuth.createUserWithEmailAndPassword(strEmail,strPass)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(LoginActivity.this,"가입성공", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(LoginActivity.this,"가입실패", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strEmail = email.getText().toString();
                String strPass = pass.getText().toString();

                mAuth.signInWithEmailAndPassword(strEmail, strPass)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(LoginActivity.this, "로그인성공", Toast.LENGTH_SHORT).show();
                                    // 로그인한 사용자의 이메일 가져오기
                                    String userEmail = mAuth.getCurrentUser().getEmail();
                                    if (userEmail != null) {
                                        // 이메일을 서버로 전송
                                        sendEmailToServer(userEmail);
                                    }
                                    finish();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(LoginActivity.this, "로그인실패", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }//oncreate

    private void sendEmailToServer(String email) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            // JSON 형식으로 이메일 데이터 준비
            String json = "{\"email\":\"" + email + "\"}";
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);

            // 서버로 POST 요청
            Request request = new Request.Builder()
                    .url(BASE_URL+"api/receive-email")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                // 서버 응답 처리 (로그 출력)
                Log.d("EmailSend", response.body().string());
            } catch (IOException e) {
                Log.e("EmailSend", "Error sending email", e);
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }//아이템셀렉




}//activity