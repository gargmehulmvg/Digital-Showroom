package com.digitaldukaan.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.digitaldukaan.R
import kotlinx.android.synthetic.main.on_board_screen_dukaan_fragment.backImageView
import kotlinx.android.synthetic.main.on_board_screen_dukaan_fragment.nextTextView
import kotlinx.android.synthetic.main.on_board_screen_dukaan_location_fragment.*


class OnBoardScreenDukaanLocationFragment : BaseFragment(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView =
            inflater.inflate(R.layout.on_board_screen_dukaan_location_fragment, container, false)
        mNavController = findNavController()
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        backImageView.setOnClickListener(this)
        dukaanLocationEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val str = s.toString()
                nextTextView.isEnabled = str.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            backImageView.id -> {
                mNavController.navigateUp()
            }
            nextTextView.id -> {
                val dukanName = dukaanLocationEditText.text
                if (dukanName.isEmpty()) {
                    dukaanLocationEditText.requestFocus()
                    dukaanLocationEditText.showKeyboard()
                    dukaanLocationEditText.error = getString(R.string.mandatory_field_message)
                }
            }
        }
    }

}