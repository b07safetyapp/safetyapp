/*
package com.safetyapp.mainapp; // 1. Package where this helper lives

import com.google.firebase.auth.FirebaseAuth;          // 2. Firebase Auth for current user
import com.google.firebase.database.DataSnapshot;       // 3. Snapshot of data from database
import com.google.firebase.database.DatabaseError;      // 4. Database error handling
import com.google.firebase.database.DatabaseReference;  // 5. Reference to a location in database
import com.google.firebase.database.FirebaseDatabase;   // 6. Entry point for Firebase Realtime Database
import com.google.firebase.database.ValueEventListener; // 7. Listener for realtime updates

public class FirebaseHelper {                           // 8. Class declaration

    private final DatabaseReference ref;                // 9. Reference to user's answers node

    public FirebaseHelper() {                           // 10. Constructor
        // 11. Get current user ID from FirebaseAuth
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // 12. Build reference to /responses/{uid}
        ref = FirebaseDatabase.getInstance()
                .getReference("responses")
                .child(uid);
    }

    public void save(String qId, Object answer) {       // 13. Save helper method
        ref.child(qId).setValue(answer);                // 14. Write value to DB
    }

    public void addListener(ValueEventListener listener) { // 15. Add listener
        ref.addValueEventListener(listener);             // 16. Listen for changes
    }

    public void removeListener(ValueEventListener listener) { // 17. Remove listener
        ref.removeEventListener(listener);              // 18. Stop listening
    }
}
*/