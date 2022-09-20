package org.vosk.ime;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

public class Tools {

    public static boolean isMicrophonePermissionGranted(Activity activity) {
        int permissionCheck = ContextCompat.checkSelfPermission(activity.getApplicationContext(),
                Manifest.permission.RECORD_AUDIO);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isIMEEnabled(Activity activity) {
        InputMethodManager imeManager = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        for (InputMethodInfo i : imeManager.getEnabledInputMethodList()) {
            if (i.getPackageName().equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }
}
