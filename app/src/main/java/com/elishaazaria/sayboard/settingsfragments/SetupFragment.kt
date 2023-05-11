package com.elishaazaria.sayboard.settingsfragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.SettingsActivity
import com.elishaazaria.sayboard.Tools.isIMEEnabled
import com.elishaazaria.sayboard.Tools.isMicrophonePermissionGranted
import com.elishaazaria.sayboard.databinding.FragmentSetupBinding

class SetupFragment : Fragment() {
    private var binding: FragmentSetupBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetupBinding.inflate(inflater, container, false)
        val root: View = binding!!.root
        registerForActivityResult<String, Boolean>(ActivityResultContracts.RequestPermission()) { isGranted: Boolean? -> reloadButtons() }
        binding!!.microphonePermission.setOnClickListener { v: View? ->
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ), PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        }
        binding!!.enableKeyboard.setOnClickListener { v: View? -> startActivity(Intent("android.settings.INPUT_METHOD_SETTINGS")) }

//        InputMethodManager imeManager = (InputMethodManager) requireActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//        binding.openImeSwitcher.setOnClickListener(v -> imeManager.showInputMethodPicker());
        reloadButtons()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun reloadButtons() {
        val permissionGranted = isMicrophonePermissionGranted(requireActivity())
        binding!!.microphonePermission.text =
            getString(if (permissionGranted) R.string.mic_permission_granted else R.string.mic_permission_not_granted)
        binding!!.microphonePermission.isEnabled = !permissionGranted
        val keyboardInEnabledList = isIMEEnabled(requireActivity())
        binding!!.enableKeyboard.text =
            getString(if (keyboardInEnabledList) R.string.keyboard_enabled else R.string.keyboard_not_enabled)
        binding!!.enableKeyboard.isEnabled = !keyboardInEnabledList
        if (permissionGranted && keyboardInEnabledList) {
            (requireActivity() as SettingsActivity).permissionsGranted()
        }
    }

    //    @Override
    //    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    //        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    //        reloadButtons();
    //    }
    override fun onResume() {
        super.onResume()
        reloadButtons()
    }

    companion object {
        /* Used to handle permission request */
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
    }
}