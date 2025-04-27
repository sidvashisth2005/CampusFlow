
# Digital Permission Management System - Android App

This document contains the complete breakdown of the **Digital Permission Management System** project, including the code, Firebase setup, Firebase Cloud Messaging (FCM) integration, and all other aspects discussed.

## Table of Contents:
1. [Firebase Setup](#firebase-setup)
2. [Push Notifications Integration](#push-notifications-integration)
3. [App Screens Overview](#app-screens-overview)
4. [Database Structure](#database-structure)
5. [Figma UI Design](#figma-ui-design)
6. [Code Snippets](#code-snippets)

---

## **Firebase Setup**

1. **Set Up Firebase in Your Android Project**
   - Go to [Firebase Console](https://console.firebase.google.com/).
   - Create a new project or use an existing one.
   - Add your Android app to Firebase and download the `google-services.json` file.
   - Place the file in the `app/` folder of your Android Studio project.

2. **Add Firebase Messaging SDK**
   In your **build.gradle (app level)**, add the Firebase Messaging dependency:
   ```gradle
   dependencies {
       implementation 'com.google.firebase:firebase-messaging:23.1.0'  // Check latest version
   }
   ```

   Sync the project after adding the dependency.

3. **Configure Firebase Messaging in Your App**
   In your **AndroidManifest.xml**, add the required permissions and services:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

   <application
       android:name=".MyApplication"
       android:label="@string/app_name">
       <service
           android:name=".MyFirebaseMessagingService"
           android:permission="com.google.firebase.MESSAGING_PERMISSION">
           <intent-filter>
               <action android:name="com.google.firebase.MESSAGING_EVENT" />
           </intent-filter>
       </service>
   </application>
   ```

---

## **Push Notifications Integration**

### **Create `MyFirebaseMessagingService.java`**

```java
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.util.Log;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            // Display Notification
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    private void showNotification(String title, String body) {
        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}
```

### **Subscribing to Topics (Optional)**

```java
FirebaseMessaging.getInstance().subscribeToTopic("events")
    .addOnCompleteListener(task -> {
        String msg = task.isSuccessful() ? "Subscribed to events" : "Subscription failed";
        Log.d("FCM", msg);
    });
```

### **Sending Notifications from Firebase Console**
   - Go to Firebase Console > Cloud Messaging > Send a message.
   - Enter the title, body, and select the topic or individual users to send the message to.

### **Sending Notifications Programmatically from Your Server (Node.js Example)**

```javascript
const admin = require("firebase-admin");

admin.initializeApp();

const message = {
  notification: {
    title: "Booking Approved!",
    body: "Your room booking for the event has been approved. Check the details now.",
  },
  token: "<USER_FCM_TOKEN>", // Get this token after user logs in
};

admin.messaging().send(message)
  .then((response) => {
    console.log("Successfully sent message:", response);
  })
  .catch((error) => {
    console.log("Error sending message:", error);
  });
```

### **Handling Notification Clicks**

```java
Intent intent = new Intent(this, BookingDetailsActivity.class);
intent.putExtra("bookingId", "12345");  // Pass extra data if needed (like booking ID)
PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
    .setSmallIcon(R.drawable.ic_notification)
    .setContentTitle(title)
    .setContentText(body)
    .setContentIntent(pendingIntent) // Handle the click event
    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
```

---

## **App Screens Overview**

### **1. Login Screen**
- Allows new users to register using their college email ID and password.
- Existing users log in using their college email ID and password.

### **2. Home Screen**
- Displays upcoming events, highlights, and allows users to register events by uploading posters, and specifying event details (date, venue, time).

### **3. Explore Screen**
- Shows the university map and the timetable for lecture halls and classrooms.

### **4. Booking Screen**
- Faculty and secretaries can check the availability of rooms and book them.
- Students can only check the availability of rooms.

### **5. Community Screen**
- Secretaries can create communities for their clubs, allowing users to interact within clubs and manage events.

### **6. Profile Screen**
- Displays user information.
- Users can edit their profiles and manage pending requests and approvals.
- Admins have the authority to approve or reject requests.

---

## **Database Structure**

### **Firebase Realtime Database Schema**
```json
{
  "users": {
    "userID1": {
      "email": "user1@example.com",
      "designation": "student",
      "profile_picture": "url_to_picture"
    }
  },
  "events": {
    "eventID1": {
      "name": "Event Name",
      "poster": "url_to_poster",
      "date": "2025-04-26",
      "time": "10:00 AM",
      "venue": "Lecture Hall A"
    }
  },
  "bookings": {
    "bookingID1": {
      "room": "Lecture Hall B",
      "time": "2:00 PM",
      "userID": "userID1",
      "status": "approved"
    }
  },
  "requests": {
    "requestID1": {
      "userID": "userID1",
      "type": "event",
      "status": "pending"
    }
  }
}
```

### **Indexes for Firebase Database**
For faster fetching of event details or bookings:
```json
{
  "rules": {
    "events": {
      ".indexOn": ["date", "venue"]
    },
    "bookings": {
      ".indexOn": ["room", "status"]
    }
  }
}
```

---

## **Figma UI Design**
The UI design for the app has been created using **Figma**. The color scheme and design elements are inspired by the college theme, and the app contains interactive and visually appealing features.

### **Color Palette**:
- **Primary Color**: #2A9D8F (Teal)
- **Secondary Color**: #264653 (Dark Blue)
- **Accent Color**: #E9C46A (Yellow)
- **Background Color**: #F1FAEE (Light Grey)
- **Text Color**: #333333 (Dark Grey)

### **Typography**:
- **Primary Font**: Roboto
- **Secondary Font**: Lora

---

## **Code Snippets**

### **Firebase Configuration in `MainActivity.java`**
```java
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// Initialize Firebase
FirebaseDatabase database = FirebaseDatabase.getInstance();
DatabaseReference myRef = database.getReference("events");

myRef.setValue("Hello, Firebase!");
```

### **Login Logic in `LoginActivity.java`**
```java
FirebaseAuth mAuth = FirebaseAuth.getInstance();

public void loginUser(String email, String password) {
    mAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Successfully logged in
                FirebaseUser user = mAuth.getCurrentUser();
            } else {
                // Failed to log in
                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
}
```

---

## Conclusion

This document contains all the details, code snippets, configurations, and steps necessary for the **Digital Permission Management System** using Firebase and Android. You now have a comprehensive overview of the entire system, from database setup to UI design and Firebase Push Notifications.

Let me know if you need any more details or modifications! 🚀
