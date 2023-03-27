package com.elishaazaria.sayboard.settingsfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elishaazaria.sayboard.data.LocalModel;
import com.elishaazaria.sayboard.R;
import com.elishaazaria.sayboard.Tools;
import com.elishaazaria.sayboard.data.VoskServerData;
import com.elishaazaria.sayboard.databinding.FragmentModelsBinding;
import com.elishaazaria.sayboard.downloader.messages.DownloadError;
import com.elishaazaria.sayboard.downloader.messages.DownloadProgress;
import com.elishaazaria.sayboard.downloader.messages.DownloadState;
import com.elishaazaria.sayboard.downloader.messages.ModelInfo;
import com.elishaazaria.sayboard.downloader.messages.State;
import com.elishaazaria.sayboard.downloader.messages.Status;
import com.elishaazaria.sayboard.downloader.messages.StatusQuery;
import com.elishaazaria.sayboard.downloader.messages.UnzipProgress;
import com.elishaazaria.sayboard.preferences.ModelPreferences;
import com.elishaazaria.sayboard.settingsfragments.modelsfragment.AdapterDataProvider;
import com.elishaazaria.sayboard.settingsfragments.modelsfragment.AddVoskServerDialogFragment;
import com.elishaazaria.sayboard.settingsfragments.modelsfragment.ModelsAdapter;
import com.elishaazaria.sayboard.settingsfragments.modelsfragment.ModelsAdapterData;
import com.elishaazaria.sayboard.settingsfragments.modelsfragment.ModelsAdapterLocalData;
import com.elishaazaria.sayboard.settingsfragments.modelsfragment.ModelsAdapterServerData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ModelsFragment extends Fragment implements ModelsAdapter.ItemClickListener, MenuProvider {
    private static final String TAG = "ModelsFragment";

    private FragmentModelsBinding binding;
    private ModelsAdapter adapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentModelsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerView;
        progressBar = binding.progressBar;

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        AdapterDataProvider dataProvider = new AdapterDataProvider() {
            @Override
            public List<ModelsAdapterData> getData() {
                ArrayList<ModelsAdapterData> list = new ArrayList<>();
                if (ModelPreferences.VOSK_SERVER_ENABLED) {
                    for (VoskServerData data : ModelPreferences.getVoskServers()) {
                        list.add(new ModelsAdapterServerData(data));
                    }
                }
                list.addAll(Tools.getModelsData(getContext()));
                return list;
            }
        };

        adapter = new ModelsAdapter(getContext(), dataProvider);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        MenuHost activity = requireActivity();
        activity.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(View view, int position, ModelsAdapterData data) {
    }

    @Override
    public void onButtonClicked(View view, int position, ModelsAdapterData data) {
        data.buttonClicked(adapter, requireContext());
    }

    public ModelsAdapterLocalData getAdapterDataForModel(ModelInfo modelInfo) {
        for (int i = 0; i < adapter.size(); i++) {
            ModelsAdapterData data = adapter.getItem(i);
            if (data instanceof ModelsAdapterLocalData) {
                ModelsAdapterLocalData localData = (ModelsAdapterLocalData) data;
                if (localData.getFilename().equals(modelInfo.filename)) {
                    return localData;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onState(DownloadState state) {
        ModelsAdapterLocalData current = getAdapterDataForModel(state.info);
        switch (state.state) {
            case DOWNLOAD_STARTED:
                progressBar.setVisibility(View.VISIBLE);
                current.downloading();
                adapter.changed(current);
                break;
            case FINISHED:
                progressBar.setVisibility(View.GONE);
                LocalModel model = Tools.getModelForLink(current.getModelLink(), getContext());
                if (model != null) current.wasInstalled(model);
                adapter.changed(current);
                break;
            case ERROR:
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Download failed for " + current.getFilename(), Toast.LENGTH_SHORT).show();
                current.downloadCanceled();
                adapter.changed(current);
                break;

            case QUEUED:
                current.wasQueued();
                adapter.changed(current);
                break;
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStatus(Status status) {
        if (status.current == null) return;
        onState(new DownloadState(status.current, status.state));
        switch (status.state) {
            case DOWNLOAD_STARTED:
                onDownloadProgress(new DownloadProgress(status.current, status.downloadProgress));
                break;
            case UNZIP_STARTED:
                onUnzipProgress(new UnzipProgress(status.current, status.unzipProgress));
                break;
        }

        for (ModelInfo modelInfo : status.queued) {
            onState(new DownloadState(modelInfo, State.QUEUED));
        }
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
//        Toast.makeText(getContext(), error.message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new StatusQuery());
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        if (ModelPreferences.VOSK_SERVER_ENABLED) {
            menuInflater.inflate(R.menu.models_fragment_menu, menu);
        }
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (ModelPreferences.VOSK_SERVER_ENABLED) {
            if (menuItem.getTitle() == getString(R.string.menu_models_add_server)) {
                new AddVoskServerDialogFragment(new AddVoskServerDialogFragment.Callback() {
                    @Override
                    public void callback(boolean add, URI uri) {
                        if (add && uri != null) {
                            ModelPreferences.addToVoskServers(new VoskServerData(uri, null));
                            adapter.reload();
                        }
                    }
                }).show(requireActivity().getSupportFragmentManager(), "AddVoskServerDialogFragment");
            }
        }

        return false;
    }
}