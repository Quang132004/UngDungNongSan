package com.example.ungdungnongsan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;

public class ProductDetailActivity extends AppCompatActivity {

	private TextView tvName, tvPrice, tvDescription, tvOrigin, tvIngredients;
	private TextView tvQuantity;
	private ImageView ivProduct;
	private int quantity = 1; // Khởi tạo giá trị mặc định là 1 cho số lượng sản phẩm
	private Product currentProduct;

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

		// Khởi tạo TextView và các Button liên quan đến số lượng
		tvQuantity = findViewById(R.id.tvQuantity);
		findViewById(R.id.btnIncrease).setOnClickListener(v -> {
			quantity++;  // Tăng số lượng lên 1
			tvQuantity.setText(String.valueOf(quantity));  // Cập nhật hiển thị
		});

		findViewById(R.id.btnDecrease).setOnClickListener(v -> {
			if (quantity > 1) {  // Đảm bảo không giảm xuống dưới 1
				quantity--;  // Giảm số lượng xuống 1
				tvQuantity.setText(String.valueOf(quantity));  // Cập nhật hiển thị
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

		setupAddToCartButton();
	}

	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return true;
	}

	private void setupAddToCartButton() {
		findViewById(R.id.btnAddToCart).setOnClickListener(v -> {
			if (currentProduct != null) {
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
