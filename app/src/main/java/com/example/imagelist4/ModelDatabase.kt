package com.example.imagelist4


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


@Database(entities = arrayOf(Model::class), version = 6 ,  exportSchema = false)
abstract class ModelRoomDatabase : RoomDatabase() {

    abstract fun modelDao(): ModelDao


    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        var INSTANCE: ModelRoomDatabase? = null


        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): ModelRoomDatabase {
            // if the INSTANCE is not null, then return it,

            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ModelRoomDatabase::class.java,
                    "table"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(ModelDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }

        }

        private class ModelDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            /**
             * Override the onCreate method to populate the database.
             */
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // If you want to keep the data through app restarts,
                // comment out the following line.
                ModelRoomDatabase.INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {

                        populateDatabase(database.modelDao())
                    }
                }

            }


            /**
             * Populate the database in a new coroutine.
             * If you want to start with more words, just add them.
             */
            suspend fun populateDatabase(modelDao: ModelDao) {
                // Start the app with a clean database every time.
                // Not needed if you only populate on creation.
                modelDao.deleteAll()


                val bitmap = createBitmap(64, 64, Bitmap.Config.ARGB_8888)
                val cv = Canvas(bitmap)
                val p = Paint()
                p.setColor(-0xff01)
                cv.drawRect(0F, 0F, 64F, 64F, p);
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val sample = stream.toByteArray()


                var item = Model(0,"name1","hello", sample)
                modelDao.insert(item)
                item = Model(0,"name2","World!", sample)
                modelDao.insert(item)
            }

        }


    }


}