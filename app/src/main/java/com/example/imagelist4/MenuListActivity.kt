package com.example.imagelist4



import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.TypeConverter
import com.example.imagelist4.AddMenuActivity.Companion.EXTRA_IMG
import com.example.imagelist4.AddMenuActivity.Companion.EXTRA_NAME
import com.example.imagelist4.R.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class MenuListActivity() : AppCompatActivity() {

    var mImageViewIcon: ImageView? = null

    private val newWordActivityRequestCode = 1
    private val modelViewModel: ModelViewModel by viewModels {
        ModelViewModel.ModelViewModelFactory((application as ModelApplication).repository)

    }

    lateinit var modelDao: ModelDao



    companion object {
        const val RESULT_ACTIVITY = 1000
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


        // RecyclerView の設定
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


        // インターフェースの実装
        adapter.setOnItemClickListener(object : ModelListAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                Toast.makeText(applicationContext, "${position}がタップされました", Toast.LENGTH_LONG).show()


                val array = arrayOf("画像を編集する", "キャンセル")
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

                //handleButton.visibility = View.VISIBLE
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

            val fromPos = viewHolder?.adapterPosition ?: 0
            val toPos = target?.adapterPosition ?: 0
            val adapter = ModelListAdapter()

            //Collections.swap(, fromPos, toPos)

            reallyMoved(fromPos, toPos)
            adapter?.notifyItemMoved(fromPos, toPos)

            return true
        }



        // 1. 行が選択された時に、このコールバックが呼ばれる。ここで行をハイライトする。
        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)

            if (actionState == ACTION_STATE_DRAG) {
                viewHolder?.itemView?.alpha = 0.5f
            }
        }

        // 2. 行が選択解除された時 (ドロップされた時) このコールバックが呼ばれる。ハイライトを解除する。
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


        //スワイプ時に実行
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            val progress: ProgressBar = findViewById(R.id.progress)
            progress.visibility = VISIBLE

            val position: Int = viewHolder.adapterPosition
            val dataset = getValue(modelViewModel.allWords)
            val myWord =dataset?.get(position)

            val adapter = ModelListAdapter()

            //snackbar UNDO
            val view: View = this@MenuListActivity.findViewById(R.id.layout)

            Snackbar.make(view, "削除されました", Snackbar.LENGTH_LONG)
                .setAction("戻す", { v ->
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


        if (fromPos < toPos) {
            for (i in fromPos until toPos) {
                Collections.swap(mWordEntities, i, i + 1)
                val order1: Int = mWordEntities?.get(i)!!.id
                val order2: Int = mWordEntities?.get(i + 1)!!.id

                val fName = mWordEntities?.get(i).name
                val fImg = mWordEntities?.get(i).image

                val tName = mWordEntities?.get(i + 1).name
                val tImg = mWordEntities?.get(i + 1).image

                modelViewModel.update(Model(order2,fName,fImg))
                modelViewModel.update(Model(order1,tName,tImg))

            }
        } else {
            for (i in fromPos downTo toPos + 1) {
                Collections.swap(mWordEntities, i, i - 1)
                val order1: Int = mWordEntities?.get(i)!!.id
                val order2: Int = mWordEntities?.get(i - 1).id
                val fName = mWordEntities?.get(i)!!.name
                val fImg = mWordEntities?.get(i).image

                val tName = mWordEntities?.get(i - 1).name
                val tImg = mWordEntities?.get(i - 1).image

                modelViewModel.update(Model(order2,fName,fImg))
                modelViewModel.update(Model(order1,tName,tImg))
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


        Log.d("debug", "onResume()")

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        if (requestCode == 123 && resultCode == RESULT_OK) {
            val name = intentData?.getStringExtra(EXTRA_NAME)
            val uri = intentData?.getExtras()?.get(EXTRA_IMG) as Uri?

            val imageArray = convertImageToByte(uri)

            val data = Model(0, name, imageArray)

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

    @TypeConverter
    fun convertImageToByte(uri: Uri?): ByteArray? {
        var data: ByteArray? = null
        try {
            val cr = baseContext.contentResolver
            val inputStream = cr.openInputStream(uri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val baos = ByteArrayOutputStream()

            val sBitmap = getResizedBitmap(bitmap, 700)
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
