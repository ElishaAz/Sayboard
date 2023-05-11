package com.elishaazaria.sayboard.settingsfragments.modelsfragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.elishaazaria.sayboard.R
import java.net.URI
import java.net.URISyntaxException

class AddVoskServerDialogFragment(private val callback: Callback) : DialogFragment() {
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(requireActivity())
        // Get the layout inflater
        val inflater = requireActivity().layoutInflater

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        dialogBuilder.setView(
            inflater.inflate(
                R.layout.add_vosk_server_dialog,
                null
            )
        ) // Add action buttons
            .setPositiveButton(R.string.dialog_add_vosk_server_add) { dialog: DialogInterface?, id: Int ->
                val hostname =
                    (getDialog()!!.findViewById<View>(R.id.hostname) as EditText).text.toString()
                val portString =
                    (getDialog()!!.findViewById<View>(R.id.port) as EditText).text.toString()
                Log.d("VoskServerDialog", "$hostname:$portString")
                try {
                    val port = portString.toInt()
                    callback.callback(true, URI(null, null, hostname, port, null, null, null))
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    callback.callback(false, null)
                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                    callback.callback(false, null)
                }
            }
            .setNegativeButton(R.string.dialog_add_vosk_server_cancel) { dialog: DialogInterface, id: Int -> dialog.cancel() }
        return dialogBuilder.create()
    }

    fun interface Callback {
        fun callback(add: Boolean, uri: URI?)
    }
}