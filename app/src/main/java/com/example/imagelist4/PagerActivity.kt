package com.example.imagelist4

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.activity.viewModels
import androidx.annotation.DimenRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import java.lang.Math.abs
import java.util.*
import kotlin.collections.ArrayList


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


    private val CODE_AUTHENTICATION_VERIFICATION = 241

    private val modelViewModel: ModelViewModel by viewModels {
        ModelViewModel.ModelViewModelFactory((application as ModelApplication).repository)
    }


    private var scrollHandler = Handler(Looper.getMainLooper())

    companion object {
        const val SCROLL_DELAY = 10000L
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()

        setContentView(R.layout.fragment_pager)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        val myDataSet = ArrayList<Model>()

        val adapter = PagerAdapter()

        val observer =  Observer<List<Model>> { model ->
            // Update the cached copy of the words in the adapter.
            model?.let { adapter.submitList(it) }
        }
        modelViewModel.allWords.observe(this,observer)



        // RecyclerView の設定
        val viewPager2 = findViewById<ViewPager2>(R.id.viewpager2)

        viewPager2.adapter = adapter



        val scrollRunnable = Runnable {
            viewPager2.currentItem = viewPager2.currentItem + 1
        }


        viewPager2?.orientation = ViewPager2.ORIENTATION_HORIZONTAL

// You need to retain one page on each side so that the next and previous items are visible
       // viewPager2?.offscreenPageLimit = 1


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


        viewPager2.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        viewPager2.apply {
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                var currentPage: Int = 0
                var mPreviousPosition: Int = 0
                var mIsEndOfCycle = false

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    scrollHandler.removeCallbacks(scrollRunnable)
                    scrollHandler.postDelayed(scrollRunnable, SCROLL_DELAY)
                }

                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                        val curr: Int = viewPager2.getCurrentItem()
                        val lastReal: Int = adapter.itemCount - 2
                        if (curr == 0) {
                            viewPager2.setCurrentItem(lastReal, false)
                        } else if (curr > lastReal) {

                            viewPager2.setCurrentItem(0, false)
                        }
                    }
                }

            })
        }




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


    override fun onResume() {
        super.onResume()
        hideSystemUI()
        val viewPager2 = findViewById<ViewPager2>(R.id.viewpager2)

        val scrollRunnable = Runnable {
            viewPager2.currentItem = viewPager2.currentItem + 1
        }

        scrollHandler.postDelayed(scrollRunnable, SCROLL_DELAY)


    }

    override fun onPause() {
        super.onPause()
        val viewPager2 = findViewById<ViewPager2>(R.id.viewpager2)

        val scrollRunnable = Runnable {
            viewPager2.currentItem = viewPager2.currentItem + 1
        }

        scrollHandler.removeCallbacks(scrollRunnable)

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