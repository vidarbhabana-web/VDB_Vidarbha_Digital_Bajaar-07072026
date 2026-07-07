package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "businesses")
data class Business(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val descriptionMr: String = "",
    val descriptionHi: String = "",
    val category: String, // e.g. Restaurants, Agriculture, Handloom, Medical, etc.
    val phone: String,
    val email: String,
    val address: String,
    val isVerified: Boolean = false,
    val rating: Float = 4.5f,
    val reviewCount: Int = 12,
    val ownerEmail: String = "",
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessId: Int,
    val name: String,
    val nameMr: String = "",
    val nameHi: String = "",
    val price: Double,
    val description: String,
    val category: String = "Product", // Product or Service listing
    val imageType: String = "default" // Orange, handloom, spice, medical, food, generic
)

@Entity(tableName = "ai_advice_logs")
data class AIAdvice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "USER" or "AI"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessId: Int,
    val businessName: String,
    val productName: String,
    val price: Double,
    val status: String, // "PENDING", "ASSIGNED", "OUT_FOR_DELIVERY", "DELIVERED"
    val deliveryAddress: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deliveryType: String = "Parcel" // Food, Medicine, Grocery, Parcel
)

enum class AppLanguage {
    ENGLISH,
    MARATHI,
    HINDI
}

enum class UserRole {
    CUSTOMER,
    VENDOR
}
