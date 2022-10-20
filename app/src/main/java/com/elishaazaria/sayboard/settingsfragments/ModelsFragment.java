package com.elishaazaria.sayboard.settingsfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elishaazaria.sayboard.Model;
import com.elishaazaria.sayboard.Tools;
import com.elishaazaria.sayboard.databinding.FragmentModelsBinding;
import com.elishaazaria.sayboard.downloader.Communication;
import com.elishaazaria.sayboard.downloader.messages.DownloadError;
import com.elishaazaria.sayboard.downloader.messages.DownloadProgress;
import com.elishaazaria.sayboard.downloader.messages.DownloadState;
import com.elishaazaria.sayboard.downloader.messages.Status;
import com.elishaazaria.sayboard.downloader.messages.UnzipProgress;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ModelsFragment extends Fragment implements ModelsAdapter.ItemClickListener {
    private static final String TAG = "ModelsFragment";

    private FragmentModelsBinding binding;
    private ModelsAdapter adapter;
    private boolean isDownloading = false;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentModelsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerView;
        progressBar = binding.progressBar;

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ModelsAdapter(getContext(), Tools.getModelsData(getContext()));
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(View view, int position, ModelsAdapter.Data data) {
    }

    @Override
    public void onDownloadButtonClicked(View view, int position, ModelsAdapter.Data data) {
        if (data.isInstalled() || isDownloading) return;
        isDownloading = true;
//        OnDownloadStatusListener listener = new OnDownloadStatusListener() {
//            @Override
//            public void onDownloadStarted() {
//                progressBar.setVisibility(View.VISIBLE);
//                data.setDownloading(true);
//                adapter.changed(data);
////                Toast.makeText(getContext(), "onDownloadStarted", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onDownloadCompleted() {
//                isDownloading = false;
//                progressBar.setVisibility(View.GONE);
//                Model model = Tools.getModelForLink(data.getModelLink(), getContext());
//                if (model != null) data.wasInstalled(model);
//                adapter.changed(data);
////                Toast.makeText(getContext(), "onDownloadCompleted", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onDownloadFailed() {
//                isDownloading = false;
//                progressBar.setVisibility(View.GONE);
//                Toast.makeText(getContext(), "Download failed for " + data.getFilename(), Toast.LENGTH_SHORT).show();
//
//                data.setDownloading(false);
//                adapter.changed(data);
//            }
//
//            @Override
//            public void onDownloadProgress(int progress) {
//                progressBar.setProgress(progress);
////                Toast.makeText(getContext(), "download started", Toast.LENGTH_SHORT).show();
//            }
//        };
        Communication.downloadModel(data.getModelLink(), requireContext());
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onState(DownloadState state) {
        switch (state.state) {
            case DOWNLOAD_STARTED:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case FINISHED:
                progressBar.setVisibility(View.GONE);
                break;
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStatus(Status status) {

    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadProgress(DownloadProgress progress) {
        progressBar.setProgress(progress.progress);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnzipProgress(UnzipProgress progress) {
        progressBar.setSecondaryProgress(progress.progress);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadError(DownloadError error) {
        Toast.makeText(getContext(), error.message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDeleteButtonClicked(View view, int position, ModelsAdapter.Data data) {
        Tools.deleteModel(data.getModel(), getContext());
        boolean removed = data.wasDeleted();
        if (removed) {
            adapter.removed(data);
        }
    }
}