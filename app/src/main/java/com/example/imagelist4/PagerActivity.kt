package com.example.imagelist4

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.annotation.DimenRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PagerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


class PagerFragment : AppCompatActivity() {


    private var mSQLiteHelper: SQLiteOpenHelper? = null
    private val dbName: String = "SampleDB"
    private val dbVersion: Int = 1

    private val CODE_AUTHENTICATION_VERIFICATION = 241

    private val modelViewModel: ModelViewModel by viewModels {
        ModelViewModel.ModelViewModelFactory((application as ModelApplication).repository)
    }

    lateinit var modelDao: ModelDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()

        setContentView(R.layout.fragment_pager)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        val adapter = PagerAdapter()

        val observer =  Observer<List<Model>> { model ->
            // Update the cached copy of the words in the adapter.
            model?.let { adapter.submitList(it) }
        }
        modelViewModel.allWords.observe(this,observer)


        // RecyclerView の設定
        val viewPager2 = findViewById<ViewPager2>(R.id.viewpager2)

        viewPager2.adapter = adapter





        viewPager2?.orientation = ViewPager2.ORIENTATION_HORIZONTAL

// You need to retain one page on each side so that the next and previous items are visible
        viewPager2?.offscreenPageLimit = 1


// Add a PageTransformer that translates the next and previous items horizontally
// towards the center of the screen, which makes them visible
        val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
        val currentItemHorizontalMarginPx = resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
        val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
        val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
            page.translationX = -pageTranslationX * position
            // Next line scales the item's height. You can remove it if you don't want this effect
            page.scaleY = 1 - (0.25f * abs(position))
            // If you want a fading effect uncomment the next line:
            // page.alpha = 0.25f + (1 - abs(position))
        }
        viewPager2?.setPageTransformer(pageTransformer)

// The ItemDecoration gives the current (centered) item horizontal margin so that
// it doesn't occupy the whole screen width. Without it the items overlap
        val itemDecoration = HorizontalMarginItemDecoration(
            this,
            R.dimen.viewpager_current_item_horizontal_margin
        )
        viewPager2?.addItemDecoration(itemDecoration)





    }



    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(this.javaClass.name, "back button pressed")

            val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (km.isKeyguardSecure) {
                val i =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        km.createConfirmDeviceCredentialIntent(
                            "Authentication required",
                            "password"
                        )
                    } else {
                        TODO("VERSION.SDK_INT < LOLLIPOP")
                    }
                startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION)

            }
        }
        return super.onKeyDown(keyCode, event)

    }






    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_AUTHENTICATION_VERIFICATION) {
            // Challenge completed, proceed with using cipher
            if (resultCode == RESULT_OK) {
                startActivity(Intent(applicationContext, MenuListActivity::class.java))
            }
        }
    }



    override fun onResume() {
        super.onResume()
        hideSystemUI()

        val adapter = ModelListAdapter()



        Log.d("debug", "onResume()")
    }



    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }



}

class HorizontalMarginItemDecoration(context: Context, @DimenRes horizontalMarginInDp: Int) :
    RecyclerView.ItemDecoration() {

    private val horizontalMarginInPx: Int =
        context.resources.getDimension(horizontalMarginInDp).toInt()

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.right = horizontalMarginInPx
        outRect.left = horizontalMarginInPx
    }

}