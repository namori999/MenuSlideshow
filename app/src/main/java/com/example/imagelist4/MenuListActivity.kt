package com.example.imagelist4



import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.util.Log
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.TypeConverter
import com.example.imagelist4.AddMenuActivity.Companion.EXTRA_IMG
import com.example.imagelist4.AddMenuActivity.Companion.EXTRA_MULTI_NAME
import com.example.imagelist4.AddMenuActivity.Companion.EXTRA_MULTI_URI
import com.example.imagelist4.AddMenuActivity.Companion.EXTRA_NAME
import com.example.imagelist4.R.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.io.*
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MenuListActivity() : AppCompatActivity() {

    var mImageViewIcon: ImageView? = null

    private val newWordActivityRequestCode = 1
    private val modelViewModel: ModelViewModel by viewModels {
        ModelViewModel.ModelViewModelFactory((application as ModelApplication).repository)

    }

    lateinit var modelDao: ModelDao



    companion object {
        const val RESULT_ACTIVITY = 1000
        const val PICK_FILE = 2
        const val NAME_TO_INSERT = "com.example.imagelist4.NAME_TO_INSERT"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_menu_list)

        val progressBar = findViewById<ProgressBar>(id.progress)

        getSupportActionBar()?.setTitle("MENU LIST")
        // add back button
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setHomeAsUpIndicator(drawable.ic_baseline_arrow_back_ios_24)


        // RecyclerView ?????????
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = ModelListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)




        modelViewModel.allWords.observe(this,
            Observer<List<Any?>?> { model -> // Update the cached copy of the words in the adapter.
                adapter.submitList(model as List<Model>?)
            })


        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback())
        itemTouchHelper.attachToRecyclerView(recyclerView)


        findViewById<FloatingActionButton>(id.fab).setOnClickListener {
            val intent = Intent(this, AddMenuActivity::class.java)

            val requestCode = 123
            startActivityForResult(intent, requestCode)
        }


        // ?????????????????????????????????
        adapter.setOnItemClickListener(object : ModelListAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                Toast.makeText(applicationContext, "${position}???????????????????????????", Toast.LENGTH_LONG).show()


                val array = arrayOf("?????????????????????", "???????????????")
                val builder = AlertDialog.Builder(this@MenuListActivity)

                // Set a title for alert dialog
                builder.setTitle("")
                builder.setItems(array, { dialogInterface, i ->
                    if (i == 0) {
                        //show update dialog
                        //showDialogUpdate(this@MenuListActivity, arrID[position], arrImage[position])
                    } else if (i == 1) {


                    }
                })

                val dialog = builder.create()
                dialog.show()
            }
        })


    }


    private fun showDialogUpdate(activity: Activity, position: Int, image: ByteArray) {
        val dialog = Dialog(activity)
        dialog.setContentView(layout.update_dialog)

        dialog.setTitle("Update")
        mImageViewIcon = findViewById(R.id.imageViewRecord) as ImageView?
        val edtName: EditText = dialog.findViewById(id.edtName)
        val btnUpdate: Button = dialog.findViewById(id.btnUpdate)

        val bmp = BitmapFactory.decodeByteArray(image, 0, 1)

        //mImageViewIcon?.setImageBitmap(Bitmap.createScaledBitmap(bmp, image.width, image.height, false))


        val width = (activity.resources.displayMetrics.widthPixels * 0.95).toInt()
        val height = (activity.resources.displayMetrics.heightPixels * 0.7).toInt()
        dialog.getWindow()?.setLayout(width, height)
        dialog.show()

//in update dialog click image view to update image

        mImageViewIcon?.setOnClickListener() {
            ActivityCompat.requestPermissions(
                this@MenuListActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                888
            )

        }

        btnUpdate.setOnClickListener() {


        }
    }


    fun imageViewToByte(image: ImageView): ByteArray? {
        val bitmap = (image.drawable as BitmapDrawable).bitmap
        //when compressing is needed
        // val smallImg = getResizedBitmap(bitmap,600)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        //smallImg?.compress(Bitmap.CompressFormat.PNG, 70, stream)

        return stream.toByteArray()

    }

    fun getItemTouchHelper(): ItemTouchHelper {
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback())
        return itemTouchHelper
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.listmenu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.move -> {

                val array = arrayOf("????????????" , "???????????????")
                val builder = AlertDialog.Builder(this@MenuListActivity)

                // Set a title for alert dialog
                builder.setTitle("")
                builder.setItems(array, { dialogInterface, i ->
                    if (i == 0) {




                        Toast.makeText(this,"sorted !",Toast.LENGTH_LONG)

                    } else if (i == 1) {
                        finish()

                    }
                })

                val dialog = builder.create()
                dialog.show()



                //handleButton.visibility = View.VISIBLE
            }
            R.id.deleteAll ->{

                val array = arrayOf("?????????????????????", "???????????????")
                val builder = AlertDialog.Builder(this@MenuListActivity)

                // Set a title for alert dialog
                builder.setTitle("?????????????????????????????????????????????????????????")
                builder.setItems(array, { dialogInterface, i ->
                    if (i == 0) {
                        modelViewModel.deleteAll()
                        Toast.makeText(this,"all data deleted !",Toast.LENGTH_LONG)

                    } else if (i == 1) {
                        finish()

                    }
                })

                val dialog = builder.create()
                dialog.show()

            }
        }
        return super.onOptionsItemSelected(item)
    }


    fun itemTouchHelperCallback() = object : ItemTouchHelper.SimpleCallback(
        UP or DOWN or LEFT or RIGHT or START or END,
        LEFT or RIGHT
    ) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {

            val fromPos = viewHolder?.absoluteAdapterPosition
            val toPos = target?.absoluteAdapterPosition
            val adapter = ModelListAdapter()


            //Collections.swap(, fromPos, toPos)

            reallyMoved(fromPos, toPos)
            adapter?.notifyItemMoved(fromPos, toPos)

            return true
        }



        // 1. ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)

            if (actionState == ACTION_STATE_DRAG) {
                viewHolder?.itemView?.alpha = 0.5f
            }
        }

        // 2. ?????????????????????????????? (????????????????????????) ???????????????????????????????????????????????????????????????????????????
        override fun clearView(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) {
            super.clearView(recyclerView, viewHolder)

            viewHolder?.itemView?.alpha = 1.0f
        }

        override fun isLongPressDragEnabled(): Boolean {
            return true

        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return true
        }


        //????????????????????????
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            val progress: ProgressBar = findViewById(R.id.progress)
            progress.visibility = VISIBLE

            val position: Int = viewHolder.getBindingAdapterPosition()
            val dataset = getValue(modelViewModel.allWords)
            val myWord =dataset?.get(position)

            val adapter = ModelListAdapter()

            //snackbar UNDO
            val view: View = this@MenuListActivity.findViewById(R.id.layout)

            Snackbar.make(view, "?????????????????????", Snackbar.LENGTH_LONG)
                .setAction("??????", { v ->
                    //if (data != null) {
                    //    modelViewModel.insert(data)
                    //}

                    if (myWord != null) {
                        modelViewModel.insert(myWord)
                    }
                    adapter.notifyItemInserted(position)
                    progress.visibility = View.INVISIBLE


                })
                .show()


            if (myWord != null) {
                reallyDeleted(position,myWord)
            }



            Handler().postDelayed(Runnable {
                hydeProgressBar()
            }, 1000)

        }
    }

    fun reallyDeleted(position:Int,myWord:Model){
        // Delete the word
        myWord?.let { modelViewModel.delete(it) }
        val adapter = ModelListAdapter()
        adapter.notifyRemoved(position)

    }

    private fun reallyMoved(
        fromPos: Int,
        toPos: Int,
    ) {
        val mWordEntities = getValue(modelViewModel.allWords)

        //val fId: Int = mWordEntities?.get(fromPos)!!.id
        //val fName = mWordEntities?.get(fromPos)?.name
        //val fImg = mWordEntities?.get(fromPos)?.image

       // val tName = mWordEntities?.get(i + 1).name
        //val tImg = mWordEntities?.get(i + 1).image

        //println("FROM data:" + fName + fImg + fId)



        if (fromPos < toPos) {
            for (i in fromPos until toPos) {
                //Collections.swap(mWordEntities, i, i + 1)
                val order1: Int = mWordEntities?.get(i)!!.id
                val order2: Int = mWordEntities?.get(i + 1)!!.id

                val fName = mWordEntities?.get(i).name
                val fUnName = mWordEntities?.get(i).unName
                val fImg = mWordEntities?.get(i).image

                val tName = mWordEntities?.get(i + 1).name
                val tUnName = mWordEntities?.get(i + 1).unName
                val tImg = mWordEntities?.get(i + 1).image

                modelViewModel.update(Model(order1,tUnName,fName,fImg))
                modelViewModel.update(Model(order2,fUnName,tName,tImg))

            }
        } else {
            for (i in fromPos downTo toPos + 1) {
                //Collections.swap(mWordEntities, i, i - 1)
                val order1: Int = mWordEntities?.get(i)!!.id
                val order2: Int = mWordEntities?.get(i - 1).id

                val fName = mWordEntities?.get(i)!!.name
                val fUnName = mWordEntities?.get(i).unName
                val fImg = mWordEntities?.get(i).image

                val tName = mWordEntities?.get(i - 1).name
                val tUnName = mWordEntities?.get(i - 1).unName
                val tImg = mWordEntities?.get(i - 1).image

                modelViewModel.update(Model(order1,tUnName,fName,fImg))
                modelViewModel.update(Model(order2,fUnName,tName,tImg))
            }
        }


    }


    @Throws(InterruptedException::class)
    fun <T> getValue(liveData: LiveData<T>): T? {
        val objects = arrayOfNulls<Any>(1)
        val latch = CountDownLatch(1)
        val observer: Observer<*> = object : Observer<Any?> {
            override fun onChanged(@Nullable o: Any?) {
                objects[0] = o
                latch.countDown()
                liveData.removeObserver(this)
            }
        }
        liveData.observeForever(observer as Observer<in T>)
        latch.await(2, TimeUnit.SECONDS)
        return objects[0] as T?
    }




    private fun hydeProgressBar() {
        val progress: ProgressBar = findViewById(R.id.progress)
        progress.visibility = View.INVISIBLE

    }




    override fun onPause() {
        super.onPause()

        val progress: ProgressBar = findViewById(R.id.progress)
        progress.visibility = VISIBLE
        Log.d("debug", "onPause()")
    }


    override fun onResume() {
        super.onResume()


        val adapter = ModelListAdapter()


        val progress: ProgressBar = findViewById(R.id.progress)
        progress.visibility = INVISIBLE


        setupPermissions()


        Log.d("debug", "onResume()")

    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)


        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("TAG", "Permission to record denied")
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Permission to access the microphone is required for this app to open document.")
                        .setTitle("Permission required")

                            builder.setPositiveButton("OK"
                            ) { dialog, id ->
                        Log.i("TAG", "Clicked")
                        makeRequest()
                    }

                    val dialog = builder.create()
                dialog.show()
            } else {
                makeRequest()
            }
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PICK_FILE)
    }

   override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PICK_FILE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i("debug", "Permission has been denied by user")
                } else {
                    Log.i("debug", "Permission has been granted by user")
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
        if (requestCode == 888) {
            if (resultCode == RESULT_OK) {
                val sourceTreeUri = intentData?.data
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (sourceTreeUri != null) {
                        this.getContentResolver().takePersistableUriPermission(
                            sourceTreeUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }
                }
                return
            }

        }

        if (requestCode == 123 && resultCode == RESULT_OK) {
            val name = intentData?.getStringExtra(EXTRA_NAME)
            val uri = intentData?.getExtras()?.get(EXTRA_IMG) as Uri?


            if (name == null){
                //multiple data

                val b = intentData?.extras
                val PlatosIntent = intentData?.extras
                val desc_plato = PlatosIntent?.getStringArrayList(EXTRA_MULTI_NAME)
                //val imagen_plato = PlatosIntent?.getParcelable(EXTRA_MULTI_URI) as ArrayList<Uri>

                val imagen_plato: ArrayList<Uri>? = PlatosIntent?.getParcelableArrayList(EXTRA_MULTI_URI)


                //val imagen_plato = PlatosIntent?.getStringArrayList(EXTRA_MULTI_URI)
                               /** */
                if (PlatosIntent != null) {
                    for (key in PlatosIntent.keySet()) {
                        val value = PlatosIntent[key]
                        println(
                            "BUNDLE KEYS:" + String.format(
                                "%s %s\n", key,
                                value.toString()
                            )
                        )
                    }


                    if (desc_plato != null) {
                        if (imagen_plato != null) {
                         //   checkPermission(desc_plato,imagen_plato)
                        }
                    }

                    var count = imagen_plato?.size
                    for (i in 0..count!! -1) {
                        // adding imageuri in array
                        ActivityCompat.requestPermissions(
                            this@MenuListActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            888
                        )

                        val name: String? = desc_plato?.get(i)

                        val uri = imagen_plato?.get(i)


                        val imageArray = convertImageToByte(uri)



                       val data = Model(0, name, name, imageArray)


                       modelViewModel.insert(data)

                    }
                    Toast.makeText(
                        applicationContext,
                        "$count items inserted !",
                        Toast.LENGTH_LONG
                    ).show()

                }




            } else {
                //single data


                val imageArray = convertImageToByte(uri)

                val data = Model(0, name, name, imageArray)

                val lengthbmp = imageArray?.size?.toLong()


                if (lengthbmp != null) {
                    if (lengthbmp > 100000) {

                        Toast.makeText(
                            applicationContext,
                            "too big Image",
                            Toast.LENGTH_LONG
                        ).show()


                    } else {


                        modelViewModel.insert(data)
                        Toast.makeText(
                            applicationContext,
                            "item inserted!",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }






        } else {
            Toast.makeText(
                applicationContext,
                "Data not saved becouse it is empty",
                Toast.LENGTH_LONG
            ).show()
        }
        Log.d("debug", "onActivityResult()")
    }



    fun checkPermission( nameArray :ArrayList<String>,imageUri :ArrayList<Uri>) {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type =  "image/*"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, imageUri)
            putExtra(NAME_TO_INSERT,nameArray)
        }
        startActivityForResult(intent, 888)




    }





    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val contentResolver = applicationContext.contentResolver
        val parcelFileDescriptor: ParcelFileDescriptor =
            contentResolver.openFileDescriptor(uri, "r")!!
        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    @TypeConverter
    fun convertImageToByte(uri: Uri?): ByteArray? {
        //if (uri != null) {
        //    isVirtualFile(uri)
        //}

        var data: ByteArray? = null
        try {
            val cr = baseContext.contentResolver
            //val inputStream = uri?.let { cr.openInputStream(it) }
            val bitmap = uri?.let { getBitmapFromUri(it) }
            val baos = ByteArrayOutputStream()


            val sBitmap = bitmap?.let { getResizedBitmap(it, 700) }
            sBitmap?.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            data = baos.toByteArray()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        return data
    }




    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
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




    private fun updateImage(cropped: ByteArray, position: Int) {

    }





}
