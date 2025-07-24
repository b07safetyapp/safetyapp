package com.safetyapp.mainapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends BaseActivity {
    RecyclerView recyclerView;
    ContactAdapter adapter;
    List<Contact> contactList;
    DatabaseReference databaseRef;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_contact;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new ContactAdapter(contactList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        databaseRef = FirebaseDatabase.getInstance().getReference("contacts");

        // Realtime listener
        databaseRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactList.clear();
                for (DataSnapshot contactSnap : snapshot.getChildren()) {
                    Contact contact = contactSnap.getValue(Contact.class);
                    assert contact != null;
                    contact.id = contactSnap.getKey(); // Store Firebase key
                    contactList.add(contact);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContactActivity.this, "Failed to load contacts.", Toast.LENGTH_SHORT).show();
            }
        });

        attachSwipeToDelete();
    }

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh1,
                                  @NonNull RecyclerView.ViewHolder vh2) {
                return false; // no reordering
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                Contact contact = contactList.get(pos);

                if (direction == ItemTouchHelper.LEFT) {
                    // Swipe Left to DELETE
                    new AlertDialog.Builder(ContactActivity.this)
                            .setTitle("Delete Contact")
                            .setMessage("Are you sure you want to delete " + contact.name + "from your contacts?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                databaseRef.child(contact.id).removeValue();
                                contactList.remove(pos);
                                adapter.notifyItemRemoved(pos);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                adapter.notifyItemChanged(pos); // Undo swipe
                            })
                            .setCancelable(false)
                            .show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // 🟢 Swipe Right to EDIT
                    showEditDialog(contact, pos);
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(findViewById(R.id.recyclerView));
    }

    private void showEditDialog(Contact contact, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit, null);

        EditText inputName = dialogView.findViewById(R.id.etContactName);
        EditText inputRelationship = dialogView.findViewById(R.id.etContactRel);
        EditText inputPhone = dialogView.findViewById(R.id.etContactPhone);

        inputName.setText(contact.name);
        inputRelationship.setText(contact.relationship);
        inputPhone.setText(contact.phone);

        new AlertDialog.Builder(this)
                .setTitle("Edit Contact")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    contact.name = inputName.getText().toString().trim();
                    contact.relationship = inputRelationship.getText().toString().trim();
                    contact.phone = inputPhone.getText().toString().trim();

                    databaseRef.child(contact.id).setValue(contact);
                    contactList.set(position, contact);
                    adapter.notifyItemChanged(position);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    adapter.notifyItemChanged(position); // Undo swipe
                })
                .setCancelable(false)
                .show();
    }
}
