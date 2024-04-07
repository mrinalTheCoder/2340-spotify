package com.example.spotifywrapped;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

public class ViewPastWrappedActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Map<String, Object>> user_wrapped_data;
    private ArrayList<String> docIds = new ArrayList<>();
    private LinearLayout linearLayout;

    private DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_past_wrapped);

        linearLayout = findViewById(R.id.container);
        getFirestoreData();
    }

    private void getFirestoreData() {
        user_wrapped_data = new ArrayList<>();
        docIds = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db.collection(currentUser.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("firestore_query", document.getId() + " => " + document.getData());
                                user_wrapped_data.add(document.getData());
                                docIds.add(document.getId());
                            }
                            repopulateCards();
                        } else {
                            Log.d("firestore_query", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void repopulateCards() {
        linearLayout.removeAllViews();
        for (int i = 0; i < user_wrapped_data.size(); i++) {
            Map<String, Object> datum = user_wrapped_data.get(i);
            View cardView = getLayoutInflater().inflate(R.layout.card_view, null);
            cardView.setTag("cardview" + i);

            ImageButton deleteButton = cardView.findViewById(R.id.btnDelete);
            deleteButton.setTag("btnDelete" + i);

            int finalI = i;
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    db.collection(currentUser.getUid()).document(docIds.get(finalI)).delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("firestore_delete", "DocumentSnapshot successfully deleted!");
                                    getFirestoreData();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("firestore_delete", "Error deleting document", e);
                                }
                            });
                }
            });
            TextView textTimestamp = cardView.findViewById(R.id.textTimestamp);
            TextView textTopArtist = cardView.findViewById(R.id.textTopArtist);
            TextView textTopSong = cardView.findViewById(R.id.textTopSong);

            // textTimestamp.setText(df.format(datum.get("time")));
            textTimestamp.setText(datum.get("time").toString());
            textTopArtist.setText(
                    "Top Artist: " +
                    ((ArrayList<String>) datum.get("topArtists")).get(0)
            );
            textTopSong.setText(
                    "Top Song: " +
                    ((ArrayList<String>) datum.get("topSongs")).get(0)
            );
            linearLayout.addView(cardView);
        }
    }
}