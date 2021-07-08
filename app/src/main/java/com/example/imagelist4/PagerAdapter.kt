package com.example.imagelist4



import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.lang.Integer.MAX_VALUE


class PagerAdapter() : RecyclerView.Adapter<PagerAdapter.PagerViewHolder>() {

    private var myDataSet: List<Model>? = null
    private var viewPager2: ViewPager2? = null


    fun submitList(myDataSet: List<Model>?) {

        this.myDataSet = myDataSet

        //これ大事。ないと、データ追加後に画面が更新されません。
        notifyDataSetChanged()

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        return PagerViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {

        val current = myDataSet?.get(position)

        holder.bind(current?.name, current?.image)

        if (position == myDataSet?.size!! - 2) {
            viewPager2?.post(runnable);
        } /*else if (position == 2) {
            viewPager2.post(runnable2);
        }*/
    }

    class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtView: TextView = itemView.findViewById(R.id.pagerText)
        val imageView: ImageView = itemView.findViewById(R.id.pagerImg)

        fun bind(text: String?, image: ByteArray?) {

            txtView.text = text
            val recordImage = image
            val bitmap = recordImage?.size?.let {
                BitmapFactory.decodeByteArray(
                    recordImage, 0,
                    it
                )
            }
            imageView.setImageBitmap(bitmap)


        }

        companion object {
            fun create(parent: ViewGroup): PagerViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.pager_item, parent, false)
                return PagerViewHolder(view)
            }

        }

    }

    private val runnable = Runnable {
        viewPager2!!.setCurrentItem(0, true)
        myDataSet = myDataSet
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {

        return if (myDataSet == null) 0 else myDataSet?.size!!


    }

    fun getAllData(): List<Model>? {
        return myDataSet

    }

}