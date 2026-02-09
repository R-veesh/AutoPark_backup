package com.example.autopark.util

object FormValidator {
    
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    /**
     * Validate email format
     */
    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, "Email is required")
        }
        
        val emailRegex = Regex(
            "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+",
            RegexOption.IGNORE_CASE
        )
        
        return if (emailRegex.matches(email)) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "Invalid email format")
        }
    }

    /**
     * Validate password
     */
    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(false, "Password is required")
        }
        
        if (password.length < 6) {
            return ValidationResult(false, "Password must be at least 6 characters")
        }
        
        return ValidationResult(true)
    }

    /**
     * Validate name
     */
    fun validateName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(false, "Name is required")
        }
        
        if (name.length < 2) {
            return ValidationResult(false, "Name must be at least 2 characters")
        }
        
        if (name.length > 50) {
            return ValidationResult(false, "Name must be less than 50 characters")
        }
        
        return ValidationResult(true)
    }

    /**
     * Validate phone number
     */
    fun validatePhoneNumber(phone: String): ValidationResult {
        if (phone.isBlank()) {
            return ValidationResult(false, "Phone number is required")
        }
        
        val phoneRegex = Regex("^[+]?[0-9]{10,15}$")
        
        return if (phoneRegex.matches(phone.replace(" ", "").replace("-", ""))) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "Invalid phone number format")
        }
    }

    /**
     * Validate vehicle number
     */
    fun validateVehicleNumber(vehicleNumber: String): ValidationResult {
        if (vehicleNumber.isBlank()) {
            return ValidationResult(false, "Vehicle number is required")
        }
        
        if (vehicleNumber.length < 3) {
            return ValidationResult(false, "Vehicle number must be at least 3 characters")
        }
        
        if (vehicleNumber.length > 15) {
            return ValidationResult(false, "Vehicle number must be less than 15 characters")
        }
        
        return ValidationResult(true)
    }

    /**
     * Validate vehicle type
     */
    fun validateVehicleType(vehicleType: String): ValidationResult {
        if (vehicleType.isBlank()) {
            return ValidationResult(false, "Vehicle type is required")
        }
        
        val validTypes = listOf("Car", "Bike", "Truck", "Van", "SUV", "Motorcycle")
        
        return if (validTypes.contains(vehicleType)) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "Invalid vehicle type")
        }
    }

    /**
     * Validate vehicle brand
     */
    fun validateVehicleBrand(brand: String): ValidationResult {
        if (brand.isBlank()) {
            return ValidationResult(false, "Vehicle brand is required")
        }
        
        if (brand.length < 2) {
            return ValidationResult(false, "Vehicle brand must be at least 2 characters")
        }
        
        return ValidationResult(true)
    }

    /**
     * Validate vehicle model
     */
    fun validateVehicleModel(model: String): ValidationResult {
        if (model.isBlank()) {
            return ValidationResult(false, "Vehicle model is required")
        }
        
        if (model.length < 2) {
            return ValidationResult(false, "Vehicle model must be at least 2 characters")
        }
        
        return ValidationResult(true)
    }

    /**
     * Validate vehicle color
     */
    fun validateVehicleColor(color: String): ValidationResult {
        if (color.isBlank()) {
            return ValidationResult(false, "Vehicle color is required")
        }
        
        if (color.length < 2) {
            return ValidationResult(false, "Vehicle color must be at least 2 characters")
        }
        
        return ValidationResult(true)
    }

    /**
     * Validate parking lot name
     */
    fun validateParkingLotName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(false, "Parking lot name is required")
        }
        
        if (name.length < 3) {
            return ValidationResult(false, "Parking lot name must be at least 3 characters")
        }
        
        return ValidationResult(true)
    }

    /**
     * Validate address
     */
    fun validateAddress(address: String): ValidationResult {
        if (address.isBlank()) {
            return ValidationResult(false, "Address is required")
        }
        
        if (address.length < 5) {
            return ValidationResult(false, "Address must be at least 5 characters")
        }
        
        return ValidationResult(true)
    }

    /**
     * Validate city
     */
    fun validateCity(city: String): ValidationResult {
        if (city.isBlank()) {
            return ValidationResult(false, "City is required")
        }
        
        if (city.length < 2) {
            return ValidationResult(false, "City must be at least 2 characters")
        }
        
        return ValidationResult(true)
    }

    /**
     * Validate state
     */
    fun validateState(state: String): ValidationResult {
        if (state.isBlank()) {
            return ValidationResult(false, "State is required")
        }
        
        if (state.length < 2) {
            return ValidationResult(false, "State must be at least 2 characters")
        }
        
        return ValidationResult(true)
    }

    /**
     * Validate zip code
     */
    fun validateZipCode(zipCode: String): ValidationResult {
        if (zipCode.isBlank()) {
            return ValidationResult(false, "Zip code is required")
        }
        
        val zipRegex = Regex("^[0-9]{5,6}$")
        
        return if (zipRegex.matches(zipCode)) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "Invalid zip code format")
        }
    }

    /**
     * Validate parking rate
     */
    fun validateParkingRate(rate: Double): ValidationResult {
        if (rate <= 0) {
            return ValidationResult(false, "Rate must be greater than 0")
        }
        
        if (rate > 1000) {
            return ValidationResult(false, "Rate cannot exceed 1000")
        }
        
        return ValidationResult(true)
    }

    /**
     * Validate parking capacity
     */
    fun validateParkingCapacity(capacity: Int): ValidationResult {
        if (capacity <= 0) {
            return ValidationResult(false, "Capacity must be greater than 0")
        }
        
        if (capacity > 10000) {
            return ValidationResult(false, "Capacity cannot exceed 10000")
        }
        
        return ValidationResult(true)
    }

    /**
     * Validate credit card number (basic validation)
     */
    fun validateCreditCardNumber(cardNumber: String): ValidationResult {
        if (cardNumber.isBlank()) {
            return ValidationResult(false, "Card number is required")
        }
        
        val cleanedNumber = cardNumber.replace(" ", "").replace("-", "")
        
        if (cleanedNumber.length < 13 || cleanedNumber.length > 19) {
            return ValidationResult(false, "Invalid card number")
        }
        
        if (!cleanedNumber.all { it.isDigit() }) {
            return ValidationResult(false, "Card number must contain only digits")
        }
        
        return ValidationResult(true)
    }

    /**
     * Validate CVV
     */
    fun validateCVV(cvv: String): ValidationResult {
        if (cvv.isBlank()) {
            return ValidationResult(false, "CVV is required")
        }
        
        if (cvv.length < 3 || cvv.length > 4) {
            return ValidationResult(false, "Invalid CVV")
        }
        
        if (!cvv.all { it.isDigit() }) {
            return ValidationResult(false, "CVV must contain only digits")
        }
        
        return ValidationResult(true)
    }

    /**
     * Validate expiry date (MM/YY format)
     */
    fun validateExpiryDate(expiry: String): ValidationResult {
        if (expiry.isBlank()) {
            return ValidationResult(false, "Expiry date is required")
        }
        
        val expiryRegex = Regex("^(0[1-9]|1[0-2])/([0-9]{2})$")
        
        if (!expiryRegex.matches(expiry)) {
            return ValidationResult(false, "Invalid expiry date format (MM/YY)")
        }
        
        val parts = expiry.split("/")
        val month = parts[0].toInt()
        val year = 2000 + parts[1].toInt()
        
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        
        return if (year < currentYear || (year == currentYear && month < currentMonth)) {
            ValidationResult(false, "Card has expired")
        } else {
            ValidationResult(true)
        }
    }
}