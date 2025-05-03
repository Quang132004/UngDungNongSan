package com.example.ungdungnongsan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.*;

import java.util.List;

public class AdminProductAdapterAll extends RecyclerView.Adapter<AdminProductAdapterAll.ProductViewHolder> {

    private List<Product> productList;
    private Context context;

    public AdminProductAdapterAll(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_product_manage_all, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText("Giá: " + product.getPrice() + " VNĐ");

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context).load(product.getImageUrl()).into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_image); // ảnh mặc định nếu không có
        }

        // Trạng thái duyệt
        if (product.isApproved()) {
            holder.tvStatus.setText("Trạng thái: Đã duyệt");
            holder.tvStatus.setTextColor(Color.GREEN);
            holder.btnApprove.setVisibility(View.GONE); // ẩn nút duyệt nếu đã duyệt
        } else {
            holder.tvStatus.setText("Trạng thái: Chưa duyệt");
            holder.tvStatus.setTextColor(Color.RED);
            holder.btnApprove.setVisibility(View.VISIBLE);
        }

        // Truy vấn người đăng
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(product.getUserId());

        userRef.child("email").get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String email = dataSnapshot.getValue(String.class);
                holder.tvUserId.setText("Người đăng: " + email);
            } else {
                holder.tvUserId.setText("Người đăng: Không rõ");
            }
        }).addOnFailureListener(e -> {
            holder.tvUserId.setText("Người đăng: Lỗi");
        });

        // Xử lý nút "Duyệt"
        holder.btnApprove.setOnClickListener(v -> {
            DatabaseReference productRef = FirebaseDatabase.getInstance()
                    .getReference("products")
                    .child(product.getCategory())
                    .child(product.getKey());

            productRef.child("approved").setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Đã duyệt sản phẩm", Toast.LENGTH_SHORT).show();
                        product.setApproved(true);
                        notifyItemChanged(position);
                    });
        });

        // Xử lý nút "Sửa"
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddEditProductActivity.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
        });

        // Xử lý nút "Xoá"
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xoá sản phẩm")
                    .setMessage("Bạn có chắc chắn muốn xoá sản phẩm này không?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        DatabaseReference productRef = FirebaseDatabase.getInstance()
                                .getReference("products")
                                .child(product.getCategory())
                                .child(product.getKey());

                        productRef.removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Đã xoá sản phẩm", Toast.LENGTH_SHORT).show();
                                    productList.remove(position);
                                    notifyItemRemoved(position);
                                });
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvUserId, tvStatus;
        Button btnEdit, btnDelete, btnApprove;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnApprove = itemView.findViewById(R.id.btnApprove);
        }
    }
}
