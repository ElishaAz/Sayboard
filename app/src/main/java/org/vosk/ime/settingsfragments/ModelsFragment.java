package org.vosk.ime.settingsfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.vosk.ime.FileDownloadService;
import org.vosk.ime.Model;
import org.vosk.ime.ModelLink;
import org.vosk.ime.Tools;
import org.vosk.ime.databinding.FragmentModelsBinding;

import java.util.List;
import java.util.Locale;
import java.util.Map;

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