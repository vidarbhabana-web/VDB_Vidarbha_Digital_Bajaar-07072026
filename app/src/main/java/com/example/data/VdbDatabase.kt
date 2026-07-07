package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses ORDER BY rating DESC, reviewCount DESC")
    fun getAllBusinesses(): Flow<List<Business>>

    @Query("SELECT * FROM businesses WHERE category = :category ORDER BY rating DESC")
    fun getBusinessesByCategory(category: String): Flow<List<Business>>

    @Query("SELECT * FROM businesses WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchBusinesses(query: String): Flow<List<Business>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: Business): Long

    @Query("DELETE FROM businesses WHERE id = :id")
    suspend fun deleteBusiness(id: Int)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE businessId = :businessId")
    fun getProductsByBusiness(businessId: Int): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Int)
}

@Dao
interface AIAdviceDao {
    @Query("SELECT * FROM ai_advice_logs ORDER BY timestamp ASC")
    fun getAdviceLogs(): Flow<List<AIAdvice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvice(advice: AIAdvice)

    @Query("DELETE FROM ai_advice_logs")
    suspend fun clearLogs()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: Int, status: String)
}

@Database(entities = [Business::class, Product::class, AIAdvice::class, Order::class], version = 1, exportSchema = false)
abstract class VdbDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao
    abstract fun productDao(): ProductDao
    abstract fun aiAdviceDao(): AIAdviceDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: VdbDatabase? = null

        fun getDatabase(context: Context): VdbDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VdbDatabase::class.java,
                    "vdb_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class VdbRepository(private val db: VdbDatabase) {
    val allBusinesses: Flow<List<Business>> = db.businessDao().getAllBusinesses()
    val allOrders: Flow<List<Order>> = db.orderDao().getAllOrders()
    val aiLogs: Flow<List<AIAdvice>> = db.aiAdviceDao().getAdviceLogs()

    fun getBusinessesByCategory(category: String): Flow<List<Business>> =
        db.businessDao().getBusinessesByCategory(category)

    fun searchBusinesses(query: String): Flow<List<Business>> =
        db.businessDao().searchBusinesses(query)

    fun getProductsByBusiness(businessId: Int): Flow<List<Product>> =
        db.productDao().getProductsByBusiness(businessId)

    suspend fun insertBusiness(business: Business): Long =
        db.businessDao().insertBusiness(business)

    suspend fun deleteBusiness(id: Int) =
        db.businessDao().deleteBusiness(id)

    suspend fun insertProduct(product: Product) =
        db.productDao().insertProduct(product)

    suspend fun deleteProduct(id: Int) =
        db.productDao().deleteProduct(id)

    suspend fun insertAdvice(advice: AIAdvice) =
        db.aiAdviceDao().insertAdvice(advice)

    suspend fun clearAdviceLogs() =
        db.aiAdviceDao().clearLogs()

    suspend fun insertOrder(order: Order): Long =
        db.orderDao().insertOrder(order)

    suspend fun updateOrderStatus(id: Int, status: String) =
        db.orderDao().updateOrderStatus(id, status)

    suspend fun seedMockDataIfEmpty() {
        // Since we want standard seed data on launch, we will check if any businesses exist.
        // Handled in ViewModel init.
    }
}
