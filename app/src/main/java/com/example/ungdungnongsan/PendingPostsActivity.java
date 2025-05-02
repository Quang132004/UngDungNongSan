package com.example.ungdungnongsan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PendingPostsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PendingPostAdapter adapter;
    private List<Product> pendingProductList = new ArrayList<>();
    private DatabaseReference productRef;
    private ActivityResultLauncher<Intent> editProductLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_posts);

        recyclerView = findViewById(R.id.recyclerPendingPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PendingPostAdapter(this, pendingProductList, new PendingPostAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Product product) {
                Intent intent = new Intent(PendingPostsActivity.this, AddEditProductActivity.class);
                intent.putExtra("product", product); // Product cần implements Serializable hoặc Parcelable
                editProductLauncher.launch(intent); // Dùng launcher thay cho startActivity
            }

            @Override
            public void onDeleteClick(Product product) {
                confirmDelete(product);
            }
        });

        recyclerView.setAdapter(adapter);

        productRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("products");

        setupEditProductLauncher();
        fetchPendingPosts();
    }

    private void setupEditProductLauncher() {
        editProductLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Reload lại danh sách sau khi sửa xong
                        fetchPendingPosts();
                    }
                }
        );
    }

    private void fetchPendingPosts() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pendingProductList.clear();

                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    for (DataSnapshot productSnapshot : categorySnapshot.getChildren()) {
                        Product product = productSnapshot.getValue(Product.class);
                        if (product != null && currentUserId.equals(product.getUserId())) {
                            product.setKey(productSnapshot.getKey());
                            product.setCategory(categorySnapshot.getKey()); // rất quan trọng để xoá/sửa
                            pendingProductList.add(product);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PendingPostsActivity.this, "Lỗi tải bài đăng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc muốn xoá sản phẩm \"" + product.getName() + "\"?")
                .setPositiveButton("Xoá", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteProduct(Product product) {
        if (product.getCategory() == null || product.getKey() == null) {
            Toast.makeText(this, "Thông tin sản phẩm không đầy đủ để xoá!", Toast.LENGTH_SHORT).show();
            return;
        }

        productRef.child(product.getCategory()).child(product.getKey())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xoá sản phẩm", Toast.LENGTH_SHORT).show();
                    pendingProductList.remove(product);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi xoá sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
