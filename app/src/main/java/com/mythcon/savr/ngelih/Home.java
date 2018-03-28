package com.mythcon.savr.ngelih;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mythcon.savr.ngelih.Common.Common;
import com.mythcon.savr.ngelih.Interface.ItemClickListener;
import com.mythcon.savr.ngelih.Model.Category;
import com.mythcon.savr.ngelih.Model.Token;
import com.mythcon.savr.ngelih.Service.ListenOrder;
import com.mythcon.savr.ngelih.ViewHolder.MenuViewHolder;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;

    TextView textFullName;

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    ShimmerFrameLayout shimmerContainer;

    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        //init Paper
        Paper.init(this);

        //init FIrebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");
        shimmerContainer = (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container); //buat animasi loading parsing data

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent cartIntent = new Intent(Home.this,Cart.class);
               startActivity(cartIntent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set name for user
        View headerView = navigationView.getHeaderView(0);
        textFullName = (TextView) headerView.findViewById(R.id.textFullName);
        textFullName.setText(Common.currentUser.getName());

        //Load menu
        recycler_menu = (RecyclerView) findViewById(R.id.recycler_menu);
        recycler_menu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);

/*        //Register Service
        Intent service = new Intent(Home.this, ListenOrder.class);
        startService(service);
*/
        //Menambahkan Token Saat Login
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void updateToken(String token) { //Menambahkan Token Saat Login
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token dataToken = new Token(token,false);  //false karena token dikirim dari Client
        tokens.child(Common.currentUser.getPhone()).setValue(dataToken);
    }

    private void loadMenu() {
        shimmerContainer.startShimmerAnimation();
        if (Common.isConnectedToInternet(this)) {

            adapter = new FirebaseRecyclerAdapter<Category,
                    MenuViewHolder>(Category.class, R.layout.menu_item, MenuViewHolder.class, category) {
                @Override
                protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {
                    viewHolder.textMenuName.setText(model.getName());
                    Picasso.with(getBaseContext()).load(model.getImage())
                            .into(viewHolder.imageView);

                    shimmerContainer.stopShimmerAnimation();  //Stop animation
                    shimmerContainer.setVisibility(View.GONE);

                    final Category clickitem = model;
                    viewHolder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onclick(View view, int position, boolean isLongClick) {
                            Intent foodlist = new Intent(Home.this, FoodList.class);
                            //get Category ID
                            foodlist.putExtra("CategoryId", adapter.getRef(position).getKey());
                            startActivity(foodlist);
                        }
                    });
                }
            };
            recycler_menu.setAdapter(adapter);

        }else {
            Toast.makeText(this, "Please check you internet connection !!", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh)
            loadMenu();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_cart) {
            Intent cartIntent = new Intent(Home.this,Cart.class);
            startActivity(cartIntent);
        } else if (id == R.id.nav_orders) {
            Intent orderIntent = new Intent(Home.this,OrderStatus.class);
            startActivity(orderIntent);
        } else if (id == R.id.nav_log_out) {

            //Delete Remember user and pass
            Paper.book().destroy();

            //logout
            Intent signIn = new Intent(Home.this, SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        shimmerContainer.startShimmerAnimation();
        loadMenu();
    }

    @Override
    protected void onPause() {
        shimmerContainer.stopShimmerAnimation();
        super.onPause();
    }
}
