package xyz.sentrionic.harmony.di.main

import dagger.Module
import dagger.android.ContributesAndroidInjector
import xyz.sentrionic.harmony.ui.main.account.AccountFragment
import xyz.sentrionic.harmony.ui.main.account.ChangePasswordFragment
import xyz.sentrionic.harmony.ui.main.account.UpdateAccountFragment
import xyz.sentrionic.harmony.ui.main.create_story.CreateStoryFragment
import xyz.sentrionic.harmony.ui.main.profile.ProfileFragment
import xyz.sentrionic.harmony.ui.main.search.ProfileSearchFragment
import xyz.sentrionic.harmony.ui.main.search.SearchFragment
import xyz.sentrionic.harmony.ui.main.story.StoryFragment
import xyz.sentrionic.harmony.ui.main.story.ViewStoryFragment

@Module
abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeStoryFragment(): StoryFragment

    @ContributesAndroidInjector()
    abstract fun contributeAccountFragment(): AccountFragment

    @ContributesAndroidInjector()
    abstract fun contributeChangePasswordFragment(): ChangePasswordFragment

    @ContributesAndroidInjector()
    abstract fun contributeCreateStoryFragment(): CreateStoryFragment

    @ContributesAndroidInjector()
    abstract fun contributeViewStoryFragment(): ViewStoryFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateAccountFragment(): UpdateAccountFragment

    @ContributesAndroidInjector()
    abstract fun contributeSearchFragment(): SearchFragment

    @ContributesAndroidInjector()
    abstract fun contributeProfileSearchFragment(): ProfileSearchFragment

    @ContributesAndroidInjector()
    abstract fun contributeProfileFragment(): ProfileFragment
}