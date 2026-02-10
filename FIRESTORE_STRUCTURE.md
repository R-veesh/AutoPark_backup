# Firestore Database Structure

This document outlines the complete Firebase Firestore structure for the Smart VIP Parking System.

## Collections Overview

### 1. `users` Collection
Document ID: Firebase Auth UID
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "isVIP": true,
  "role": "DRIVER" | "ADMIN",
  "createdAt": timestamp,
  "updatedAt": timestamp
}
```

### 2. `parking_lots` Collection
Document ID: Auto-generated Firestore ID
```json
{
  "name": "City Center Parking",
  "address": "123 Main Street, Downtown",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "totalSpots": 150,
  "availableSpots": 120,
  "description": "Premium parking facility in city center",
  "contactNumber": "+1234567890",
  "openingTime": "06:00",
  "closingTime": "23:00",
  "is24Hours": true,
  "createdAt": timestamp,
  "updatedAt": timestamp
}
```

### 3. `parking_rates` Collection
Document ID: Auto-generated Firestore ID
```json
{
  "parkingLotId": "parking_lot_document_id",
  "rateType": "NORMAL" | "VIP" | "HOURLY" | "OVERNIGHT",
  "pricePerHour": 5.0,
  "pricePerDay": 40.0,
  "overnightPrice": 25.0,
  "minChargeAmount": 5.0,
  "maxChargePerDay": 100.0,
  "isActive": true,
  "vipMultiplier": 1.5,
  "createdAt": timestamp,
  "updatedAt": timestamp
}
```

### 4. `vehicles` Collection
Document ID: Auto-generated Firestore ID
```json
{
  "ownerId": "user_document_id",
  "vehicleNumber": "ABC-1234",
  "make": "Toyota",
  "model": "Camry",
  "year": 2022,
  "color": "Blue",
  "licensePlate": "ABC-1234",
  "isActive": true,
  "createdAt": timestamp,
  "updatedAt": timestamp
}
```

### 5. `parking_transactions` Collection
Document ID: Auto-generated Firestore ID
```json
{
  "parkingLotId": "parking_lot_document_id",
  "vehicleId": "vehicle_document_id",
  "ownerId": "user_document_id",
  "vehicleNumber": "ABC-1234",
  "entryTime": 1672531200000,
  "exitTime": 1672538400000,
  "duration": 120,
  "rateType": "NORMAL",
  "chargeAmount": 10.0,
  "status": "ACTIVE" | "COMPLETED" | "CANCELLED",
  "paymentMethod": "CASH" | "CARD" | "WALLET",
  "paymentStatus": "PENDING" | "COMPLETED" | "FAILED",
  "notes": "",
  "createdAt": timestamp,
  "updatedAt": timestamp
}
```

### 6. `invoices` Collection
Document ID: Auto-generated Firestore ID
```json
{
  "transactionId": "parking_transaction_document_id",
  "userId": "user_document_id",
  "vehicleNumber": "ABC-1234",
  "amount": 25.50,
  "dueDate": timestamp,
  "status": "PENDING" | "PAID" | "OVERDUE",
  "paymentMethod": "",
  "paidAt": timestamp,
  "createdAt": timestamp,
  "updatedAt": timestamp
}
```

### 7. `overdue_charges` Collection
Document ID: Auto-generated Firestore ID
```json
{
  "userId": "user_document_id",
  "vehicleNumber": "ABC-1234",
  "amount": 50.0,
  "reason": "Late payment penalty",
  "status": "PENDING" | "PAID" | "WAIVED",
  "createdAt": timestamp,
  "updatedAt": timestamp
}
```

## Real-time Updates

### Critical Indexes for Performance:

1. **parking_transactions** collection:
   - `vehicleId` + `status` (for active parking lookup)
   - `ownerId` + `entryTime` (for user history)
   - `status` + `entryTime` (for daily reports)

2. **vehicles** collection:
   - `ownerId` (for user vehicle lookup)
   - `vehicleNumber` (for QR scanning)

3. **parking_lots** collection:
   - `city` (for location-based filtering)

## Data Flow Example

### Vehicle Entry Flow:
1. Driver generates QR: `vehicleNumber|vehicleId|userId`
2. Admin scans QR and selects parking lot
3. System checks:
   - Vehicle exists in `vehicles` collection
   - No ACTIVE transaction in `parking_transactions`
   - Parking lot has `availableSpots > 0`
4. Creates new `parking_transactions` document with status "ACTIVE"
5. Decrements `availableSpots` in `parking_lots`

### Vehicle Exit Flow:
1. Admin scans QR (or enters vehicle number)
2. System finds ACTIVE transaction in `parking_transactions`
3. Calculates charge based on duration and rate
4. Updates transaction with `exitTime`, `duration`, `chargeAmount`, status "COMPLETED"
5. Increments `availableSpots` in `parking_lots`
6. Creates invoice if payment is pending

## Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only read/write their own documents
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Users can read vehicles they own
    match /vehicles/{vehicleId} {
      allow read, write: if request.auth != null && 
        resource.data.ownerId == request.auth.uid;
    }
    
    // Users can read their own transactions
    match /parking_transactions/{transactionId} {
      allow read: if request.auth != null && 
        resource.data.ownerId == request.auth.uid;
      allow write: if request.auth != null;
    }
    
    // Parking lots are publicly readable but only admins can write
    match /parking_lots/{lotId} {
      allow read: if true;
      allow write: if request.auth != null && 
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "ADMIN";
    }
  }
}
```

## Indexing Requirements

Create these composite indexes in Firebase Console:

1. **parking_transactions**:
   - Fields: `vehicleId` (Ascending), `status` (Ascending)
   - Fields: `ownerId` (Ascending), `entryTime` (Descending)
   - Fields: `status` (Ascending), `entryTime` (Descending)

2. **vehicles**:
   - Fields: `ownerId` (Ascending)
   - Fields: `vehicleNumber` (Ascending)

3. **parking_lots**:
   - Fields: `city` (Ascending)