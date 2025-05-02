package com.example.ungdungnongsan;

import android.content.Context;
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
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PendingPostAdapter extends RecyclerView.Adapter<PendingPostAdapter.PostViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    public PendingPostAdapter(Context context, List<Product> productList, OnItemClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pending_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(product.getPrice() + " VNĐ");
        holder.tvStatus.setText(product.isApproved() ? "Đã duyệt" : "Chờ duyệt");

        // Load ảnh sản phẩm
        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.app_logo) // Ảnh tạm khi chưa load xong
                .error(R.drawable.app_logo) // Ảnh lỗi nếu load fail
                .into(holder.ivImage);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(product);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvStatus;
        Button btnEdit, btnDelete;
        ImageView ivImage;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            ivImage = itemView.findViewById(R.id.ivProductImage);
        }
    }
}
