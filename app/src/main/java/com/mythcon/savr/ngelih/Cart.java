package com.mythcon.savr.ngelih;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mythcon.savr.ngelih.Common.Common;
import com.mythcon.savr.ngelih.Databases.Database;
import com.mythcon.savr.ngelih.Model.MyResponse;
import com.mythcon.savr.ngelih.Model.Notification;
import com.mythcon.savr.ngelih.Model.Order;
import com.mythcon.savr.ngelih.Model.Request;
import com.mythcon.savr.ngelih.Model.Sender;
import com.mythcon.savr.ngelih.Model.Token;
import com.mythcon.savr.ngelih.Remote.APIService;
import com.mythcon.savr.ngelih.ViewHolder.CartAdapter;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Daftar menu yang sebelumnya ditampung pada class CartAdapter
 * Daftar menu dari class CartAdapter yang nanti di PUSH dari class ini
 * Digunakan Untuk PUSH dari database local (SQLite) ke Firebase
 * PUSH ke Node Request
 * test clone and branch success
 */

public class Cart extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requestFood;

    TextView txtTotalPrice;
    FButton btnPlace;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    APIService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //init Service
        service = Common.getFCMService();

        //init Firebase
        database = FirebaseDatabase.getInstance();
        requestFood = database.getReference("Request");

        recyclerView = (RecyclerView) findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = (TextView) findViewById(R.id.total);
        btnPlace = (FButton) findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this, "Your chart is empty !!", Toast.LENGTH_SHORT).show();
            }
        });

        loadListFood();
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step");
        alertDialog.setMessage("Enter yout address");

        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment,null);

        final MaterialEditText edtAddress = order_address_comment.findViewById(R.id.edtAddress);
        final MaterialEditText edtComment = order_address_comment.findViewById(R.id.edtComment);

        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Request
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        edtAddress.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        "0",
                        edtComment.getText().toString(),
                        cart
                );

                //save to Firebase
                String order_number = String.valueOf(System.currentTimeMillis());
                requestFood.child(order_number).setValue(request);

                //Delete Cart
                new Database(getBaseContext()).cleanCart();

                sendNotification(order_number);
//                Toast.makeText(Cart.this, "Thank you, Order place", Toast.LENGTH_SHORT).show();
//                finish();
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void sendNotification(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query dataToken = tokens.orderByChild("isServerToken").equalTo(true);
        dataToken.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    Token serverToken = postSnapshot.getValue(Token.class);
                    //memanggil RetrofitClient Notification Method
                    Notification notification = new Notification("NgelihWarung","You have new order"+order_number);
                    Sender content = new Sender(serverToken.getToken(),notification);

                    service.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                   if (response.code() == 200) {
                                       if (response.body().success == 1) {
                                           Toast.makeText(Cart.this, "Thank you, Order place", Toast.LENGTH_SHORT).show();
                                           finish();
                                       } else {
                                           Toast.makeText(Cart.this, "Failed !!!", Toast.LENGTH_SHORT).show();
                                       }
                                   }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.d("Error ",t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        int total = 0;
        if (cart.size() > 0) {
            for (Order order : cart) {
                total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
                Locale locale = new Locale("in", "ID");
                NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);

                txtTotalPrice.setText(numberFormat.format(total));
            }
        }
        else {
            Locale locale = new Locale("in", "ID");
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);

            txtTotalPrice.setText(numberFormat.format(total));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int order) {
        cart.remove(order);     //remove list by position

        new Database(this).cleanCart();     //delete data from SQLite
        for (Order item:cart){                      //Update data
            new Database(this).addCart(item);
        }

        loadListFood();
    }
}
