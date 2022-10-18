package com.elishaazaria.sayboard.settingsfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elishaazaria.sayboard.FileDownloadService;
import com.elishaazaria.sayboard.Model;
import com.elishaazaria.sayboard.Tools;
import com.elishaazaria.sayboard.databinding.FragmentModelsBinding;

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
        FileDownloadService.OnDownloadStatusListener listener = new FileDownloadService.OnDownloadStatusListener() {
            @Override
            public void onDownloadStarted() {
                progressBar.setVisibility(View.VISIBLE);
//                Toast.makeText(getContext(), "onDownloadStarted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadCompleted() {
                isDownloading = false;
                progressBar.setVisibility(View.GONE);
                Model model = Tools.getModelForLink(data.getModelLink(), getContext());
                if (model != null) data.wasInstalled(model);
                adapter.notifyItemChanged(position);
//                Toast.makeText(getContext(), "onDownloadCompleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadFailed() {
                isDownloading = false;
                progressBar.setVisibility(View.GONE);
//                Toast.makeText(getContext(), "onDownloadFailed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadProgress(int progress) {
                progressBar.setProgress(progress);
//                Toast.makeText(getContext(), "download started", Toast.LENGTH_SHORT).show();
            }
        };
        Tools.downloadModelFromLink(data.getModelLink(), listener, requireContext());
    }
}