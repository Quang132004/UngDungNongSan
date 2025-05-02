package com.example.ungdungnongsan;

import java.io.Serializable;

public class Product implements Serializable {
	private String id;
	private String name;
	private String imageUrl;
	private String price;
	private String description;
	private String origin;
	private String ingredients;
	private String key;
	private String category;
	private String userId; // Dùng userId thay cho idSeller
	private int quantity;
	private boolean isApproved;

	public Product() {
		this.isApproved = false;
	}

	public Product(String id, String name, String imageUrl, String price, String description, String origin,
				   String ingredients, String key, String category, String userId, int quantity) {
		this.id = id;
		this.name = name;
		this.imageUrl = imageUrl;
		this.price = price;
		this.description = description;
		this.origin = origin;
		this.ingredients = ingredients;
		this.key = key;
		this.category = category;
		this.userId = userId;
		this.quantity = quantity;
		this.isApproved = false;
	}

	public Product(String name, String imageUrl, String price, String description, String origin, String ingredients) {
		this.name = name;
		this.imageUrl = imageUrl;
		this.price = price;
		this.description = description;
		this.origin = origin;
		this.ingredients = ingredients;
		this.isApproved = false;
	}

	// Getter và Setter
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }

	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }

	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public String getOrigin() { return origin; }
	public void setOrigin(String origin) { this.origin = origin; }

	public String getIngredients() { return ingredients; }
	public void setIngredients(String ingredients) { this.ingredients = ingredients; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getImageUrl() { return imageUrl; }
	public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

	public String getPrice() { return price; }
	public void setPrice(String price) { this.price = price; }

	public int getQuantity() { return quantity; }
	public void setQuantity(int quantity) { this.quantity = quantity; }

	public boolean isApproved() { return isApproved; }
	public void setApproved(boolean approved) { isApproved = approved; }
}
