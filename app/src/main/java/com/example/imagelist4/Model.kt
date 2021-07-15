package com.example.imagelist4

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Entity(tableName = "model")
data class Model (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo var id: Int = 0,
    @ColumnInfo(name = "underName") var unName:String?,
    @ColumnInfo(name = "name") var name: String? ,
    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
    var image: ByteArray?)




@Dao
interface ModelDao {

      @Insert(onConflict = OnConflictStrategy.IGNORE)
      suspend fun insert(table: Model)


      @Update
      suspend fun update(table: Model)

      @Delete
      suspend fun delete(table: Model)

    @Delete
    fun deleteModel(vararg model: Model)




    @Query("DELETE FROM model WHERE id = :modelId")
    suspend fun deleteById(modelId: Long)


    @Query("DELETE FROM model")
    suspend fun deleteAll()


    @Query("SELECT * FROM model ORDER BY underName ASC")
    fun getAll(): Flow<List<Model>>


    @Query("SELECT * FROM model where id= :id")
    suspend fun getUserById(id: Int) : Model

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg model: Model)

    @Query("SELECT * FROM model ORDER BY name ASC")
    fun getSortByAscName(): Flow<List<Model>>

}
