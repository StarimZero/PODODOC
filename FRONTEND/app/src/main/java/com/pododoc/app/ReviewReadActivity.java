package com.pododoc.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReviewReadActivity extends AppCompatActivity {
    FirebaseFirestore db =FirebaseFirestore.getInstance();
    FirebaseAuth auth= FirebaseAuth.getInstance();
    FirebaseUser user= auth.getCurrentUser();
    ImageView image;
    TextView email,date, rating;
    EditText contents;
    RatingBar ratingBar;
    String id ="";
    ReviewVO vo = new ReviewVO();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_read);
        getSupportActionBar().setTitle("리뷰:");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        id= intent.getStringExtra("id");

        image = findViewById(R.id.image);
        email=findViewById(R.id.email);
        date=findViewById(R.id.date);
        rating= findViewById(R.id.rating);
        contents= findViewById(R.id.contents);
        ratingBar=findViewById(R.id.ratingBar);

        getRead();

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
                AlertDialog.Builder box= new AlertDialog.Builder(ReviewReadActivity.this);
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
                box.setNegativeButton("아니오",null);
                box.show();
            }
        });

        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder box= new AlertDialog.Builder(ReviewReadActivity.this);
                box.setMessage("리뷰를 수정하시겠습니까?");
                box.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        vo.setContents(contents.getText().toString());
                        db.collection("review")
                                .document(id)
                                .set(vo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        finish();
                                    }
                                });
                    }
                });
                box.setNegativeButton("아니오",null);
                box.show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void getRead(){
        db.collection("review").document(id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot doc= task.getResult();
                        String strEmail = doc.getData().get("email").toString();
                        image.setImageResource(Integer.parseInt( doc.getData().get("photo").toString() ));
                        email.setText(strEmail);
                        date.setText(doc.getData().get("date").toString());
                        contents.setText(doc.getData().get("contents").toString());
                        float floatrating = Float.parseFloat( doc.getData().get("rating").toString() );
                        rating.setText(doc.getData().get("rating").toString());
                        ratingBar.setRating(floatrating);

                        if(!user.getEmail().equals(strEmail)){
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
}//activity