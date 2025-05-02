package com.example.ungdungnongsan;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class UserListActivity extends AppCompatActivity {
    private RecyclerView rvUsers;
    private UserAdapter adapter;
    private List<Users> userList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();

        adapter = new UserAdapter(userList, new UserAdapter.OnUserActionListener() {
            @Override
            public void onEdit(int position) {
                showUserDialog(userList.get(position), position);
            }

            @Override
            public void onDelete(int position) {
                String emailKey = userList.get(position).getEmail().replace(".", "_");
                usersRef.child(emailKey).removeValue();
                userList.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(UserListActivity.this, "Đã xoá tài khoản", Toast.LENGTH_SHORT).show();
            }
        });

        rvUsers.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");

        loadUsers();

        findViewById(R.id.btnAddUser).setOnClickListener(v -> showUserDialog(null, -1));
    }

    private void loadUsers() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Users user = userSnapshot.getValue(Users.class);
                    if (user != null) userList.add(user);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserListActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserDialog(@Nullable Users user, int position) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_user, null);
        EditText edtEmail = dialogView.findViewById(R.id.edtEmail);
        EditText edtPassword = dialogView.findViewById(R.id.edtPassword);
        EditText edtName = dialogView.findViewById(R.id.edtName);
        EditText edtPhone = dialogView.findViewById(R.id.edtPhone);
        EditText edtAddress = dialogView.findViewById(R.id.edtAddress);
        EditText edtRole = dialogView.findViewById(R.id.edtRole);

        if (user != null) {
            edtEmail.setText(user.getEmail());
            edtEmail.setEnabled(false); // Không cho sửa email
            edtPassword.setVisibility(View.GONE); // Không hiển thị mật khẩu khi sửa
            edtName.setText(user.getName());
            edtPhone.setText(user.getPhone());
            edtAddress.setText(user.getAddress());
            edtRole.setText(user.getRole());
        }

        new AlertDialog.Builder(this)
                .setTitle(user == null ? "Thêm Tài Khoản" : "Sửa Tài Khoản")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String email = edtEmail.getText().toString().trim();
                    String password = edtPassword.getText().toString().trim();
                    String name = edtName.getText().toString().trim();
                    String phone = edtPhone.getText().toString().trim();
                    String address = edtAddress.getText().toString().trim();
                    String role = edtRole.getText().toString().trim();

                    if (email.isEmpty() || (user == null && password.isEmpty())) {
                        Toast.makeText(this, "Email và mật khẩu không được trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (user == null) {
                        // THÊM MỚI -> dùng Firebase Authentication tạo account
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        String userId = task.getResult().getUser().getUid();

                                        Users newUser = new Users(userId, email, password, name, phone, address);
                                        newUser.setRole(role);

                                        usersRef.child(userId).setValue(newUser);

                                        userList.add(newUser);
                                        adapter.notifyItemInserted(userList.size() - 1);

                                        Toast.makeText(this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        // SỬA -> chỉ cập nhật thông tin database
                        Users updatedUser = new Users(user.getUserId(), email, user.getPassword(), name, phone, address);
                        updatedUser.setRole(role);

                        usersRef.child(user.getUserId()).setValue(updatedUser);

                        userList.set(position, updatedUser);
                        adapter.notifyItemChanged(position);

                        Toast.makeText(this, "Cập nhật tài khoản thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

}
