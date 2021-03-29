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
import com.digitaldukaan.constants.ToolBarManager
import kotlinx.android.synthetic.main.layout_create_store_fragment.*

class CreateStoreFragment : BaseFragment() {

    companion object {
        private const val TAG = "CreateStoreFragment"
        fun newInstance(): CreateStoreFragment {
            return CreateStoreFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_create_store_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        val progressAnimation: ObjectAnimator = ObjectAnimator.ofInt(progressBarStyleHorizontal, "progress", 0, 25)
        progressAnimation.apply {
            setAutoCancel(true)
            duration = Constants.STORE_CREATION_PROGRESS_ANIMATION_INTERVAL
        }
        progressAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                Log.d(TAG, "onAnimationRepeat: do nothing")
            }

            override fun onAnimationEnd(animation: Animator?) {
                clearFragmentBackStack()
                launchFragment(HomeFragment.newInstance(), true)
            }

            override fun onAnimationCancel(animation: Animator?) {
                Log.d(TAG, "onAnimationCancel: do nothing")
            }

            override fun onAnimationStart(animation: Animator?) {
                Log.d(TAG, "onAnimationStart: do nothing")
            }
        })
        progressAnimation.start()
    }

}