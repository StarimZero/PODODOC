package com.pododoc.app;

import static com.pododoc.app.RemoteService.CAMERA_REQUEST;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReviewReadActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference ref = storage.getReference();
    ImageView image;
    TextView email, date, rating;
    EditText contents;
    RatingBar ratingBar;
    String id = "";
    String writer = "";
    ReviewVO vo = new ReviewVO();
    String strFile = "";
    String prevFile= "";
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_read);
        getSupportActionBar().setTitle("리뷰:");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        writer = intent.getStringExtra("email");
        image = findViewById(R.id.image);
        email = findViewById(R.id.email);
        date = findViewById(R.id.date);
        rating = findViewById(R.id.rating);
        contents = findViewById(R.id.contents);
        ratingBar = findViewById(R.id.ratingBar);
        LinearLayout buttons = findViewById(R.id.buttons);

        getRead();

        if (user.getEmail().equals(writer)) {
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder box = new AlertDialog.Builder(ReviewReadActivity.this);
                    box.setMessage("리뷰사진을 수정하시겠습니까?");
                    box.setNegativeButton("앨범에서선택", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityResult.launch(intent);
                        }
                    });
                    box.setPositiveButton("카메라", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                        }
                    });
                    box.setNeutralButton("취소", null);
                    box.show();
                }
            });
        } else {
            buttons.setVisibility(View.INVISIBLE);
            contents.setEnabled(false);
            ratingBar.setIsIndicator(true);
        }


        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                ratingBar.setRating(v);
                rating.setText(String.valueOf(v));
                vo.setRating(v);
            }
        });

        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder box = new AlertDialog.Builder(ReviewReadActivity.this);
                box.setTitle("KOSMO");
                box.setMessage("리뷰를 삭제하시겠습니까?");
                box.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.collection("review").document(id).delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        finish();
                                    }
                                });
                    }
                });
                box.setNegativeButton("아니오", null);
                box.show();
            }
        });

        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder box = new AlertDialog.Builder(ReviewReadActivity.this);
                box.setMessage("리뷰를 수정하시겠습니까?");
                box.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(strFile.equals("")){
                            String fixedUrl = uri.toString();
                            vo.setPhoto(fixedUrl);
                            updateReview();
                        }else{
                            uploadPhoto(strFile);
                        }
                    }
                });
                box.setNegativeButton("아니오", null);
                box.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void getRead() {
        db.collection("review").document(id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot doc = task.getResult();
                        String strEmail = doc.getData().get("email").toString();
                        if (doc.getData().get("photo") == null) {
                            image.setImageResource(R.drawable.person);
                            vo.setPhoto(null);
                        } else {
                            prevFile = doc.getData().get("photo").toString();
                            Picasso.with(ReviewReadActivity.this).load(prevFile).into(image);
                            vo.setPhoto(doc.getData().get("photo").toString());
                            uri =Uri.parse(prevFile);
                            Log.i("imagePath", prevFile);
                        }
                        Log.i("photo",doc.getData().get("photo").toString());
                        email.setText(strEmail);
                        date.setText(doc.getData().get("date").toString());
                        contents.setText(doc.getData().get("contents").toString());
                        float floatrating = Float.parseFloat(doc.getData().get("rating").toString());
                        rating.setText(doc.getData().get("rating").toString());
                        ratingBar.setRating(floatrating);

                        if (!user.getEmail().equals(strEmail)) {
                            contents.setEnabled(false);
                            findViewById(R.id.buttons).setVisibility(View.INVISIBLE);
                            ratingBar.setIsIndicator(true);
                        }

                        vo.setId(id);
                        vo.setContents(contents.getText().toString());
                        vo.setEmail(email.getText().toString());
                        vo.setDate(date.getText().toString());
                        vo.setRating(floatrating);
                        vo.setIndex(Integer.parseInt(doc.getData().get("index").toString()));
                    }
                });
    }

    //앨범에서 이미지 선택
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == RESULT_OK) {
                        Cursor cursor = getContentResolver().query(o.getData().getData(), null, null, null, null);
                        cursor.moveToFirst();
                        int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        strFile = cursor.getString(index);
                        image.setImageBitmap(BitmapFactory.decodeFile(strFile));
                        cursor.close();
                        vo.setPhoto(strFile);
                    }
                }
            }
    );  //startActivityResult

    //카메라에서 촬영후 이미지 업로드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            // 카메라로 촬영된 이미지를 처리
            Bitmap photoBitmap = (Bitmap) data.getExtras().get("data");
            image.setImageBitmap(photoBitmap);
            // Bitmap을 파일로 저장
            try {
                // 임시 파일 생성
                File tempFile = createImageFile();
                FileOutputStream fos = new FileOutputStream(tempFile);
                photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                // 파일 경로를 strFile에 저장
                strFile = tempFile.getAbsolutePath();
                Log.i("strFile", strFile);
                vo.setPhoto(strFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 이미지 파일 생성 메서드
    private File createImageFile() throws IOException {
        // 파일 이름을 타임스탬프로 생성
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // 임시 파일을 앱의 캐시 디렉토리에 생성
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* 파일 이름 */
                ".jpg",         /* 파일 확장자 */
                storageDir      /* 저장할 디렉토리 */
        );
        return image;
    }

    public void updateReview() {
        vo.setContents(contents.getText().toString());
        Log.i("updatevo",vo.toString());
        db.collection("review")
                .document(id)
                .set(vo, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                    }
                });
    }

    private void uploadPhoto(String strFile) {
        Uri file = Uri.fromFile(new File(strFile));
        // Storage에 저장할 경로 설정
        String fileName = System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = ref.child("reviewPhoto/" + fileName);
        imageRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                vo.setPhoto(imageUrl);
                                updateReview();
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
}//activity