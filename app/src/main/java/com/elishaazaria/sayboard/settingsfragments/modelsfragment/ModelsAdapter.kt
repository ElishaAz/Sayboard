package com.elishaazaria.sayboard.settingsfragments.modelsfragment

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elishaazaria.sayboard.R

class ModelsAdapter(context: Context, dataProvider: AdapterDataProvider) :
    RecyclerView.Adapter<ModelsAdapter.ViewHolder>() {
    enum class DataState {
        CLOUD, INSTALLED, DOWNLOADING, QUEUED
    }

    private val context: Context
    private val dataProvider: AdapterDataProvider
    private val mData: MutableList<ModelsAdapterData>
    private val mInflater: LayoutInflater
    private var mClickListener: ItemClickListener? = null

    // data is passed into the constructor
    init {
        mInflater = LayoutInflater.from(context)
        this.context = context
        this.dataProvider = dataProvider
        mData = ArrayList(dataProvider.data)
    }

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.fragment_models_entry, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mData[position]
        holder.titleTextView.text = data.title
        holder.subtitleTextView.text = data.subtitle
        holder.downloadButton.setImageResource(data.imageRes)
        holder.data = data
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mData.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var titleTextView: TextView
        var subtitleTextView: TextView
        var downloadButton: ImageButton
        var data: ModelsAdapterData? = null

        init {
            titleTextView = itemView.findViewById(R.id.titleTextView)
            subtitleTextView = itemView.findViewById(R.id.subtitleTextView)
            downloadButton = itemView.findViewById(R.id.downloadButton)
            downloadButton.setOnClickListener { view: View -> onButtonClick(view) }
            itemView.setOnClickListener { view: View -> onClick(view) }
        }

        private fun onClick(view: View) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition, data!!)
        }

        private fun onButtonClick(view: View) {
            if (mClickListener != null) {
                mClickListener!!.onButtonClicked(view, adapterPosition, data!!)
            }
        }
    }

    // convenience method for getting data at click position
    fun getItem(index: Int): ModelsAdapterData {
        return mData[index]
    }

    fun size(): Int {
        return mData.size
    }

    fun changed(data: ModelsAdapterData?): Boolean {
        val index = mData.indexOf(data)
        if (index == -1) return false
        notifyItemChanged(index)
        return true
    }

    fun removed(data: ModelsAdapterData?): Boolean {
        val index = mData.indexOf(data)
        if (index == -1) return false
        mData.removeAt(index)
        notifyItemRemoved(index)
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reload() {
        mData.clear()
        mData.addAll(dataProvider.data)
        notifyDataSetChanged()
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View, position: Int, data: ModelsAdapterData)
        fun onButtonClicked(view: View, position: Int, data: ModelsAdapterData)
    }
}