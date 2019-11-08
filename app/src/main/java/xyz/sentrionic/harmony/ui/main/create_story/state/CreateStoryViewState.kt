package xyz.sentrionic.harmony.ui.main.create_story.state

import android.net.Uri

data class CreateStoryViewState(

    // CreateBlogFragment vars
    var storyFields: NewStoryFields = NewStoryFields()

)
{
    data class NewStoryFields(
        var newStoryCaption: String? = null,
        var newImageUri: Uri? = null
    )
}