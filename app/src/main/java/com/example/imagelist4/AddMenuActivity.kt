package com.example.imagelist4


import android.Manifest.*
import android.R.attr
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.View.VISIBLE
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.*
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


const val MENU_NAME = "name"
const val MENU_IMG = "img"


final class AddMenuActivity : AppCompatActivity() {
    private var addTodoBtn: Button? = null


    private var descEditText: EditText? = null
    var mImageView: ImageView? = null

    //private val dbName: String = "SampleDB"
    //private val tableName: String = "SampleTable"
    //private val dbVersion: Int = 1

    private lateinit var myDataSet: ArrayList<Model>

    private var mUserList:List<Model> = listOf()




    private val  recyclerView:RecyclerView?  =null

    private var mSQLiteHelper: SQLiteOpenHelper? = null

    val REQUEST_CODE_GALLERY = 999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "ADD MENU PAGE"
        setContentView(R.layout.activity_add_record)

        hideKeyboard()
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)


        val subjectEditText = findViewById<View>(R.id.subject_edittext) as EditText
        mImageView = findViewById(R.id.pickImage) as ImageView
        val addTodoBtn = findViewById<View>(R.id.add_record) as Button
        val progressBar = ProgressBar(this)



        myDataSet = ArrayList<Model>()


        //select image by on imageview click
        mImageView!!.setOnClickListener() {

            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "画像を選択"), REQUEST_CODE_GALLERY)

        }



        addTodoBtn.setOnClickListener() {
            // 親レイアウトに、ProgressBarを追加
            //val linearLayout = findViewById<LinearLayout>(R.id.layout)
            //linearLayout.removeView(progressBar)
            //linearLayout.addView(progressBar)


            // キーボードを非表示
            hideKeyboard()


            if (subjectEditText?.text.toString().equals("")) {
                Toast.makeText(this, "画像を選んでください", Toast.LENGTH_SHORT).show()
            } else {
// ProgressBarの生成


                // progressBar.visibility = View.VISIBLE


                    intentData()

                    finish()
                }


            }

    }

    companion object {
        const val EXTRA_NAME = "com.example.imagelist4.NAME"
        const val EXTRA_IMG = "com.example.imagelist4.IMG"
        const val EXTRA_URI = "com.example.imageList4.URI"

    }

    fun intentData(){
        val subjectEditText = findViewById<View>(R.id.subject_edittext) as EditText
        val name = subjectEditText?.text.toString()
        //val uri = bitmapToUri(mImageView!!,name)
        //        val image = onActivityResult(data).result.uri

        val uri = intent.getExtras()?.get(EXTRA_URI) as Uri?



        val intent = Intent()


// MainActivityを起動する
                //startActivity(intent)
                intent.putExtra(EXTRA_NAME, name)
                intent.putExtra(EXTRA_IMG, uri)
                setResult(Activity.RESULT_OK, intent)

    }

    // Sequentially executes doWorld followed by "Hello"
    fun main() = runBlocking {
        doWorld()
        println("Done")
        finish()
    }

    // Concurrently executes both sections
    suspend fun doWorld() = coroutineScope { // this: CoroutineScope
        launch {
            val subjectEditText = findViewById<View>(R.id.subject_edittext) as EditText
            val name = subjectEditText?.text.toString()


            val bitmap = (mImageView!!.drawable).toBitmap()
            val uri = getImageUriFromBitmap(this@AddMenuActivity,bitmap)

            val replyIntent = Intent()

// MainActivityを起動する
            startActivity(intent)
            replyIntent.putExtra(EXTRA_NAME,name)
            replyIntent.putExtra(EXTRA_IMG,uri)
            setResult(Activity.RESULT_OK, replyIntent)

            println("World 2")

        }
        launch {
            println("World 1")

        }

        val progressBar = findViewById<FrameLayout>(R.id.proBerLayout)
        progressBar.visibility = VISIBLE
        println("Hello")

    }


    fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri{
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }


    fun imageViewToByte(image: ImageView): ByteArray? {
        val bitmap = (image.drawable).toBitmap()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
        return stream.toByteArray()
    }


    private fun hideKeyboard() {
        val view = this@AddMenuActivity.currentFocus
        if (view != null) {
            val manager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    fun checkDataSize() {

        val subjectEditText = findViewById<View>(R.id.subject_edittext) as EditText
        val name = subjectEditText?.text.toString()
        val img = imageViewToByte(mImageView!!)
        val resultIntent = Intent()

        val lengthbmp = img?.size?.toLong()
        if (lengthbmp != null) {
            if (lengthbmp > 100000) {

                val builder = AlertDialog.Builder(this@AddMenuActivity)
                builder.setTitle("画像のサイズが大きすぎます")

                // Set other dialog properties
                // Create the AlertDialog
                builder.create()
                finish()


            } else {

                return

            }
        }

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {

            val imageUri: Uri? = data!!.data

            val fileName = getFileNameFromUri(this, data.data)
            val name = getPreffix(fileName)
            val subjectEditText = findViewById<View>(R.id.subject_edittext) as EditText
            subjectEditText?.setText(name)


            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            val disp = wm.defaultDisplay

            val realSize = Point().also {
                (this.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.apply { getSize(
                    it
                ) }
            }
            //disp.getRealSize(realSize)

            val realScreenWidth = realSize.x
            val realScreenHeight = realSize.y

            CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON) //enable image guidlines
                .setAspectRatio(realScreenHeight, realScreenWidth) // image will be square
                .setBorderLineThickness(
                    getResources().getDimensionPixelSize(R.dimen.thickness).toFloat()
                ) //borderline thickness wll be 1dp
                .start(this)


        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val result = CropImage.getActivityResult(data)

            if (resultCode == RESULT_OK) {



                (findViewById<View>(R.id.pickImage) as ImageView).setImageURI(
                        result.uri
                    )

                intent.putExtra(EXTRA_URI,result.uri)


            }

            super.onActivityResult(requestCode, resultCode, data)
        }

    }



    fun getFileNameFromUri(context: Context, uri: Uri?): String? {
        // is null
        if (null == uri) {
            return null
        }

        // get scheme
        val scheme = uri.scheme

        // get file name
        var fileName: String? = null
        when (scheme) {
            "content" -> {
                val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
                val cursor: Cursor? = context.contentResolver
                    .query(uri, projection, null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        fileName = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                        )
                    }
                    cursor.close()
                }
            }
            "file" -> fileName = File(uri.path).getName()
        }

        return fileName

    }

    open fun getPreffix(fileName: String?): String? {
        if (fileName == null) return null
        val point = fileName.lastIndexOf(".")
        return if (point != -1) {
            fileName.substring(0, point)
        } else fileName
    }

    open fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }



}
