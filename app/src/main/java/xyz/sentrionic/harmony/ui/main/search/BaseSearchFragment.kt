package xyz.sentrionic.harmony.ui.main.search

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.ui.main.story.BaseStoryFragment
import xyz.sentrionic.harmony.ui.main.story.StoryViewModel

abstract class BaseSearchFragment : BaseStoryFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.searchFragment, activity as AppCompatActivity)

        viewModel = activity?.run {
            ViewModelProvider(this, providerFactory).get(StoryViewModel::class.java)
        }?: throw Exception("Invalid Activity")

        cancelActiveJobs()
    }

}
