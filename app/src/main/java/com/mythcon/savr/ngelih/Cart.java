package com.mythcon.savr.ngelih;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mythcon.savr.ngelih.Common.Common;
import com.mythcon.savr.ngelih.Databases.Database;
import com.mythcon.savr.ngelih.Model.Order;
import com.mythcon.savr.ngelih.Model.Request;
import com.mythcon.savr.ngelih.ViewHolder.CartAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.hoang8f.widget.FButton;

/**
 * Daftar menu yang sebelumnya ditampung pada class CartAdapter
 * Daftar menu dari class CartAdapter yang nanti di PUSH dari class ini
 * Digunakan Untuk PUSH dari database local (SQLite) ke Firebase
 * PUSH ke Node Request
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

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
                showAlertDialog();
            }
        });

        loadListFood();
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step");
        alertDialog.setMessage("Enter yout address");

        final EditText edtAddress = new EditText(Cart.this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        edtAddress.setLayoutParams(layoutParams);
        alertDialog.setView(edtAddress);
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
                        cart
                );

                //save to Firebase
                requestFood.child(String.valueOf(System.currentTimeMillis())).setValue(request);

                //Delete Cart
                new Database(getBaseContext()).cleanCart();
                Toast.makeText(Cart.this, "Thank you, Order place", Toast.LENGTH_SHORT).show();
                finish();
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

    private void loadListFood() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart,this);
        recyclerView.setAdapter(adapter);

        int total = 0;
        for (Order order:cart){
            total += (Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
            Locale locale = new Locale("in","ID");
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);

            txtTotalPrice.setText(numberFormat.format(total));
        }
    }
}
