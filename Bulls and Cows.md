# משימת מעבדה: פיתוח משחק "בול פגיעה" (Bulls and Cows)

**מגמת הנדסת תוכנה | כיתה י"א | ארכיטקטורת MVC ב-Android**

---

## 1. תיאור המשחק וחוקיו

"בול פגיעה" הוא משחק לפענוח קוד מספרי:

1. בתחילת המשחק, המערכת מגרילה קוד סודי המורכב מ-**4 ספרות שונות** (למשל: `7291`).
2. המשתמש מזין ניחוש בן 4 ספרות שונות.
3. המערכת בודקת את הניחוש ומחזירה משוב:
   * **בול (Bull):** ספרה שנמצאת בקוד הסודי ומופיעה **בדיוק באותו המיקום**.
   * **פגיעה (Cow):** ספרה שנמצאת בקוד הסודי, אך מופיעה **במיקום אחר**.
4. המשחק מסתיים כאשר השחקן משיג **4 בולים** (פיצוח מלא של הקוד).

---

## 2. דגשי תוכנה ועקרונות פיתוח

בפרויקט זה נשים דגש על יסודות מדעי המחשב:

1. **שימוש במערכים פרימיטיביים קבועים (`int[]`):** ללא שימוש ב-`ArrayList` או מחלקות מעטפת, כדי לשמור על יעילות זיכרון.
2. **ערבוב אלגוריתמי (Fisher-Yates Shuffle):** הגרלת הקוד מתבצעת על ידי ערבוב מערך קבוע בגודל 10 המכיל את הספרות $0 \dots 9$, ולקיחת 4 התאים הראשונים.
3. **בדיקת תווים בטווח אסקי (ASCII Range):** בדיקה האם תו הוא ספרה תתבצע ישירות באמצעות ביטוי יחס:
   $$c \ge \text{'0'} \quad \text{AND} \quad c \le \text{'9'}$$
4. **קוד מובנה ללא `break`:** שימוש בתנאי לולאה או במשתני דגל בוליאניים (`boolean`) לצורך עצירת סריקה.

---

## 3. חלק א': קוד שלד למודל (`BullsAndCowsModel.java`)

<div dir="ltr">

```java
package com.example.bullsandcows;

import java.util.Random;

public class BullsAndCowsModel {
    private String secretCode;
    private int attemptsCount;
    private boolean isGameOver;

    public BullsAndCowsModel() {
        startNewGame();
    }

    /**
     * מאפסת את המשחק ומגרילה קוד סודי בן 4 ספרות שונות
     * באמצעות מערך קבוע של 10 ספרות וערבוב Fisher-Yates.
     */
    public void startNewGame() {
        attemptsCount = 0;
        isGameOver = false;

        // מערך קבוע של הספרות 0-9
        int[] digits = new int[10];
        for (int i = 0; i < 10; i++) {
            digits[i] = i;
        }

        // אלגוריתם ערבוב
        Random rnd = new Random();
        for (int i = digits.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int temp = digits[i];
            digits[i] = digits[j];
            digits[j] = temp;
        }

        // הרכבת הקוד הסודי מ-4 הספרות הראשונות
        String code = "";
        for (int i = 0; i < 4; i++) {
            code += digits[i];
        }
        secretCode = code;
    }

    /**
     * בדיקת תקינות הקלט:
     * 1. מחרוזת באורך 4 בדיוק.
     * 2. כל תו ערכו בטווח הספרות '0' עד '9'.
     * 3. אין אף ספרה שחוזרת על עצמה פעמיים.
     */
    public boolean isValidGuess(String guess) {
        if (guess == null || guess.length() != 4) {
            return false;
        }

        for (int i = 0; i < 4; i++) {
            char c = guess.charAt(i);

            // בדיקת טווח אסקי
            if (c < '0' || c > '9') {
                return false;
            }

            // בדיקת ספרות כפולות (ללא ספריות עזר)
            for (int j = i + 1; j < 4; j++) {
                if (c == guess.charAt(j)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * משימת התלמיד:
     * מחזירה מערך פרימיטיבי בגודל 2:
     * אינדקס 0: כמות הבולים (ספרה נכונה במקום הנכון)
     * אינדקס 1: כמות הפגיעות (ספרה קיימת אך במיקום אחר)
     *
     * דגש: יש לכתוב ללא שימוש בהוראת break!
     */
    public int[] checkGuess(String guess) {
        int bulls = 0;
        int cows = 0;

        attemptsCount++;

        // ==========================================
        // TODO: כתבו כאן את אלגוריתם הבדיקה
        // 1. עברו בלולאה על 4 התווים של הניחוש (guess).
        // 2. אם התו במיקום i שווה בדיוק לתו בקוד הסודי במיקום i:
        //    קדמו את מונה ה-bulls.
        // 3. אחרת:
        //    סירקו בלולאה פנימית האם התו מופיע במקום אחר כלשהו בקוד הסודי.
        //    (רמז למניעת break: השתמשו במשתנה בוליאני found או בתנאי לולאה).
        // ==========================================

        // בדיקת ניצחון
        if (bulls == 4) {
            isGameOver = true;
        }

        return new int[]{bulls, cows};
    }

    public int getAttemptsCount() {
        return attemptsCount;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    // פעולה לצורכי בדיקות ודיבוג של המורה
    public String getSecretCode() {
        return secretCode;
    }
}
```

</div>

---

## 4. חלק ב': ממשק המשתמש (`res/layout/activity_main.xml`)

<div dir="ltr">

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="20dp"
    android:gravity="center_horizontal">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="🎯 בול פגיעה"
        android:textSize="26sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp" />

    <!-- שורת קלט וכפתור ניחוש -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">

        <EditText
            android:id="@+id/etGuess"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:hint="4 ספרות שונות"
            android:inputType="number"
            android:maxLength="4"
            android:textSize="18sp" />

        <Button
            android:id="@+id/btnSubmit"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="נחש!"
            android:onClick="onGuessClicked" />
    </LinearLayout>

    <!-- היסטוריית ניחושים עם גלילה -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:layout_marginTop="16dp"
        android:background="#F1F5F9"
        android:padding="12dp">

        <TextView
            android:id="@+id/tvHistory"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="16sp"
            android:lineSpacingExtra="6dp" />
    </ScrollView>

    <Button
        android:id="@+id/btnNewGame"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="משחק חדש"
        android:onClick="onNewGameClicked"
        android:layout_marginTop="12dp" />

</LinearLayout>
```

</div>

---

## 5. חלק ג': חיבור הבקר (`MainActivity.java`)

### רקע טכני חשוב: שרשור טקסט וירידת שורה
בניגוד ל-`setText` שדורס את כל הטקסט הקודם, פעולת `append` מוסיפה טקסט חדש לסוף הטקסט הקיים.
כדי שכל ניחוש יוצג בשורה נפרדת, עלינו לשרשר בסוף המחרוזת את התו המיוחד `\n` (קיצור של New Line - ירידת שורה).

**דוגמת תחביר להצגת תוצאה:**
<div dir="ltr">

```java
// שליחת הניחוש למודל וקבלת מערך בגודל 2:
int[] result = model.checkGuess(guess);
int bulls = result[0];
int cows = result[1];

// הרכבת שורת דיווח ושרשורה למסך עם ירידת שורה (\n):
String entry = "#" + model.getAttemptsCount() + ": " + guess + " ➜ "
             + bulls + " בול | " + cows + " פגיעה\n";

tvHistory.append(entry);
```

</div>

---

### משימת התלמיד: מימוש `onGuessClicked`
עליכם לממש את הפעולה לפי השלבים הבאים:
1. **שליפת הקלט:** קראו את המחרוזת מתוך `etGuess`.
2. **אימות תקינות:** העבירו את המחרוזת לפעולה `model.isValidGuess(...)`. אם היא מחזירה `false`, הציגו הודעת שגיאה ב-`Toast` ועצרו את הפעולה (`return`).
3. **בדיקת התוצאה:** זמנו את `model.checkGuess(...)` וקבלו את המערך עם כמות הבולים והפגיעות.
4. **עדכון הממשק:** שרשרו את הניסיון ל-`tvHistory` בעזרת `append` ונקו את שדה הקלט (`etGuess.setText("")`).
5. **בדיקת ניצחון:** בדקו האם המשחק הסתיים (`model.isGameOver()`). אם כן – הציגו ברכת ניצחון ונעלו את כפתור הניחוש (`btnSubmit.setEnabled(false)`).

<div dir="ltr">

```java
package com.example.bullsandcows;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private BullsAndCowsModel model;
    private EditText etGuess;
    private TextView tvHistory;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        model = new BullsAndCowsModel();
        etGuess = findViewById(R.id.etGuess);
        tvHistory = findViewById(R.id.tvHistory);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    /**
     * משימת התלמיד:
     * ממשו את פעולת הניחוש בהתאם ל-5 השלבים המפורטים מעלה.
     */
    public void onGuessClicked(View v) {
        // ==========================================
        // TODO:
        // 1. שלפו את הניחוש מ-etGuess
        // 2. בדקו תקינות בעזרת model.isValidGuess(...)
        // 3. זמנו את model.checkGuess(...) לקבלת בולים ופגיעות
        // 4. הוסיפו את השורה ל-tvHistory באמצעות append ו-\n, ונקו את etGuess
        // 5. אם model.isGameOver() מתקיים - הציגו Toast ונעלו את btnSubmit
        // ==========================================
    }

    public void onNewGameClicked(View v) {
        model.startNewGame();
        tvHistory.setText("");
        etGuess.setText("");
        btnSubmit.setEnabled(true);
        Toast.makeText(this, "הוגרל קוד חדש, בהצלחה!", Toast.LENGTH_SHORT).show();
    }
}
```

</div>