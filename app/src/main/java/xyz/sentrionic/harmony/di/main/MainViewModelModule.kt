package xyz.sentrionic.harmony.di.main

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import xyz.sentrionic.harmony.di.ViewModelKey
import xyz.sentrionic.harmony.ui.main.account.AccountViewModel
import xyz.sentrionic.harmony.ui.main.create_story.CreateStoryViewModel
import xyz.sentrionic.harmony.ui.main.search.SearchViewModel
import xyz.sentrionic.harmony.ui.main.story.StoryViewModel

@Module
abstract class MainViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accoutViewModel: AccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StoryViewModel::class)
    abstract fun bindStoryViewModel(storyViewModel: StoryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchViewModel(searchViewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateStoryViewModel::class)
    abstract fun bindCreateStoryViewModel(createBlogViewModel: CreateStoryViewModel): ViewModel

}