package com.ishihata_tech.myna_card_demo.ui.common

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.ishihata_tech.myna_card_demo.R
import com.ishihata_tech.myna_card_demo.databinding.CommunicateDialogBinding
import com.ishihata_tech.myna_card_demo.myna.Reader
import java.io.IOException

class CommunicateDialogFragment : DialogFragment(), NfcAdapter.ReaderCallback {
    enum class State {
        WAIT_FOR_CARD, CONNECTING
    }

    interface OnConnectListener {
        fun onConnect(reader: Reader)
    }

    private lateinit var nfcAdapter: NfcAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var binding: CommunicateDialogBinding? = null

    private var state: State = State.WAIT_FOR_CARD
    set(value) {
        field = value
        when (value) {
            State.WAIT_FOR_CARD -> {
                binding?.textPleaseTouchCard?.visibility = View.VISIBLE
                binding?.textPleaseWait?.visibility = View.INVISIBLE
            }
            State.CONNECTING -> {
                binding?.textPleaseTouchCard?.visibility = View.INVISIBLE
                binding?.textPleaseWait?.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.also {
            state = State.valueOf(it.getString("state", State.WAIT_FOR_CARD.name))
        }
        isCancelable = false

        nfcAdapter = NfcAdapter.getDefaultAdapter(context)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("state", state.name)
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableReaderMode(
            activity,
            this,
            NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    override fun onPause() {
        try {
            nfcAdapter.disableReaderMode(activity)
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Failed to disable reader mode", e)
        }
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<CommunicateDialogBinding>(
            inflater,
            R.layout.communicate_dialog,
            container,
            false
        ).also {
            it.lifecycleOwner = this
        }

        // 状態を表示に反映させる
        state = state

        return binding!!.root
    }

    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) return

        val listener = parentFragment
        if (listener !is OnConnectListener) return

        handler.post {
            state = State.CONNECTING
        }

        val reader = Reader(tag)
        try {
            reader.connect()
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Failed to connect", e)
            handler.post {
                state = State.WAIT_FOR_CARD
            }
            return
        }
        listener.onConnect(reader)
        try {
            reader.close()
        } catch (e: IOException) {
            Log.d(LOG_TAG, "Failed to close reader", e)
        }

        handler.post {
            dismiss()
        }
    }

    companion object {
        private const val LOG_TAG = "CommunicateDialogFragment"
        private const val FRAGMENT_TAG = "CommunicateDialogFragment"

        fun show(fragmentManager: FragmentManager) {
            CommunicateDialogFragment().showNow(fragmentManager, FRAGMENT_TAG)
        }
    }
}