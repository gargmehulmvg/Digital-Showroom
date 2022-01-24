package com.digitaldukaan.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
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
import com.digitaldukaan.models.dto.ImageFolder
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
        val list = getPicturePaths()
        Log.d(TAG, "fetchGalleryImages: ${list.size}")
        setupRecyclerView(list)
    }

    private fun setupRecyclerView(imagesList: ArrayList<ImageFolder>) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            imagesRecyclerView?.apply {
                Log.d(TAG, "setupRecyclerView: imagesList :: \n\n$imagesList")
                layoutManager = GridLayoutManager(mActivity, 3)
                adapter = MultiImageSelectionAdapter(imagesList, mActivity)
            }
        }
    }

    override fun onToolbarSideIconClicked() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            showToast("MultiImageSelection Done")
        }
    }

    private fun getPicturePaths(): ArrayList<ImageFolder> {
        val picFolders: ArrayList<ImageFolder> = ArrayList()
        val picPaths: ArrayList<String> = ArrayList()
        val allImagesUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID
        )
        val cursor: Cursor? = mActivity?.contentResolver?.query(allImagesUri, projection, null, null, null)
        try {
            cursor?.moveToFirst()
            do {
                val folds = ImageFolder()
                val name = cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                val folder = cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                val dataPath = cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                var folderpaths = dataPath?.substring(0, dataPath.lastIndexOf("$folder/"))
                folderpaths = "$folderpaths$folder/"
                if (!picPaths.contains(folderpaths)) {
                    picPaths.add(folderpaths)
                    folds.path = folderpaths
                    folds.folderName = folder
                    folds.firstPic = dataPath
                    folds.addpics()
                    picFolders.add(folds)
                } else {
                    for (i in 0 until picFolders.size) {
                        if (picFolders[i].path.equals(folderpaths)) {
                            picFolders[i].firstPic = dataPath
                            picFolders[i].addpics()
                        }
                    }
                }
            } while (true == cursor?.moveToNext())
            cursor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        for (i in 0 until picFolders.size) {
            Log.d(TAG, picFolders[i].folderName.toString() + " and path = " + picFolders[i].path + " " + picFolders[i].numberOfPics)
        }
        //reverse order ArrayList
        /* ArrayList<imageFolder> reverseFolders = new ArrayList<>();
        for(int i = picFolders.size()-1;i > reverseFolders.size()-1;i--){
            reverseFolders.add(picFolders.get(i));
        }*/
        return picFolders
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(ProfilePreviewFragment::class.simpleName, "onRequestPermissionResult")
        if (Constants.STORAGE_REQUEST_CODE == requestCode) {
            when {
                grantResults.isEmpty() -> Log.i(ProfilePreviewFragment::class.simpleName, "User interaction was cancelled.")
                PackageManager.PERMISSION_GRANTED == grantResults[0] -> {
                    val list = getPicturePaths()
                    Log.d(TAG, "fetchGalleryImages: ${list.size}")
                    setupRecyclerView(list)
                }
                else -> showShortSnackBar("Permission was denied")
            }
        }
    }

}