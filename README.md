# ספרינט מערכות מבוזרות: TicketMaster 🎟️

**נושא:** תחרות על משאב משותף (Race Conditions) ופעולות אטומיות (Transactions) ב-Cloud Firestore  
**סביבת פיתוח:** Android Studio (Java) + Firebase Firestore  

---

## 1. רקע: המודל העסקי (Keep It Simple)

בספרינט זה אנחנו מנהלים אירוע מכירת כרטיסים להופעה מבוקשת.  
כל הנתונים מרוכזים במסמך יחיד בענן, שמיוצג באפליקציה על ידי מחלקת POJO פשוטה ומוכרת:

### מחלקת המודל: `Event.java`
```java
import java.util.ArrayList;
import java.util.List;

public class Event {
    private int availableTickets;
    private List<String> buyers;

    // בנאי ריק - חובה עבור Firestore
    public Event() {
        this.buyers = new ArrayList<>();
    }

    public Event(int availableTickets) {
        this.availableTickets = availableTickets;
        this.buyers = new ArrayList<>();
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    public void setAvailableTickets(int availableTickets) {
        this.availableTickets = availableTickets;
    }

    public List<String> getBuyers() {
        return buyers;
    }

    public void setBuyers(List<String> buyers) {
        this.buyers = buyers;
    }
}
```

---

## 2. תשתית התצוגה: `ListView` בזמן אמת

בדיוק כפי שעשינו בפרויקט הלוח/צ'אט הכיתתי, נשתמש ב-`ListView` וב-`ArrayAdapter` פשוט.  
בזכות העובדה שכל הנתונים יושבים במסמך אחד, **מאזין יחיד** מעדכן בו-זמנית גם את מונה הכרטיסים וגם את רשימת הרוכשים:

```java
private TextView tvTickets;
private ListView lvBuyers;
private ArrayList<String> buyersList;
private ArrayAdapter<String> adapter;
private FirebaseFirestore db;
private DocumentReference eventRef;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initUI();
    initDatabase();
}

private void initUI() {
    tvTickets = findViewById(R.id.tvTickets);
    lvBuyers = findViewById(R.id.lvBuyers);
    Button btnBuy = findViewById(R.id.btnBuy);

    buyersList = new ArrayList<>();
    adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, buyersList);
    lvBuyers.setAdapter(adapter);

    btnBuy.setOnClickListener(v -> handleBuyTicket());
}

private void initDatabase() {
    db = FirebaseFirestore.getInstance();
    eventRef = db.collection("events").document("oasis");

    // האזנה רציפה למסמך האירוע בלייב
    eventRef.addSnapshotListener((snapshot, error) -> {
        if (error != null || snapshot == null || !snapshot.exists()) return;

        Event event = snapshot.toObject(Event.class);
        if (event != null) {
            tvTickets.setText("כרטיסים שנותרו: " + event.getAvailableTickets());

            // עדכון רשימת הרוכשים על המסך
            buyersList.clear();
            if (event.getBuyers() != null) {
                buyersList.addAll(event.getBuyers());
            }
            adapter.notifyDataSetChanged();
        }
    });
}
```

---

## 3. חלק א': מימוש אלגוריתם הרכישה הנאיבי 💥

### המשימה שלכם:
בתוך הפונקציה `handleBuyTicket()`, כתבו בעצמכם את לוגיקת הרכישה הרגילה והמוכרת (בדיקה -> עדכון בזיכרון -> כתיבה לענן):

1. בצעו קריאה חד-פעמית של המסמך באמצעות `eventRef.get()`.
2. המירו את הנתון שהתקבל לאובייקט: `Event event = snapshot.toObject(Event.class)`.
3. בדקו:
   * **אם `event.getAvailableTickets() > 0`:**
     - הפחיתו 1 ממספר הכרטיסים (`setAvailableTickets`).
     - הוסיפו את שימכם לרשימת הרוכשים (`event.getBuyers().add("שם התלמיד")`).
     - שימרו את האובייקט המעודכן חזרה בענן באמצעות `eventRef.set(event)`.
     - הציגו הודעת `Toast`: *"כרטיס נרכש בהצלחה!"*.
   * **אם אין כרטיסים פנויים:**
     - הציגו הודעת `Toast`: *"הכרטיסים אזלו!"*.

> 🧪 **בדיקה אישית:** הריצו את האפליקציה במכשיר שלכם. לחצו על הכפתור וודאו שהמספר יורד ושמכם מתווסף ל-`ListView`.

---

## 4. חלק ב': מבחן העומס  (Live Stress Test) ⚠️

**עצרו והמתינו להנחיית המורה!**

המורה יאפס כעת את המסמך בענן:
* `availableTickets` = **5 כרטיסים בלבד**.
* `buyers` = `[]` (רשימה ריקה).

בספירה לאחור: יחד לוחצים על **"קנה כרטיס"** בדיוק באותו שבריר שנייה.

### שאלות לניתוח הכשל (ענו במחברת/עורך):
1. כמה כרטיסים נותרו לפי התצוגה בענן? האם המספר הגיוני ביחס לכמות הלחיצות?
2. כמה שמות מופיעים בפועל ב-`ListView`? איפה שאר התלמידים שלחצו?
3. **מדוע נמחקו נתונים (Data Loss)?** הסבירו את בעיית **Read-Modify-Write** בהקשר של הרשימה והמונה.

---

## 5. חלק ג': התיקון ההנדסי – פעולה אטומית (Transaction) 🛡️

כדי למנוע דריסות הדדיות, כל שרשרת הפעולות (קריאת המצב הקיים, החישוב בזיכרון, והשמירה) חייבת להתבצע כ**מקשה אחת בלתי ניתנת להפרדה (Atomic Operation)**.

אם הנתונים בענן ישתנו בזמן שהאפליקציה שלכם חישבה את המצב החדש, מנגנון ה-Transaction יבטל את הפעולה וינסה אותה שוב אוטומטית (**Optimistic Concurrency Control**).

### החלפת לוגיקת הרכישה בקוד חסין:

```java
private void handleBuyTicket() {
    String myName = "שם התלמיד"; // החליפו בשמכם

    db.runTransaction(transaction -> {
        // שלב 1: קריאת המצב הנוכחי מתוך הטרנזקציה (READ)
        DocumentSnapshot snapshot = transaction.get(eventRef);
        Event event = snapshot.toObject(Event.class);

        if (event == null || event.getAvailableTickets() <= 0) {
            // זריקת שגיאה ייעודית תבטל את כל הטרנזקציה
            throw new FirebaseFirestoreException("Sold Out", FirebaseFirestoreException.Code.ABORTED);
        }

        // שלב 2: עדכון הנתונים בזיכרון
        int updatedTickets = event.getAvailableTickets() - 1;
        List<String> updatedBuyers = event.getBuyers();
        if (updatedBuyers == null) {
            updatedBuyers = new ArrayList<>();
        }
        updatedBuyers.add(myName);

        // שלב 3: כתיבה חזרה למסמך (WRITE)
        transaction.update(eventRef, 
            "availableTickets", updatedTickets,
            "buyers", updatedBuyers
        );

        return updatedTickets;
    }).addOnSuccessListener(remaining -> {
        Toast.makeText(this, "רכישה מאובטחת הצליחה! נותרו: " + remaining, Toast.LENGTH_SHORT).show();
    }).addOnFailureListener(e -> {
        if (e.getMessage() != null && e.getMessage().contains("Sold Out")) {
            Toast.makeText(this, "הכרטיסים אזלו!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "הרכישה נכשלה, נסו שוב: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
}
```

> 🔁 **מבחן עומס חוזר:** מאפסים שוב ל-5 כרטיסים ומריצים שוב יחד במקביל.  
> צפו בתוצאה: בדיוק 5 רוכשים, 0 כרטיסים נותרים, ואפס שמות שנדרסו!

---

## 6. חלק ד': אתגרי הרחבה למקצוענים 🚀

1. **מניעת ספסרות (חוק עסקי מורכב):**  
   אסור לאותו תלמיד להופיע יותר מפעם אחת ברשימת הרוכשים.  
   בדקו בתוך הטרנזקציה (`if (event.getBuyers().contains(myName))`) והכשילו את הרכישה במקרה של ניסיון כפול.

2. **ביטול רכישה (Refund Transaction):**  
   הוסיפו כפתור "ביטול כרטיס".  
   הטרנזקציה תבדוק האם המשתמש ברשימה, תסיר אותו, ותעלה את `availableTickets` ב-1 – הכל בצורה אטומית ומסונכרנת.
