package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class AddressFieldsPageInfoResponse (
    @SerializedName("address_fields_list")      var addressFieldsList: ArrayList<AddressFieldsItemResponse?>?,
    @SerializedName("static_text")              var staticText: AddressFieldsStaticTextResponse?
)

data class AddressFieldsStaticTextResponse(
    @SerializedName("heading_page")             var heading_page: String?,
    @SerializedName("text_yes")                 var text_yes: String?,
    @SerializedName("text_no")                  var text_no: String?,
    @SerializedName("text_confirm_back")        var text_confirm_back: String?,
    @SerializedName("error_select_1_field")     var error_select_1_field: String?,
    @SerializedName("error_field_is_mandatory") var error_field_is_mandatory: String?,
    @SerializedName("text_save_changes")        var text_save_changes: String?
)

data class AddressFieldsItemResponse(
    @SerializedName("id")                       var id: Int,
    @SerializedName("heading")                  var heading: String?,
    @SerializedName("sub_heading")              var subHeading: String?,
    @SerializedName("text_mandatory")           var textMandatory: String?,
    @SerializedName("is_mandatory_enabled")     var isMandatoryEnabled: Boolean,
    @SerializedName("is_mandatory")             var isMandatory: Boolean,
    @SerializedName("is_field_selected")        var isFieldSelected: Boolean,
    @SerializedName("is_field_enabled")         var isFieldEnabled: Boolean
)