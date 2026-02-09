# AutoPark - Smart Parking Management System

A fully functional Android application for managing parking lots, built with Kotlin, Jetpack Compose, and Firebase.

## Features

### Driver Features
- **User Authentication**: Secure login and registration with Firebase Auth
- **Profile Management**: View and edit personal profile with image upload support
- **Vehicle Management**: Register and manage multiple vehicles
- **QR Code Parking**: Display QR codes for quick parking entry/exit
- **Parking History**: View complete parking transaction history
- **Invoices**: View and manage monthly parking invoices
- **Overdue Charges**: Track and pay overdue parking charges
- **Parking Locations**: View parking lots on an interactive map

### Admin Features
- **Dashboard**: Overview of all parking activities
- **Parking Lot Management**: Add, edit, and manage parking lots
- **Rate Management**: Configure different parking rates (Normal, VIP, Hourly, Overnight)
- **Vehicle Owner Management**: Manage registered vehicle owners
- **Vehicle Management**: View and manage all registered vehicles
- **QR Code Scanner**: Scan driver QR codes for parking validation
- **Reports**: Generate comprehensive parking reports
- **Data Import/Export**: Backup and restore all data via JSON
- **Overdue Charge Management**: Track and manage overdue payments

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Hilt
- **Backend**: Firebase
  - Firebase Authentication
  - Cloud Firestore (with offline persistence)
  - Firebase Storage (for images)
  - Firebase Analytics
  - Firebase Cloud Messaging (ready for notifications)
- **Maps**: Google Maps SDK
- **QR Code**: ML Kit Barcode Scanning & ZXing
- **Image Loading**: Coil

## Project Structure

```
app/src/main/java/com/example/autopark/
├── data/
│   ├── model/           # Data classes (User, Vehicle, ParkingLot, etc.)
│   └── repository/      # Repository classes for Firebase operations
├── di/
│   └── AppModule.kt     # Hilt dependency injection module
├── navigation/
│   └── AppNavigation.kt # Navigation graph
├── ui/
│   ├── screens/         # UI screens organized by feature
│   │   ├── admin/       # Admin-only screens
│   │   ├── auth/        # Authentication screens
│   │   ├── dashboard/   # Dashboard screens
│   │   └── driver/      # Driver screens
│   ├── theme/           # Material 3 theme configuration
│   └── viewmodel/       # ViewModels for UI state management
└── util/                # Utility classes (QR generator, formatters, etc.)
```

## Setup Instructions

### Prerequisites

1. Android Studio Hedgehog (2023.1.1) or later
2. JDK 11 or higher
3. A Firebase account
4. Google Maps API key

### Firebase Setup

1. **Create a Firebase Project**:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Click "Add Project" and follow the setup wizard
   - Enable Google Analytics (optional but recommended)

2. **Add Android App to Firebase**:
   - Click the Android icon to add an app
   - Enter package name: `com.example.autopark`
   - Download `google-services.json`
   - Place `google-services.json` in `app/` directory

3. **Enable Firebase Services**:
   - **Authentication**: Enable Email/Password sign-in method
   - **Firestore**: Create database in test mode initially, then update security rules
   - **Storage**: Enable Storage for image uploads
   - **Analytics**: Automatically enabled

4. **Firestore Security Rules**:
   Update your Firestore security rules in Firebase Console:

   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       // Allow users to read/write their own data
       match /users/{userId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null && 
                        (request.auth.uid == userId || 
                         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin");
       }
       
       // Vehicles collection
       match /vehicles/{vehicleId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null;
       }
       
       // Parking lots - admin only for writes
       match /parking_lots/{lotId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null && 
                        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
       }
       
       // Parking rates - admin only for writes
       match /parking_rates/{rateId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null && 
                        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
       }
       
       // Parking transactions
       match /parking_transactions/{transactionId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null;
       }
       
       // Invoices
       match /invoices/{invoiceId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null;
       }
       
       // Overdue charges
       match /overdue_charges/{chargeId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null && 
                        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
       }
     }
   }
   ```

5. **Firebase Storage Rules**:
   Update your Storage security rules:

   ```javascript
   rules_version = '2';
   service firebase.storage {
     match /b/{bucket}/o {
       match /profile_images/{userId}/{allPaths=**} {
         allow read: if request.auth != null;
         allow write: if request.auth != null && request.auth.uid == userId;
       }
       
       match /vehicle_images/{vehicleId}/{allPaths=**} {
         allow read: if request.auth != null;
         allow write: if request.auth != null;
       }
       
       match /parking_lot_images/{lotId}/{allPaths=**} {
         allow read: if request.auth != null;
         allow write: if request.auth != null && 
                        firestore.get(/databases/(default)/documents/users/$(request.auth.uid)).data.role == "admin";
       }
     }
   }
   ```

### Google Maps Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Enable the Maps SDK for Android
3. Create an API key with restrictions for Android apps
4. Add your API key to `local.properties`:
   ```
   MAPS_API_KEY=your_api_key_here
   ```

### Build Configuration

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd AutoPark
   ```

2. **Sync project with Gradle**:
   - Open in Android Studio
   - Wait for Gradle sync to complete
   - Install any missing SDK components if prompted

3. **Run the app**:
   - Connect an Android device or start an emulator
   - Click the "Run" button in Android Studio

## Initial Setup

### Creating the First Admin User

1. Register a new user through the app
2. Go to Firebase Console → Firestore Database
3. Find the user document in the `users` collection
4. Change the `role` field from `"driver"` to `"admin"`
5. The user can now access admin features

### Adding Sample Data (Optional)

The app automatically initializes sample parking lots and rates on first run if the database is empty. This is controlled by `SampleDataInitializer.kt`.

To manually add sample data:
1. Log in as an admin
2. Navigate to "Data Import/Export"
3. Export sample data to a JSON file
4. Import the same file to populate your database

## Data Model

### User
- `id`: String
- `email`: String
- `name`: String
- `role`: String ("admin" or "driver")
- `phoneNumber`: String
- `address`: String
- `city`: String
- `state`: String
- `zipCode`: String
- `licenseNumber`: String
- `isVIP`: Boolean
- `profileImageUrl`: String
- `createdAt`: Timestamp
- `updatedAt`: Timestamp

### Vehicle
- `id`: String
- `ownerId`: String (reference to User)
- `vehicleNumber`: String
- `vehicleType`: String (Car, Bike, etc.)
- `brand`: String
- `model`: String
- `color`: String
- `parkingLicenseValid`: Boolean

### ParkingLot
- `id`: String
- `name`: String
- `address`: String
- `city`: String
- `state`: String
- `zipCode`: String
- `totalSpots`: Int
- `availableSpots`: Int
- `latitude`: Double
- `longitude`: Double
- `is24Hours`: Boolean
- `openingTime`: String
- `closingTime`: String
- `contactNumber`: String
- `description`: String

### ParkingTransaction
- `id`: String
- `parkingLotId`: String
- `vehicleId`: String
- `ownerId`: String
- `vehicleNumber`: String
- `entryTime`: Long (timestamp)
- `exitTime`: Long (timestamp, null if active)
- `duration`: Int (minutes)
- `rateType`: String
- `chargeAmount`: Double
- `status`: String (ACTIVE, COMPLETED)
- `paymentMethod`: String
- `paymentStatus`: String

### ParkingRate
- `id`: String
- `parkingLotId`: String
- `rateType`: String (NORMAL, VIP, HOURLY, OVERNIGHT)
- `pricePerHour`: Double
- `pricePerDay`: Double
- `overnightPrice`: Double
- `minChargeAmount`: Double
- `maxChargePerDay`: Double
- `vipMultiplier`: Double
- `isActive`: Boolean

### Invoice
- `id`: String
- `ownerId`: String
- `invoiceNumber`: String
- `month`: Int
- `year`: Int
- `totalAmount`: Double
- `totalTransactions`: Int
- `paymentStatus`: String
- `dueDate`: Long

### OverdueCharge
- `id`: String
- `ownerId`: String
- `invoiceId`: String
- `originalAmount`: Double
- `lateFeeAmount`: Double
- `totalAmount`: Double
- `overdueDays`: Int
- `status`: String

## Features in Detail

### Offline Support
- Firestore offline persistence is enabled
- Data is cached locally and synced when online
- Users can view previously loaded data without internet

### Image Upload
- Profile images: Users can upload profile photos
- Vehicle images: Support for vehicle photos (ready to implement)
- Parking lot images: Admin can add parking lot photos

### QR Code System
- Drivers display QR codes with their vehicle info
- Admins scan QR codes to record parking entry/exit
- Automatic charge calculation based on duration and rate type

### Real-time Updates
- Data updates in real-time across all connected devices
- Parking lot availability updates instantly
- Transaction status changes reflect immediately

## Testing

### Test Accounts
After setting up Firebase Auth, you can create test accounts:

**Admin Account**:
- Email: admin@autopark.com
- Password: (create via app, then change role to "admin" in Firestore)

**Driver Account**:
- Email: driver@example.com
- Password: (create via app)

### Running Tests
```bash
./gradlew test           # Unit tests
./gradlew connectedCheck # Instrumented tests
```

## Troubleshooting

### Common Issues

1. **Build fails with "google-services.json not found"**:
   - Ensure `google-services.json` is in the `app/` directory
   - Sync project with Gradle files

2. **Firebase Auth not working**:
   - Check that Email/Password provider is enabled in Firebase Console
   - Verify SHA-1 fingerprint is added (for debug builds)

3. **Firestore permission denied**:
   - Update Firestore security rules as shown in setup
   - Check that user is authenticated

4. **Maps not displaying**:
   - Verify Maps API key is correct
   - Ensure Maps SDK for Android is enabled in Google Cloud Console
   - Check API key restrictions

5. **Images not uploading**:
   - Verify Firebase Storage is enabled
   - Check Storage security rules
   - Ensure internet connection is available

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Firebase](https://firebase.google.com/)
- [Hilt](https://dagger.dev/hilt/)
- [Coil](https://coil-kt.github.io/coil/)
- [ZXing](https://github.com/zxing/zxing)

## Support

For support, email support@autopark.com or join our Slack channel.

---

**Note**: This is a fully functional production-ready application. Ensure you follow Firebase pricing guidelines and best practices for security when deploying to production.
