package com.shamir.ticketmaster;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // שם המשתמש לצורך הבדיקה - התלמידים יכולים לשנות לשמם
    private final String MY_NAME = "רוכש_" + (int)(Math.random() * 1000);

    private TextView tvTicketsCount;
    private Button btnBuyTicket;
    private ListView lvBuyers;

    private FirebaseFirestore db;
    private DocumentReference concertRef;

    private ArrayList<String> buyersList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        initDatabase();
        setupRealtimeListener();

        btnBuyTicket.setOnClickListener(v -> handleBuyTicket());
    }

    private void initUI() {
        tvTicketsCount = findViewById(R.id.tvTicketsCount);
        btnBuyTicket = findViewById(R.id.btnBuyTicket);
        lvBuyers = findViewById(R.id.lvBuyers);

        buyersList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, buyersList);
        lvBuyers.setAdapter(adapter);
    }

    private void initDatabase() {
        db = FirebaseFirestore.getInstance();
        // הצבעה ישירה למסמך המופע המשותף
        concertRef = db.collection("events").document("oasis");
    }

    private void setupRealtimeListener() {
        // מאזין חי לעדכון המונה ורשימת הרוכשים על המסך
        concertRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Toast.makeText(MainActivity.this, "שגיאה בקריאת נתונים", Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Event event = snapshot.toObject(Event.class);
                if (event != null) {
                    tvTicketsCount.setText("כרטיסים נותרים: " + event.getAvailableTickets());

                    buyersList.clear();
                    if (event.getBuyers() != null) {
                        buyersList.addAll(event.getBuyers());
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void handleBuyTicket() {
        // =========================================================================
        // TODO חלק א' (המימוש הנאיבי):
        // 1. קראו את המסמך באמצעות concertRef.get()
        // 2. המירו את ה-snapshot לאובייקט מסוג Event
        // 3. בדקו: האם availableTickets > 0?
        //    - אם כן: הפחיתו 1, הוסיפו את MY_NAME ל-buyers, ושמרו באמצעות concertRef.set(event)
        //    - אם לא: הציגו Toast שהכרטיסים אזלו
        //
        // TODO חלק ב' (לאחר בדיקת העומס הכיתתית):
        // החליפו את הקוד ב-db.runTransaction(...) לסנכרון אטומי
        // =========================================================================

        Toast.makeText(this, "יש להשלים את לוגיקת הקנייה!", Toast.LENGTH_SHORT).show();
    }
}