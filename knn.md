# חלק ג': אלגוריתם ה-KNN (K-Nearest Neighbors) 🧠

## הקדמה: מהו בעצם KNN?
אם נדמיין את מאפייני חלונות הזמן (ממוצע, מקסימום, מינימום, סטיית תקן) כקואורדינטות במרחב 4-ממדי, כל סוג פעולה (הליכה, מנוחה, ניעור) ייצור "ענן" נקודות באזור אחר במרחב.
אלגוריתם KNN (שכנים קרובים ביותר) מבצע את השלבים הבאים עבור חלון זמן חדש שאינו מוכר:
1. מודד את המרחק מהנקודה החדשה לכל הנקודות ההיסטוריות.
2. בוחר את ה-$K$ השכנים הקרובים ביותר.
3. מבצע הצבעה: הרוב קובע!

כדי למדוד "מרחק" במרחב 4-ממדי, נשתמש במרחק האוקלידי (פיתגורס מורחב):
$$Distance = \sqrt{(Max_1-Max_2)^2 + (Min_1-Min_2)^2 + (Mean_1-Mean_2)^2 + (StdDev_1-StdDev_2)^2}$$

---

## משימת תכנות 1: מחלקת KNNClassifier
צרו את מנוע הסיווג. מחלקה זו תשמור את המידע שנקליט (Training Data) ותסווג נקודות חדשות בזמן אמת.

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KNNClassifier {
    private List<DataPoint> trainingData;
    private int k;

    public KNNClassifier(int k) {
        this.k = k;
        this.trainingData = new ArrayList<>();
    }

    public void addTrainingData(DataPoint point) {
        trainingData.add(point);
    }

    public String classify(DataPoint unknownPoint) {
        if (trainingData.isEmpty()) return "Unknown";

        // 1. חישוב מרחקים
        List<DistanceRecord> distances = new ArrayList<>();
        for (DataPoint tp : trainingData) {
            double dist = calculateDistance(tp, unknownPoint);
            distances.add(new DistanceRecord(tp, dist));
        }

        // 2. מיון לפי מרחק
        Collections.sort(distances, new Comparator<DistanceRecord>() {
            @Override
            public int compare(DistanceRecord r1, DistanceRecord r2) {
                return Double.compare(r1.distance, r2.distance);
            }
        });

        // 3. הצבעה בין K השכנים הקרובים
        Map<String, Integer> votes = new HashMap<>();
        int limit = Math.min(k, distances.size());
        for (int i = 0; i < limit; i++) {
            String label = distances.get(i).point.label;
            votes.put(label, votes.getOrDefault(label, 0) + 1);
        }

        // 4. מציאת התווית שניצחה ברוב קולות
        String bestLabel = "Unknown";
        int maxVotes = -1;
        for (Map.Entry<String, Integer> entry : votes.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                bestLabel = entry.getKey();
            }
        }
        return bestLabel;
    }

    private double calculateDistance(DataPoint p1, DataPoint p2) {
        double dMax = Math.pow(p1.max - p2.max, 2);
        double dMin = Math.pow(p1.min - p2.min, 2);
        double dMean = Math.pow(p1.mean - p2.mean, 2);
        double dStdDev = Math.pow(p1.stdDev - p2.stdDev, 2);
        return Math.sqrt(dMax + dMin + dMean + dStdDev);
    }

    // מחלקת עזר פנימית למיון המרחקים
    private static class DistanceRecord {
        DataPoint point;
        double distance;
        DistanceRecord(DataPoint p, double d) { point = p; distance = d; }
    }
}
```

---

## משימת תכנות 2: חיבור ממשק המשתמש וההקלטה
ב-`MainActivity`, הוסיפו כפתורים המאפשרים להקליט 10 חלונות של כל סוג פעולה ולשמור אותם ב-`KNNClassifier`. לאחר שיש נתונים, כל חלון חדש יועבר למתודת ה-`classify` ותוצאתו תוצג על המסך.

*טיפ:* צרו משתנה בוליאני או מחרוזת שמייצגת את מצב ההקלטה. אם אתם מקליטים, הוסיפו את חלון הזמן למאגר ה-`trainingData`. אם סיימתם, הפעילו את הסיווג!

### המבחן הסופי 🏆
1. הניחו את הטלפון על השולחן והקליטו דוגמאות למצב "נייח".
2. לכו עם הטלפון בחדר והקליטו דוגמאות ל-"הליכה".
3. נערו את הטלפון והקליטו "ניעור".
4. כעת צפו במסך: האפליקציה אמורה לזהות בדיוק מה אתם עושים בזמן אמת, אך ורק על בסיס המתמטיקה שלכם!