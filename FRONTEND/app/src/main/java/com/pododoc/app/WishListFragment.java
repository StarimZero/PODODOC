package com.pododoc.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;


public class WishListFragment extends Fragment {
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser user=mAuth.getCurrentUser();
    FirebaseDatabase db=FirebaseDatabase.getInstance();
    ArrayList<WineVO> array=new ArrayList<>();
    WishAdapter adapter = new WishAdapter();
    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_wish_list, container, false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            getList();
            ListView list = view.findViewById(R.id.list);
            list.setAdapter(adapter);
        } else {
            Intent intent = new Intent(getActivity(),LoginActivity.class);
            startActivity(intent);
        }
        return view;
    }

    public void getList(){
        array.clear();
        DatabaseReference ref=db.getReference("/like/" + user.getUid());
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                WineVO vo=snapshot.getValue(WineVO.class);
                array.add(vo);
                adapter.notifyDataSetChanged();
                Log.i("vo", vo.toString());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                WineVO vo = snapshot.getValue(WineVO.class);
                for(WineVO c : array){
                    if (c.getIndex()==(vo.getIndex())){
                        array.remove(c);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }//create

    class WishAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return array.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View itemView = getLayoutInflater().inflate(R.layout.item_mywine,viewGroup,false);
            ImageView image, flag;
            TextView name, country, winery, grape, region, rating, date;
            CardView card;
            RatingBar ratingBar;
            WineVO vo = array.get(i);

            image=itemView.findViewById(R.id.photo);
            winery=itemView.findViewById(R.id.winery);
            name=itemView.findViewById(R.id.name);
            grape=itemView.findViewById(R.id.grape);
            region=itemView.findViewById(R.id.region);
            country=itemView.findViewById(R.id.country);
            flag=itemView.findViewById(R.id.flag);
            card=itemView.findViewById(R.id.list);
            rating=itemView.findViewById(R.id.ratingScore);
            ratingBar=itemView.findViewById(R.id.ratingBar);
            date= itemView.findViewById(R.id.date);

            Picasso.with(getActivity()).load(vo.getWineImage()).into(image);
            winery.setText(vo.getWineWinery());
            name.setText(vo.getWineName());
            grape.setText(vo.getWineType());
            region.setText(vo.getWineRegion());
            country.setText(vo.getWineCountry());
            rating.setText(String.format("%.1f", vo.getWineRating()));
            ratingBar.setRating(vo.getWineRating());
            int index = vo.getIndex();
            date.setText("");
            date.setCompoundDrawablesWithIntrinsicBounds(R.drawable.delete,0,0,0);

            String strCountry = vo.getWineCountry().toString().toLowerCase().replace(" ", "");
            TypedArray icons = getResources().obtainTypedArray(R.array.flags);
            String[] countries = getResources().getStringArray(R.array.countries);
            int flagIndex = Arrays.asList(countries).indexOf(strCountry);
            if (flagIndex >= 0) {
                flag.setImageDrawable(icons.getDrawable(flagIndex));
            } else {
                flag.setImageResource(R.drawable.flag); // 기본 이미지
            }
            icons.recycle();

            date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder box = new AlertDialog.Builder(getActivity());
                    box.setMessage("즐겨찾기를 해제하시겠습니까?");
                    box.setPositiveButton("아니요",null);
                    box.setNegativeButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DatabaseReference ref=db.getReference("/like/" + user.getUid() + "/" + index);
                            ref.removeValue();
                            Toast.makeText(getActivity(), "삭제성공", Toast.LENGTH_SHORT).show();
                        }
                    });
                    box.show();
                }
            });

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ReadActivity.class);
                    intent.putExtra("index",index);
                    startActivity(intent);
                }
            });

            return itemView;
        }
    }
}