package com.digitaldukaan.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.MultiImageSelectionAdapter
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.layout_multi_image_selection.*


class MultiImageSelectionFragment: BaseFragment(), IOnToolbarIconClick {

    private var mImageSelectionCount = 0

    companion object {
        fun newInstance(count: Int): MultiImageSelectionFragment {
            val fragment = MultiImageSelectionFragment()
            fragment.mImageSelectionCount = count
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "MultiImageSelectionFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_multi_image_selection, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let {
            ToolBarManager.getInstance().apply {
                hideToolBar(mActivity, false)
                headerTitle = ""
                onBackPressed(this@MultiImageSelectionFragment)
                setSideIconVisibility(true)
                setSideIcon(ContextCompat.getDrawable(it, R.drawable.ic_white_tick), this@MultiImageSelectionFragment)
            }
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.STORAGE_REQUEST_CODE)
                return
            }
        }
        val list = fetchGalleryImages()
        Log.d(TAG, "fetchGalleryImages: ${list.size}")
        setupRecyclerView(list)
    }

    private fun setupRecyclerView(imagesList: ArrayList<String>) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            imagesRecyclerView?.apply {
                Log.d(TAG, "setupRecyclerView: imagesList :: \n\n$imagesList")
                layoutManager = GridLayoutManager(mActivity, 2)
                adapter = MultiImageSelectionAdapter(imagesList, mActivity)
            }
        }
    }

    override fun onToolbarSideIconClicked() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            showToast("MultiImageSelection Done")
        }
    }

    private fun fetchGalleryImages(): ArrayList<String> {
        try {
            mActivity?.let { context ->
                val columns = arrayOf(
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media._ID
                )
                val orderBy = MediaStore.Images.Media.DATE_TAKEN //order data by date
                val imageCursor: Cursor = context.managedQuery(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                    null, "$orderBy DESC"
                )
                val galleryImageUrls: ArrayList<String> = ArrayList()
                for (i in 0 until imageCursor.count) {
                    imageCursor.moveToPosition(i)
                    val dataColumnIndex: Int = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA) //get column index
                    galleryImageUrls.add(imageCursor.getString(dataColumnIndex)) //get Image from column index
                }
                return galleryImageUrls
            }
            return ArrayList()
        } catch (e: Exception) {
            return ArrayList()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(ProfilePreviewFragment::class.simpleName, "onRequestPermissionResult")
        if (Constants.STORAGE_REQUEST_CODE == requestCode) {
            when {
                grantResults.isEmpty() -> Log.i(ProfilePreviewFragment::class.simpleName, "User interaction was cancelled.")
                PackageManager.PERMISSION_GRANTED == grantResults[0] -> {
                    val list = fetchGalleryImages()
                    Log.d(TAG, "fetchGalleryImages: ${list.size}")
                    setupRecyclerView(list)
                }
                else -> showShortSnackBar("Permission was denied")
            }
        }
    }

}