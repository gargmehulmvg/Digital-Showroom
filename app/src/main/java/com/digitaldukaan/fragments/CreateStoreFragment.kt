package com.digitaldukaan.fragments

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.CustomDomainBottomSheetResponse
import com.digitaldukaan.services.CreateStoreService
import com.digitaldukaan.services.serviceinterface.ICreateStoreServiceInterface
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_create_store_fragment.*

class CreateStoreFragment : BaseFragment(), ICreateStoreServiceInterface {

    private var mService: CreateStoreService? = null

    companion object {
        private const val TAG = "CreateStoreFragment"
        fun newInstance(): CreateStoreFragment = CreateStoreFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_create_store_fragment, container, false)
        return mContentView
    }

    override fun onBackPressed(): Boolean {
        Log.d(TAG, "onBackPressed: do nothing")
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mService = CreateStoreService()
        mService?.setServiceInterface(this)
        mService?.getCustomDomainBottomSheetData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance()?.hideToolBar(mActivity, true)
        val progressAnimation: ObjectAnimator? = ObjectAnimator.ofInt(progressBarStyleHorizontal, "progress", 0, 25)
        progressAnimation?.apply {
            setAutoCancel(true)
            duration = Constants.STORE_CREATION_PROGRESS_ANIMATION_INTERVAL
        }
        progressAnimation?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                Log.d(TAG, "onAnimationRepeat: do nothing")
            }

            override fun onAnimationEnd(animation: Animator?) {
                try {
                    clearFragmentBackStack()
                } catch (e: Exception) {
                    Log.e(TAG, "onAnimationEnd: ${e.message}", e)
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
                Log.d(TAG, "onAnimationCancel: do nothing")
            }

            override fun onAnimationStart(animation: Animator?) {
                Log.d(TAG, "onAnimationStart: do nothing")
            }
        })
        progressAnimation?.start()
    }

    override fun onCreateStoreResponse(commonApiResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonApiResponse.mIsSuccessStatus) {
                val customDomainBottomSheetResponse = Gson().fromJson<CustomDomainBottomSheetResponse>(commonApiResponse.mCommonDataStr, CustomDomainBottomSheetResponse::class.java)
                StaticInstances.sCustomDomainBottomSheetResponse = customDomainBottomSheetResponse
                launchFragment(HomeFragment.newInstance(true), true)
            }
        }
    }

    override fun onCreateStoreServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

}