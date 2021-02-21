package com.digitaldukaan.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.transition.TransitionInflater
import com.digitaldukaan.R
import kotlinx.android.synthetic.main.on_board_screen_dukaan_fragment.*


class OnBoardScreenDukaanNameFragment : BaseFragment() {

    private val mDukaanNameStaticData = mStaticData.mStaticData.mOnBoardStep1StaticData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.on_board_screen_dukaan_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dukaanNameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
             val str = s.toString()
             nextTextView.isEnabled = str.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        setupUIFromStaticData()
    }

    private fun setupUIFromStaticData() {
        stepOneTextView.text = mDukaanNameStaticData.mStepCount
        enterDukaanNameHeading.text = mDukaanNameStaticData.mDukaanName
        dukaanNameEditText.hint = mDukaanNameStaticData.mTitleHinText
        nextTextView.text = mDukaanNameStaticData.mNextButton
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            backImageView.id -> {
                mActivity.onBackPressed()
            }
            nextTextView.id -> {
                val dukanName = dukaanNameEditText.text
                if (dukanName.isEmpty()) {
                    dukaanNameEditText.requestFocus()
                    dukaanNameEditText.showKeyboard()
                    dukaanNameEditText.error = getString(R.string.mandatory_field_message)
                } else {
                    launchFragment(OnBoardScreenDukaanLocationFragment(), true)
                }
            }
        }
    }

}