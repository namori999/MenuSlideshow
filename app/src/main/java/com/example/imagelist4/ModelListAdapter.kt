package com.example.imagelist4



import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView


class ModelListAdapter : RecyclerView.Adapter<ModelListAdapter.ModelViewHolder>() {
    private val mContext: Context? = null

    private var myDataSet: List<Model>? = null
    private val mWords // Cached copy of words
            : List<Model>? = null


    // リスナー格納変数
    private lateinit var listener: onItemClickListener




    fun submitList(myDataSet: List<Model>?): List<Model>? {


        this.myDataSet = myDataSet

        //これ大事。ないと、データ追加後に画面が更新されません。
        notifyDataSetChanged()

        return myDataSet

    }



    fun notify(myDataSet: List<Model>?){
        this.myDataSet = myDataSet
        notifyDataSetChanged()
    }


    //インターフェースの作成
    interface onItemClickListener {
        fun onItemClick(position: Int)
    }


    fun setOnItemClickListener(listener: onItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        return ModelViewHolder.create(parent)
    }


    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        val current = myDataSet?.get(position)

        holder.bind(current?.name, current?.image)
        holder.imageView?.setOnClickListener() {
            listener.onItemClick(position)
        }


    }

    class ModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtView: TextView = itemView.findViewById(R.id.txtName)
        val imageView: ImageView = itemView.findViewById(R.id.imgIcon)
        private val handleView: ImageView = itemView.findViewById(R.id.handleButton)


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
            handleView.setImageResource(R.drawable.ic_baseline_drag_handle_24)


        }

        companion object {
            fun create(parent: ViewGroup): ModelViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.row, parent, false)
                return ModelViewHolder(view)
            }

        }

    }

    class WordsComparator : DiffUtil.ItemCallback<Model>() {
        override fun areItemsTheSame(oldItem: Model, newItem: Model): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Model, newItem: Model): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun getItemCount(): Int {
        return if (myDataSet == null) 0 else myDataSet?.size!!
    }


    override fun getItemId(position: Int): Long {
        return if (myDataSet == null) 0 else myDataSet?.get(position)?.id!!.toLong()
    }


    fun getItemAtPosition(position: Int): Model? {
        return mWords?.get(position);
    }

    fun notifyRemoved(position: Int){
        this.notifyItemRemoved(position)
        this.notifyDataSetChanged()

    }

}


