package com.elishaazaria.sayboard.settingsfragments.modelsfragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.elishaazaria.sayboard.R;
import com.elishaazaria.sayboard.downloader.messages.ModelInfo;

import java.util.ArrayList;
import java.util.List;

public class ModelsAdapter extends RecyclerView.Adapter<ModelsAdapter.ViewHolder> {

    public enum DataState {
        CLOUD, INSTALLED, DOWNLOADING, QUEUED
    }

    private final Context context;
    private final AdapterDataProvider dataProvider;
    private final List<ModelsAdapterData> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public ModelsAdapter(Context context, AdapterDataProvider dataProvider) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.dataProvider = dataProvider;
        mData = new ArrayList<>(dataProvider.getData());
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.fragment_models_entry, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ModelsAdapterData data = mData.get(position);
        holder.titleTextView.setText(data.getTitle());
        holder.subtitleTextView.setText(data.getSubtitle());
        holder.downloadButton.setImageResource(data.getImageRes());

        holder.data = data;
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView subtitleTextView;
        ImageButton downloadButton;
        ModelsAdapterData data;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            subtitleTextView = itemView.findViewById(R.id.subtitleTextView);
            downloadButton = itemView.findViewById(R.id.downloadButton);

            downloadButton.setOnClickListener(this::onButtonClick);
            itemView.setOnClickListener(this::onClick);
        }

        private void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition(), data);
        }

        private void onButtonClick(View view) {
            if (mClickListener != null) {
                mClickListener.onButtonClicked(view, getAdapterPosition(), data);
            }
        }
    }

    // convenience method for getting data at click position
    public ModelsAdapterData getItem(int index) {
        return mData.get(index);
    }

    public int size() {
        return mData.size();
    }

    public boolean changed(ModelsAdapterData data) {
        int index = mData.indexOf(data);
        if (index == -1) return false;
        notifyItemChanged(index);
        return true;
    }

    public boolean removed(ModelsAdapterData data) {
        int index = mData.indexOf(data);
        if (index == -1) return false;
        mData.remove(index);
        notifyItemRemoved(index);
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void reload() {
        mData.clear();
        mData.addAll(dataProvider.getData());
        notifyDataSetChanged();
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position, ModelsAdapterData data);

        void onButtonClicked(View view, int position, ModelsAdapterData data);
    }
}
