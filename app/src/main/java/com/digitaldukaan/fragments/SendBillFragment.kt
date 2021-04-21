package com.digitaldukaan.fragments

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.constants.getBitmapFromUri
import kotlinx.android.synthetic.main.layout_send_bill.*

class SendBillFragment : BaseFragment() {

    private var mImageUri: Uri? = null

    companion object {
        private const val TAG = "SendBillFragment"
        fun newInstance(uri: Uri?): SendBillFragment {
            val fragment = SendBillFragment()
            fragment.mImageUri = uri
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_send_bill, container, false)
        hideBottomNavigationView(true)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, true)
        }
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadImageFromUri()
        sendBillEditText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(str: Editable?) {
                sendBillTextView.isEnabled = str?.toString()?.isEmpty() != true
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "beforeTextChanged: do nothing")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: do nothing")
            }

        })
    }

    private fun loadImageFromUri() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mImageUri?.let {
                val bitmap = getBitmapFromUri(mImageUri, mActivity)
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            refreshImageView.id -> openFullCamera()
            backButtonToolbar.id -> mActivity.onBackPressed()
        }
    }

    override fun onImageSelectionResultUri(fileUri: Uri?) {
        mImageUri = fileUri
        loadImageFromUri()
    }

}