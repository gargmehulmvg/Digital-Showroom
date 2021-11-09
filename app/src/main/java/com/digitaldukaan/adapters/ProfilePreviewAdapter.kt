package com.digitaldukaan.adapters

import android.graphics.Rect
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.interfaces.IProfilePreviewItemClicked
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse
import com.digitaldukaan.models.response.StoreBusinessResponse
import java.util.*

class ProfilePreviewAdapter(
    private val mActivity: MainActivity,
    private var mSettingsKeysList: ArrayList<ProfilePreviewSettingsKeyResponse>,
    private val mProfilePreviewListener: IProfilePreviewItemClicked,
    private val mBusinessList: ArrayList<StoreBusinessResponse>?
) : RecyclerView.Adapter<ProfilePreviewAdapter.ProfilePreviewViewHolder>() {

    inner class ProfilePreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val settingKeyHeading: TextView = itemView.findViewById(R.id.settingKeyHeading)
        val gstValueTextView: TextView = itemView.findViewById(R.id.gstValueTextView)
        val reEnterGstValueTextView: TextView = itemView.findViewById(R.id.reEnterGstValueTextView)
        val gstKycVerificationGroup: View = itemView.findViewById(R.id.gstKycVerificationGroup)
        val addSettingKeyHeading: TextView = itemView.findViewById(R.id.addSettingKeyHeading)
        val profilePreviewContainer: View = itemView.findViewById(R.id.profilePreviewContainer)
        val profilePreviewDefaultScreenGroup: View = itemView.findViewById(R.id.profilePreviewDefaultScreenGroup)
        val addSettingKeyDataTextView: TextView = itemView.findViewById(R.id.addSettingKeyDataTextView)
        val profilePreviewBusinessTypeRecyclerView: RecyclerView = itemView.findViewById(R.id.profilePreviewBusinessTypeRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePreviewViewHolder {
        val view = ProfilePreviewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.profile_preview_item, parent, false))
        view.profilePreviewContainer.setOnClickListener{ mProfilePreviewListener.onProfilePreviewItemClicked(mSettingsKeysList[view.adapterPosition], view.adapterPosition + 1) }
        return view
    }

    override fun getItemCount(): Int = mSettingsKeysList.size

    override fun onBindViewHolder(holder: ProfilePreviewAdapter.ProfilePreviewViewHolder, position: Int) {
        val settingKeyItem = mSettingsKeysList[position]
        settingKeyItem.let { responseItem ->
            holder.apply {
                settingKeyHeading.text = responseItem.mHeadingText
                if (isEmpty(responseItem.mValue)) {
                    profilePreviewDefaultScreenGroup.visibility = View.VISIBLE
                    addSettingKeyDataTextView.visibility = View.GONE
                    addSettingKeyHeading.text = responseItem.mDefaultText
                    addSettingKeyHeading.setTextColor(ContextCompat.getColor(mActivity, R.color.open_green))
                } else {
                    profilePreviewDefaultScreenGroup.visibility = View.GONE
                    addSettingKeyDataTextView.visibility = View.VISIBLE
                    addSettingKeyDataTextView.text = responseItem.mValue
                    addSettingKeyHeading.setTextColor(ContextCompat.getColor(mActivity, R.color.black))
                }
                settingKeyItem.mIsEditable.let { isEditable ->
                    holder.profilePreviewContainer.isEnabled = isEditable
                    addSettingKeyDataTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, if (isEditable) R.drawable.ic_edit else 0, 0)
                }
                when (settingKeyItem.mAction) {
                    Constants.ACTION_STORE_LOCATION -> addSettingKeyDataTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location_color, 0, R.drawable.ic_edit, 0)
                    Constants.ACTION_EMAIL_AUTHENTICATION -> addSettingKeyDataTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_google_g, 0, R.drawable.ic_edit, 0)
                    Constants.ACTION_DOMAIN_SUCCESS -> addSettingKeyDataTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_round_green_arrow_small, 0)
                    Constants.ACTION_BUSINESS_TYPE -> {
                        profilePreviewBusinessTypeRecyclerView.visibility = View.VISIBLE
                        val layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false).apply {
                            reverseLayout = true
                            stackFromEnd = true
                        }
                        profilePreviewBusinessTypeRecyclerView.layoutManager = layoutManager
                        val profilePreviewBusinessTypeAdapter = ProfilePreviewBusinessTypeAdapter(mBusinessList)
                        profilePreviewBusinessTypeRecyclerView.apply {
                            adapter = profilePreviewBusinessTypeAdapter
                            addItemDecoration(OverlapDecoration())
                        }
                    }
                    Constants.ACTION_BANK_ACCOUNT -> {
                        StaticInstances.sBankDetails?.let { bankItem ->
                            val bankStr = "${bankItem.accountHolderName} \n" + "Account No. : ${bankItem.accountNumber} \n" + "IFSC code : ${bankItem.ifscCode}"
                            addSettingKeyDataTextView.text = bankStr
                        }
                    }
                    Constants.ACTION_GST_ADD -> {
                        addSettingKeyDataTextView.apply {
                            text = null
                            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0)
                        }
                        gstKycVerificationGroup.visibility = View.VISIBLE
                    }
                    Constants.ACTION_GST_PENDING -> {
                        gstKycVerificationGroup.visibility = View.VISIBLE
                        gstValueTextView.visibility = View.VISIBLE
                        addSettingKeyDataTextView.apply {
                            text = null
                            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0)
                        }
                        gstValueTextView.apply {
                            text = responseItem.mValue
                            background = ContextCompat.getDrawable(mActivity, R.drawable.curve_light_yellow_background)
                            setTextColor(ContextCompat.getColor(mActivity, R.color.gst_pending_text_color))
                        }
                    }
                    Constants.ACTION_GST_REJECTED -> {
                        gstKycVerificationGroup.visibility = View.VISIBLE
                        reEnterGstValueTextView.visibility = View.VISIBLE
                        reEnterGstValueTextView.text = Html.fromHtml("<u>Re-Enter OTP</u>", Html.FROM_HTML_MODE_COMPACT)
                        gstValueTextView.visibility = View.VISIBLE
                        addSettingKeyDataTextView.apply {
                            text = null
                            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0)
                        }
                        gstValueTextView.apply {
                            text = responseItem.mValue
                            background = ContextCompat.getDrawable(mActivity, R.drawable.curve_light_red_background)
                            setTextColor(ContextCompat.getColor(mActivity, R.color.red))
                        }
                    }
                    Constants.ACTION_GST_VERIFIED -> {
                        gstKycVerificationGroup.visibility = View.VISIBLE
                        addSettingKeyDataTextView.apply {
                            text = null
                            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0)
                        }
                        gstValueTextView.apply {
                            text = Html.fromHtml(responseItem.mValue, Html.FROM_HTML_MODE_COMPACT)
                        }
                    }
                }
            }
        }
    }

}

class OverlapDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount
        if (position == (itemCount - 1)) outRect.set(0, 0, 0, 0) else outRect.set(-25, 0, 0, 0)
    }
}