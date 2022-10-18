package com.elishaazaria.sayboard;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.elishaazaria.sayboard.R;

public class MainFragment extends Activity {

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private Button microphonePermission;
    private Button enableKeyboard;
    private Button openImeSwitcher;

    private InputMethodManager imeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);

        microphonePermission = findViewById(R.id.microphonePermission);
        enableKeyboard = findViewById(R.id.enableKeyboard);
        openImeSwitcher = findViewById(R.id.openImeSwitcher);

        imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);

        microphonePermission.setOnClickListener(v ->
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO));

        enableKeyboard.setOnClickListener(v ->
                startActivity(new Intent("android.settings.INPUT_METHOD_SETTINGS")));

        openImeSwitcher.setOnClickListener(v -> imeManager.showInputMethodPicker());

        reloadButtons();
    }

    private void reloadButtons() {
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);

        boolean permissionGranted = permissionCheck == PackageManager.PERMISSION_GRANTED;
        microphonePermission.setText(getString(permissionGranted ? R.string.mic_permission_granted : R.string.mic_permission_not_granted));
        microphonePermission.setEnabled(!permissionGranted);

        boolean keyboardInEnabledList = false;
        for (InputMethodInfo i :
                imeManager.getEnabledInputMethodList()) {
            if (i.getPackageName().equals(this.getPackageName())) {
                keyboardInEnabledList = true;
                break;
            }
        }

        enableKeyboard.setText(getString(keyboardInEnabledList? R.string.keyboard_enabled : R.string.keyboard_not_enabled));
        enableKeyboard.setEnabled(!keyboardInEnabledList);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        reloadButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadButtons();
    }
}