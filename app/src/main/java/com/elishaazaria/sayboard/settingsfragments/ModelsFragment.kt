package com.elishaazaria.sayboard.settingsfragments

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.Tools.getModelForLink
import com.elishaazaria.sayboard.Tools.getModelsData
import com.elishaazaria.sayboard.data.VoskServerData
import com.elishaazaria.sayboard.databinding.FragmentModelsBinding
import com.elishaazaria.sayboard.downloader.messages.*
import com.elishaazaria.sayboard.preferences.ModelPreferences
import com.elishaazaria.sayboard.settingsfragments.modelsfragment.*
import com.elishaazaria.sayboard.settingsfragments.modelsfragment.ModelsAdapter.ItemClickListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.URI

class ModelsFragment : Fragment(), ItemClickListener, MenuProvider {
    private lateinit var binding: FragmentModelsBinding
    private lateinit var adapter: ModelsAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentModelsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        recyclerView = binding.recyclerView
        progressBar = binding.progressBar
        recyclerView.layoutManager = LinearLayoutManager(context)
        val dataProvider: AdapterDataProvider = object : AdapterDataProvider {
            override val data: List<ModelsAdapterData>
                get() {
                    val list = ArrayList<ModelsAdapterData>()
                    if (ModelPreferences.VOSK_SERVER_ENABLED) {
                        for (data in ModelPreferences.voskServers) {
                            list.add(ModelsAdapterServerData(data))
                        }
                    }
                    list.addAll(getModelsData(context))
                    return list
                }
        }
        adapter = ModelsAdapter(requireContext(), dataProvider)
        adapter.setClickListener(this)
        recyclerView.adapter = adapter
        val activity: MenuHost = requireActivity()
        activity.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        return root
    }

    override fun onItemClick(view: View, position: Int, data: ModelsAdapterData) {}
    override fun onButtonClicked(view: View, position: Int, data: ModelsAdapterData) {
        data.buttonClicked(adapter, requireContext())
    }

    fun getAdapterDataForModel(modelInfo: ModelInfo): ModelsAdapterLocalData? {
        for (i in 0 until adapter.size()) {
            val data = adapter.getItem(i)
            if (data is ModelsAdapterLocalData) {
                val localData = data
                if (localData.filename == modelInfo.filename) {
                    return localData
                }
            }
        }
        return null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onState(state: DownloadState) {
        val current = getAdapterDataForModel(state.info) ?: return
        when (state.state) {
            State.DOWNLOAD_STARTED -> {
                progressBar.visibility = View.VISIBLE
                current.downloading()
                adapter.changed(current)
            }
            State.FINISHED -> {
                progressBar.visibility = View.GONE
                val model = getModelForLink(current.modelLink!!, context)
                if (model != null) current.wasInstalled(model)
                adapter.changed(current)
            }
            State.ERROR -> {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    context,
                    "Download failed for " + current.filename,
                    Toast.LENGTH_SHORT
                ).show()
                current.downloadCanceled()
                adapter.changed(current)
            }
            State.QUEUED -> {
                current.wasQueued()
                adapter.changed(current)
            }
            else -> {}
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onStatus(status: Status) {
        onState(DownloadState(status.current, status.state))
        when (status.state) {
            State.DOWNLOAD_STARTED -> onDownloadProgress(
                DownloadProgress(
                    status.current,
                    status.downloadProgress
                )
            )
            State.UNZIP_STARTED -> onUnzipProgress(
                UnzipProgress(
                    status.current,
                    status.unzipProgress
                )
            )
            else -> {}
        }
        for (modelInfo in status.queued) {
            onState(DownloadState(modelInfo, State.QUEUED))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadProgress(progress: DownloadProgress) {
        progressBar.progress = progress.progress
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnzipProgress(progress: UnzipProgress) {
        progressBar.secondaryProgress = progress.progress
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadError(error: DownloadError?) {
//        Toast.makeText(getContext(), error.message, Toast.LENGTH_SHORT).show();
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        EventBus.getDefault().post(StatusQuery())
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        if (ModelPreferences.VOSK_SERVER_ENABLED) {
            menuInflater.inflate(R.menu.models_fragment_menu, menu)
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (ModelPreferences.VOSK_SERVER_ENABLED) {
            if (menuItem.title === getString(R.string.menu_models_add_server)) {
                AddVoskServerDialogFragment { add: Boolean, uri: URI? ->
                    if (add && uri != null) {
                        ModelPreferences.addToVoskServers(VoskServerData(uri, null))
                        adapter.reload()
                    }
                }.show(requireActivity().supportFragmentManager, "AddVoskServerDialogFragment")
            }
        }
        return false
    }

    companion object {
        private const val TAG = "ModelsFragment"
    }
}