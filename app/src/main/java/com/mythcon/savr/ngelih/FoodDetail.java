package com.mythcon.savr.ngelih;

import android.graphics.Typeface;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mythcon.savr.ngelih.Common.Common;
import com.mythcon.savr.ngelih.Databases.Database;
import com.mythcon.savr.ngelih.Model.Food;
import com.mythcon.savr.ngelih.Model.Order;
import com.mythcon.savr.ngelih.Model.Rating;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

/**
 * Menampilkan Detail menu dari list menu
 * Memasukkan makanan ke dalam chart
 * Memberikan Rating terhadap makanan
 */

public class FoodDetail extends AppCompatActivity implements RatingDialogListener{

    TextView food_name, food_price, food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart,btnRating;
    ElegantNumberButton numberButton;
    RatingBar ratingBar;

    String foodId="";
    Food currentFood;

    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference rates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        //inisialisasi Firebase

        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");
        rates = database.getReference("Rating");

        numberButton = (ElegantNumberButton) findViewById(R.id.number_button);
        btnCart = (FloatingActionButton) findViewById(R.id.btn_cart);
        btnRating = (FloatingActionButton) findViewById(R.id.btn_rating);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addCart(new Order(
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount())
                );
                Toast.makeText(FoodDetail.this, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });

        food_name = (TextView) findViewById(R.id.food_name);
        food_description = (TextView) findViewById(R.id.food_description);
        food_price = (TextView) findViewById(R.id.food_price);
        food_image = (ImageView) findViewById(R.id.img_food);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        //Get From Intent
        if (getIntent()!= null)
            foodId = getIntent().getStringExtra("FoodId");
        if (!foodId.isEmpty())
        {
            if (Common.isConnectedToInternet(getBaseContext())) {
                getDetailFood(foodId);
                getRatingFood(foodId);
            }
            else{
                Toast.makeText(FoodDetail.this, "Please check you internet connection !!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void getRatingFood(String foodId) {
        Query foodRating = rates.orderByChild("foodId").equalTo(foodId);

        foodRating.addValueEventListener(new ValueEventListener() {
            int count=0,sum=0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum+=Integer.parseInt(item.getRateValue());
                    count++;
                }
                if (count != 0) {
                    float average = sum / count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quite Ok","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Berikan kami bintang dan berikan feedbackmu")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Isikan komentar anda...")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingFadeAnim)
                .create(FoodDetail.this)
                .show();
    }

    private void getDetailFood(final String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);

                Picasso.with(getBaseContext()).load(currentFood.getImage())
                        .into(food_image);
                Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
                collapsingToolbarLayout.setTitle(currentFood.getName());

                collapsingToolbarLayout.setCollapsedTitleTypeface(typeface);
                collapsingToolbarLayout.setExpandedTitleTypeface(typeface);

                food_price.setText(currentFood.getPrice());
                food_name.setText(currentFood.getName());
                food_name.setVisibility(View.GONE);
                food_description.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPositiveButtonClicked(int value, final String comments) {
        //Get Rating dan uPload to firebase
        final Rating rating = new Rating(Common.currentUser.getPhone(),
                foodId,
                String.valueOf(value),
                comments);

        rates.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Common.currentUser.getPhone()).exists()){
                    rates.child(Common.currentUser.getPhone()).removeValue();

                    rates.child(Common.currentUser.getPhone()).setValue(rating);
                }
                else {
                    rates.child(Common.currentUser.getPhone()).setValue(rating);
                }
                Toast.makeText(FoodDetail.this, "Thank you for submit rating :)", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }
}
