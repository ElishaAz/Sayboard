package com.elishaazaria.sayboard.settingsfragments.modelsfragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.elishaazaria.sayboard.R;

import java.net.URI;
import java.net.URISyntaxException;

public class AddVoskServerDialogFragment extends DialogFragment {

    private final Callback callback;

    public AddVoskServerDialogFragment(Callback callback) {

        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        dialogBuilder.setView(inflater.inflate(R.layout.add_vosk_server_dialog, null))
                // Add action buttons
                .setPositiveButton(R.string.dialog_add_vosk_server_add, (dialog, id) -> {
                    String hostname = ((EditText) getDialog().findViewById(R.id.hostname)).getText().toString();
                    String portString = ((EditText) getDialog().findViewById(R.id.port)).getText().toString();
                    Log.d("VoskServerDialog", hostname + ":" + portString);
                    try {
                        int port = Integer.parseInt(portString);
                        callback.callback(true, new URI(null, null, hostname, port, null, null, null));
                    } catch (NumberFormatException | URISyntaxException e) {
                        e.printStackTrace();
                        callback.callback(false, null);
                    }
                })
                .setNegativeButton(R.string.dialog_add_vosk_server_cancel, (dialog, id) -> dialog.cancel());
        return dialogBuilder.create();
    }

    public interface Callback {
        void callback(boolean add, URI uri);
    }
}
