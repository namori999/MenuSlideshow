package com.example.imagelist4

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Entity(tableName = "model")
data class Model (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo var id: Int = 0,
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


    @Query("SELECT * FROM model")
      suspend fun getAll(): Array<Model>


    @Query("DELETE FROM model")
    suspend fun deleteAll()


    @Query("SELECT * FROM model")
    fun getAlphabetizedWords(): Flow<List<Model>>


    @Query("SELECT * FROM model where id= :id")
    suspend fun getUserById(id: Int) : Model



}
