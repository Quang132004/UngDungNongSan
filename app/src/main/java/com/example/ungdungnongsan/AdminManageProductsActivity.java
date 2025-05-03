package com.example.ungdungnongsan;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminManageProductsActivity extends AppCompatActivity {
    private RecyclerView rvAllProducts;
    private AdminProductAdapterAll adapter;
    private List<Product> productList;
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_products);

        rvAllProducts = findViewById(R.id.rvAllProducts);
        rvAllProducts.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        adapter = new AdminProductAdapterAll(productList, this);
        rvAllProducts.setAdapter(adapter);

        productsRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("products");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllProducts(); // Luôn làm mới khi quay lại
    }

    private void loadAllProducts() {
        productList.clear();
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String categoryName = categorySnapshot.getKey();

                    for (DataSnapshot productSnapshot : categorySnapshot.getChildren()) {
                        Product product = productSnapshot.getValue(Product.class);
                        if (product != null) {
                            product.setKey(productSnapshot.getKey());
                            product.setCategory(categoryName);

                            // Ghi đè thêm thông tin userId nếu chưa có
                            String userId = productSnapshot.child("userId").getValue(String.class);
                            product.setUserId(userId != null ? userId : "");

                            productList.add(product);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
