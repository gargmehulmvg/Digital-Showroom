package com.digitaldukaan.adapters

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.interfaces.IProfilePreviewItemClicked
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse

class ProfilePreviewAdapter(
    private val mActivity: MainActivity,
    private var mSettingsKeysList: ArrayList<ProfilePreviewSettingsKeyResponse>,
    private val mProfilePreviewListener: IProfilePreviewItemClicked
) :
    RecyclerView.Adapter<ProfilePreviewAdapter.ProfilePreviewViewHolder>() {

    inner class ProfilePreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val settingKeyHeading: TextView = itemView.findViewById(R.id.settingKeyHeading)
        val addSettingKeyHeading: TextView = itemView.findViewById(R.id.addSettingKeyHeading)
        val profilePreviewContainer: View = itemView.findViewById(R.id.profilePreviewContainer)
        val profilePreviewDefaultScreenGroup: View = itemView.findViewById(R.id.profilePreviewDefaultScreenGroup)
        val profilePreviewDataGroup: View = itemView.findViewById(R.id.profilePreviewDataGroup)
        val addSettingKeyDataTextView: TextView = itemView.findViewById(R.id.addSettingKeyDataTextView)
        val profilePreviewBusinessTypeRecyclerView: RecyclerView = itemView.findViewById(R.id.profilePreviewBusinessTypeRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePreviewViewHolder {
        val view = ProfilePreviewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.profile_preview_item, parent, false))
        view.profilePreviewContainer.setOnClickListener{ mProfilePreviewListener.onProfilePreviewItemClicked(mSettingsKeysList[view.adapterPosition], view.adapterPosition + 1) }
        return view
    }

    override fun getItemCount(): Int = mSettingsKeysList.size

    override fun onBindViewHolder(
        holder: ProfilePreviewAdapter.ProfilePreviewViewHolder,
        position: Int
    ) {
        val settingKeyItem = mSettingsKeysList[position]
        settingKeyItem.run {
            holder.apply {
                settingKeyHeading.text = mHeadingText
                if (mValue?.isEmpty() != false) {
                    profilePreviewDefaultScreenGroup.visibility = View.VISIBLE
                    profilePreviewDataGroup.visibility = View.GONE
                    addSettingKeyHeading.text = mDefaultText
                } else {
                    profilePreviewDefaultScreenGroup.visibility = View.GONE
                    profilePreviewDataGroup.visibility = View.VISIBLE
                    addSettingKeyDataTextView.text = mValue
                }
                if (settingKeyItem.mAction == Constants.ACTION_STORE_LOCATION) {
                    addSettingKeyDataTextView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_location_color,
                        0,
                        R.drawable.ic_edit,
                        0
                    )
                } else if (settingKeyItem.mAction == Constants.ACTION_BUSINESS_TYPE) {
                    profilePreviewBusinessTypeRecyclerView.visibility = View.VISIBLE
                    val layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false).apply {
                        reverseLayout = true
                        stackFromEnd = true
                    }
                    profilePreviewBusinessTypeRecyclerView.layoutManager = layoutManager
                    val profilePreviewBusinessTypeAdapter = ProfilePreviewBusinessTypeAdapter(mActivity)
                    profilePreviewBusinessTypeRecyclerView.apply {
                        adapter = profilePreviewBusinessTypeAdapter
                        addItemDecoration(OverlapDecoration())
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