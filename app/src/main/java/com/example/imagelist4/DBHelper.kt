package com.example.imagelist4

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import android.graphics.Bitmap
import android.graphics.Insets.add
import android.util.Log
import android.view.Display
import androidx.core.view.OneShotPreDrawListener.add
import androidx.work.Data
import java.io.ByteArrayOutputStream


class DBHelper(
    context: Context,
    databaseName: String,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
)
    : SQLiteOpenHelper(context, databaseName, factory, version) {

    override fun onCreate(database: SQLiteDatabase?) {
        database?.execSQL("create table if not exists SampleTable ( _id INTEGER PRIMARY KEY, name TEXT,  image BLOB)");

    }

    override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            database?.execSQL("alter table SampleTable add column deleteFlag integer default 0")
        }
    }


    //insertData
    fun insertData(name: String, image: ByteArray) {

        try {
            val database = writableDatabase

            val values = ContentValues()
            values.put("name", name)
            values.put("image", image)

            database.insert("SampleTable", null, values)

            database.close()


        }catch(exception: Exception) {
            Log.e("insertData", exception.toString())
        }


    }

    fun updateData(whereId: Int, newName: String, newBitmap: ByteArray) {
        try {
            val database = writableDatabase
            val sql = "UPDATE table_name SET column_name1 = data1, ... WHERE id=?"

            val values = ContentValues()
            values.put("name", newName)
            values.put("image", newBitmap)

            val whereClauses = "id = ?"
            val whereArgs = arrayOf(whereId)

            database.update("SampleTable", values, whereClauses, null)
        } catch (exception: Exception) {
            Log.e("updateData", exception.toString())
        }
    }


    //deleteData
    fun deleteData(id: Int) =
        try {//val dbHelper= DBHelper(context, "SampleDB", null, 1);
        val database = writableDatabase

        val sql = "DELETE FROM SampleTable  WHERE id=?"

        //database.delete("SampleTable", id.toString(), null)
        //database.execSQL(sql)

        val stmt: SQLiteStatement = database.compileStatement(sql)
        stmt.clearBindings()
        stmt.bindDouble(1, id.toDouble())
        stmt.executeUpdateDelete()
        stmt.execute()
        database.delete("SampleTable", "id=?", null)

        database.close()


        }catch (exception: Exception) {
        android.util.Log.e("DeleteData", exception.toString())
    }


    fun getData(s: String): Cursor? {
        val database = this.readableDatabase
        return database.rawQuery("SELECT * FROM SampleTable", null)
    }





}







class DBAdapter(protected val context: Context) {
    protected var dbHelper: DBHelper? =null
    protected var db: SQLiteDatabase? = null
    

    //
    // Adapter Methods
    //
    fun open(): DBAdapter {
        db = dbHelper?.writableDatabase
        return this
    }

    fun close() {
        dbHelper?.close()
    }
    

}