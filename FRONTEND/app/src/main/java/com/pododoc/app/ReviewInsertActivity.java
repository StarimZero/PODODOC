package com.pododoc.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReviewInsertActivity extends AppCompatActivity {
    ReviewVO vo = new ReviewVO();
    RatingBar ratingBar;
    TextView contents, rating;
    ImageView photo;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    FirebaseStorage storage= FirebaseStorage.getInstance();
    StorageReference ref = storage.getReference();

    String strFile="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_insert);
        getSupportActionBar().setTitle("리뷰등록");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        String strIndex = intent.getStringExtra("index");
        vo.setIndex(intent.getIntExtra("index", 0));
        vo.setEmail(user.getEmail());

        ratingBar = findViewById(R.id.ratingBar);
        rating = findViewById(R.id.rating);
        contents = findViewById(R.id.contents);

        // 레이팅바 체인지 리스너
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                rating.setText(String.valueOf(v));
                vo.setRating(v);
            }
        });
        photo=findViewById(R.id.photo);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder box= new AlertDialog.Builder(ReviewInsertActivity.this);
                box.setMessage("사진등록방법");
                box.setNegativeButton("앨범에서선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityResult.launch(intent);
                    }
                });
                box.setPositiveButton("카메라",null);
                box.show();

            }
        });

        findViewById(R.id.insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contents.getText().toString().isEmpty() ) {
                    Toast.makeText(ReviewInsertActivity.this, "내용을 입력하세요!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (ratingBar.getRating() == 0.0) {
                    Toast.makeText(ReviewInsertActivity.this, "점수를 입력하세요!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (!strFile.equals("")) {
                        uploadPhoto(strFile);
                    } else {
                        vo.setPhoto(url);
                        insertReview();// 이미지가 없으면 빈 URL로 처리
                    }
                }
            }
        });
    }

    public void insertReview(){
        vo.setContents(contents.getText().toString());
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        vo.setDate(sdf.format(date));
        db.collection("review")
            .add(vo)
            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ReviewInsertActivity.this, "리뷰가 등록되었습니다!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
    }

    //앨범에서 이미지 선택
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if(o.getResultCode() == RESULT_OK) {
                        Cursor cursor=getContentResolver().query(o.getData().getData(), null, null, null, null);
                        cursor.moveToFirst();
                        int index=cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        strFile = cursor.getString(index);
                        photo.setImageBitmap(BitmapFactory.decodeFile(strFile));
                        cursor.close();
                    }
                }
            }
    );  //startActivityResult

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadPhoto(String strFile){
        Uri file = Uri.fromFile(new File(strFile));
        // Storage에 저장할 경로 설정
        String fileName=System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = ref.child("reviewPhoto/"+ fileName);
        imageRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                vo.setPhoto(imageUrl);
                                insertReview();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Upload", "Failed to upload file: " + e.getMessage());
                    }
                });

    }
}