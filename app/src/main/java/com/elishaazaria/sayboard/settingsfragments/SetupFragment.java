package com.elishaazaria.sayboard.settingsfragments;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.elishaazaria.sayboard.R;
import com.elishaazaria.sayboard.SettingsActivity;
import com.elishaazaria.sayboard.Tools;
import com.elishaazaria.sayboard.databinding.FragmentSetupBinding;

public class SetupFragment extends Fragment {
    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private FragmentSetupBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSetupBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> reloadButtons());

        binding.microphonePermission.setOnClickListener(v -> {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        });

        binding.enableKeyboard.setOnClickListener(v ->
                startActivity(new Intent("android.settings.INPUT_METHOD_SETTINGS")));

//        InputMethodManager imeManager = (InputMethodManager) requireActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//        binding.openImeSwitcher.setOnClickListener(v -> imeManager.showInputMethodPicker());

        reloadButtons();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void reloadButtons() {
        boolean permissionGranted = Tools.isMicrophonePermissionGranted(requireActivity());
        binding.microphonePermission.setText(getString(permissionGranted ? R.string.mic_permission_granted : R.string.mic_permission_not_granted));
        binding.microphonePermission.setEnabled(!permissionGranted);

        boolean keyboardInEnabledList = Tools.isIMEEnabled(requireActivity());

        binding.enableKeyboard.setText(getString(keyboardInEnabledList ? R.string.keyboard_enabled : R.string.keyboard_not_enabled));
        binding.enableKeyboard.setEnabled(!keyboardInEnabledList);

        if (permissionGranted && keyboardInEnabledList) {
            ((SettingsActivity) requireActivity()).permissionsGranted();
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        reloadButtons();
//    }

    @Override
    public void onResume() {
        super.onResume();
        reloadButtons();
    }
}