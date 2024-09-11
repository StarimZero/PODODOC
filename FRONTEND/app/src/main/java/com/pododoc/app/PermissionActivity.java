package com.pododoc.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;

public class PermissionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        getSupportActionBar().hide();

        if(permissionCheck()){
            Intent intent = new Intent(PermissionActivity.this, IndexActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //필요한 권한을 확인하고 부여되어 있지 않으면 권한을 요청한다.
    public boolean permissionCheck() {
        String[] permissions= {
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.READ_MEDIA_VIDEO,
        };
        ArrayList<String> checkPermission = new ArrayList<>();
        for(String permission:permissions){
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                checkPermission.add(permission);
            }
        }
        if(!checkPermission.isEmpty()){
            String[] requestPermission=checkPermission.toArray(new String[checkPermission.size()]);
            ActivityCompat.requestPermissions(this, requestPermission, 100);
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isAll = true;
        for(int i=0; i < permissions.length; i++) {
            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isAll = false;
                Log.d("Permission", "Permission denied: " + permissions[i]);
                break;
            }
        }
        if(isAll) {
            Intent intent = new Intent(this, IndexActivity.class);
            startActivity(intent);
            finish();
        }else {
            Toast.makeText(this, "모든 권한을 허용해야 앱을 사용할 수 있습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}