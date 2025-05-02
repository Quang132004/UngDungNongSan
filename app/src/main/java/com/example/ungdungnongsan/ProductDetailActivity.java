package com.example.ungdungnongsan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

	private TextView tvName, tvPrice, tvDescription, tvOrigin, tvIngredients;
	private TextView edQuantity;
	private ImageView ivProduct;
	private int quantity = 1; // Khởi tạo giá trị mặc định là 1 cho số lượng sản phẩm
	private Product currentProduct;
	private RecyclerView rvSimilarProducts;

	@SuppressLint("MissingInflatedId")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_detail);

		// Set up the toolbar
		findViewById(R.id.ivCart).setOnClickListener(v -> {
			Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
			startActivity(intent);
		});

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("");

		// Khởi tạo các view
		tvName = findViewById(R.id.tvName);
		tvPrice = findViewById(R.id.tvPrice);
		tvDescription = findViewById(R.id.tvDescription);
		tvOrigin = findViewById(R.id.tvOrigin);
		tvIngredients = findViewById(R.id.tvIngredients);
		ivProduct = findViewById(R.id.ivProduct);
		rvSimilarProducts = findViewById(R.id.rvSimilarProducts);
		rvSimilarProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
		edQuantity = findViewById(R.id.edQuantity);
		edQuantity.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus) { // Khi người dùng rời khỏi ô nhập liệu
				try {
					int inputQuantity = Integer.parseInt(edQuantity.getText().toString());
					int maxQuantity = currentProduct.getQuantity(); // Lấy số lượng tối đa từ sản phẩm
					if (inputQuantity > maxQuantity) {
						Toast.makeText(this, "Chỉ còn " + maxQuantity + " sản phẩm trong kho", Toast.LENGTH_SHORT).show();
						quantity = maxQuantity;
					} else if (inputQuantity < 1) {
						Toast.makeText(this, "Số lượng không thể nhỏ hơn 1", Toast.LENGTH_SHORT).show();
						quantity = 1;
					} else {
						quantity = inputQuantity;
					}
					edQuantity.setText(String.valueOf(quantity)); // Cập nhật hiển thị
				} catch (NumberFormatException e) {
					Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
					edQuantity.setText(String.valueOf(quantity)); // Khôi phục giá trị trước đó
				}
			}
		});
		findViewById(R.id.btnIncrease).setOnClickListener(v -> {
			if (quantity < currentProduct.getQuantity()) { // Kiểm tra số lượng không vượt quá số lượng trong kho
				quantity++;  // Tăng số lượng lên 1
				edQuantity.setText(String.valueOf(quantity));  // Cập nhật hiển thị
			} else {
				Toast.makeText(this, "Không thể tăng thêm, đã đạt số lượng tối đa trong kho", Toast.LENGTH_SHORT).show();
			}
		});

		findViewById(R.id.btnDecrease).setOnClickListener(v -> {
			if (quantity > 1) {  // Đảm bảo không giảm xuống dưới 1
				quantity--;  // Giảm số lượng xuống 1
				edQuantity.setText(String.valueOf(quantity));  // Cập nhật hiển thị
			}
		});

		// Lấy thông tin sản phẩm từ intent
		currentProduct = (Product) getIntent().getSerializableExtra("product");

		if (currentProduct != null) {
			tvName.setText(currentProduct.getName());
			tvPrice.setText("Giá: " + currentProduct.getPrice());
			tvDescription.setText("Mô tả: " + currentProduct.getDescription());
			tvOrigin.setText("Nguồn gốc: " + currentProduct.getOrigin());
			tvIngredients.setText("Thành phần: " + currentProduct.getIngredients());

			// Tải hình ảnh sản phẩm
			Glide.with(this).load(currentProduct.getImageUrl()).into(ivProduct);
		}
		loadSimilarProducts(currentProduct.getCategory());
		setupAddToCartButton();
	}
	private void loadSimilarProducts(String category) {
		if (category == null) {
			return;
		}

		DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
				.getReference("products").child(category);

		databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				List<Product> similarProducts = new ArrayList<>();
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					try {
						Product product = snapshot.getValue(Product.class);
						if (product != null && currentProduct != null &&
								currentProduct.getKey() != null && product.getKey() != null &&
								!product.getKey().equals(currentProduct.getKey())) {
							similarProducts.add(product);
						}
					} catch (Exception e) {
						Log.e("ProductDetail", "Error loading similar product: " + e.getMessage());
					}
				}
				ProductAdapter adapter = new ProductAdapter(ProductDetailActivity.this, similarProducts);
				rvSimilarProducts.setAdapter(adapter);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				Toast.makeText(ProductDetailActivity.this, "Failed to load similar products", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return true;
	}

	private void setupAddToCartButton() {
		findViewById(R.id.btnAddToCart).setOnClickListener(v -> {
			if (currentProduct != null) {
				// Update quantity from EditText before proceeding
				try {
					String quantityText = edQuantity.getText().toString();
					if (!quantityText.isEmpty()) {
						quantity = Integer.parseInt(quantityText);
					}
				} catch (NumberFormatException e) {
					// In case of invalid input, keep current quantity
				}

				int maxQuantity = currentProduct.getQuantity();
				if (quantity > maxQuantity) {
					Toast.makeText(this, "Chỉ còn " + maxQuantity + " sản phẩm trong kho", Toast.LENGTH_SHORT).show();
					quantity = maxQuantity;
					edQuantity.setText(String.valueOf(quantity));
					return;
				}

				// Thêm sản phẩm vào giỏ hàng với số lượng
				for (int i = 0; i < quantity; i++) {
					CartManager.getInstance().addToCart(currentProduct);
				}
				Toast.makeText(this, "Đã thêm " + quantity + " sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
				startActivity(intent);
			} else {
				Toast.makeText(this, "Không thể thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
			}
		});
	}
}
