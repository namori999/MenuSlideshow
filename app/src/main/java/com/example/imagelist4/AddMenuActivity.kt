package com.example.imagelist4


import android.Manifest.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.ViewSwitcher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
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



    var mArrayUri: ArrayList<Uri>? = null
    var position:Int = 0




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
                val counter = findViewById<TextView>(R.id.countText)

                if (counter.getVisibility() == View.VISIBLE) {
                    // multiple data
                    intentMultipleData()

                } else {
                    // single data
                    intentData()
                }



                    finish()
                }

            }


    }



    companion object {
        const val EXTRA_NAME = "com.example.imagelist4.NAME"
        const val EXTRA_IMG = "com.example.imagelist4.IMG"
        const val EXTRA_URI = "com.example.imageList4.URI"
        const val EXTRA_MULTI_URI ="com.example.imageList4.IMGS"
        const val EXTRA_MULTI_NAME = "com.example.imageList4.NAMES"

    }

    private fun intentMultipleData() {
        val uris = intent.extras?.get(EXTRA_MULTI_URI) as ArrayList<Uri>

        val names = intent.extras?.get(EXTRA_MULTI_NAME) as ArrayList<String>
        val intent = Intent()
        val i = Intent(
            this,
            MenuListActivity::class.java
        )


        val b = Bundle()
        //b.putParcelableArrayList(EXTRA_MULTI_URI,uris)
        //b.putParcelableArrayList(EXTRA_MULTI_NAME,uris)

        b.putSerializable(EXTRA_MULTI_URI,uris)
        b.putStringArrayList(EXTRA_MULTI_NAME,names)

        i.putExtras(b)

        setResult(Activity.RESULT_OK, i)


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



    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {
            // if multiple images are selected

            if (data?.getClipData() != null) {
                var count = data.clipData?.itemCount



                val mArrayUri = arrayListOf<Uri>()
               // val mArrayImage = arrayListOf<String>()
                val mArrayName = arrayListOf<String>()

                val subjectEditText = findViewById<View>(R.id.subject_edittext) as EditText
                val imageView = findViewById<ImageView>(R.id.pickImage)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.elevation = 10F
                }

                if (count != null) {

                    for (i in 0..count - 1) {
                        val imageUri: Uri? = data.clipData?.getItemAt(i)?.uri
                        //     iv_image.setImageURI(imageUri) Here you can assign your Image URI to the ImageViews


                        val fileName = getFileNameFromUri(this, imageUri)
                        val name = getPreffix(fileName)
                        name?.let { mArrayName.add(it) }


                        //imageUri?.toString().let { mArrayImage.add(it.toString()) }

                        //val layout = findViewById<LinearLayout>(R.id.imageSwichLayaut)
                        //layout.visibility = VISIBLE

                        imageUri?.let { mArrayUri.add(it) }


                        //mArrayImage.add(imageUri.toString())


                        imageView.setImageURI(mArrayUri?.get(0))

                        subjectEditText.setText(fileName)

                    }

                    //sort by name

                    val beforeName = mArrayName
                    println("BEFOR NAME:" + beforeName)
                    val sortedName =  beforeName.sortedWith(String.CASE_INSENSITIVE_ORDER)
                    println("AFTER NAME:" +sortedName)



                    val beforeUri = mArrayUri
                    println("BEFOR URI:" + beforeUri)

                    val there = beforeUri.sortedBy { sortedName.indexOf(it) }// sorted nameを基準に並びかえてね
                    println("AFTER URI:" +there)

                    //sort using key map


                    //intentdata

                    intent.putExtra(EXTRA_MULTI_URI,mArrayUri)
                    intent.putExtra(EXTRA_MULTI_NAME,mArrayName)
                    val imageCount = data.clipData?.itemCount?.minus(1)

                    val counter = findViewById<TextView>(R.id.countText)
                    counter.visibility = VISIBLE
                    counter.text = "+ $imageCount images"


                    subjectEditText.visibility = INVISIBLE

                }

            } else if (data?.getData() != null) {
                // if single image is selected

                //   iv_image.setImageURI(imageUri) Here you can assign the picked image uri to your imageview


                val imageUri: Uri? = data!!.data

                val fileName = getFileNameFromUri(this, data.data)
                val name = getPreffix(fileName)
                val subjectEditText = findViewById<View>(R.id.subject_edittext) as EditText
                subjectEditText?.setText(name)


                val wm = getSystemService(WINDOW_SERVICE) as WindowManager
                val disp = wm.defaultDisplay

                val realSize = Point().also {
                    (this.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.apply {
                        getSize(
                            it
                        )
                    }
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
            }


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


    public class CustomObj(var customObjPropery : String){

        override fun toString(): String {
            return "$customObjPropery"    }

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

    fun getPreffix(fileName: String?): String? {
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
