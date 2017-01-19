package com.motondon.tablayoutdemo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joca on 4/6/2016.
 *
 * !!!!!!!!!!!!!!!!!IMPORTANT!!!!!!!!!!!!!!!!!!!!!!
 * Since we are updating viewPager dynamically (i.e.: by adding and removing tabs), WE MUST EXTEND FragmentStatePagerAdapter AND NOT
 * FragmentPagerAdapter. Also we must override getItemPosition() and return "PagerAdapter.POSITION_NONE" in order to adapter to refresh
 * its data.
 *
 * From the Documentation:
 *   - When using FragmentPagerAdapter, the fragment of each page the user visits will be kept in memory,
 * though its view hierarchy may be destroyed when not visible.
 *
 *   - When using FragmentStatePagerAdapter, fragments will be destroyed when lose focus. On our case, when we remove a
 * fragment from the viewPager, it will also be destroy.
 *
 * So, if we use FragmentPagerAdapter, when we remove a tab, since it will never be destroyed, we will end up with inconsistency
 * when selecting a tab that is positioned after the one that was removed. It will show the previous fragment instead.
 *
 *  See links below for details:
 *   - http://stackoverflow.com/questions/34306476/dynamically-add-and-remove-tabs-in-tablayoutmaterial-design-android/34308112#34308112
 *   - http://stackoverflow.com/questions/10849552/update-viewpager-dynamically
 *   - http://stackoverflow.com/questions/35885840/how-to-clear-the-cached-memory-of-viewpager-with-tablayout
 *   - http://stackoverflow.com/questions/18747975/difference-between-fragmentpageradapter-and-fragmentstatepageradapter?noredirect=1&lq=1
 *
 * ----------------------------------------
 *
 * Another important question is about orientation change. Since we can add or remove a tab dynamically we need a way to make them available
 * after an orientation change. Note that we must use only those that were current available before the orientation change, including the ones
 * user might have created dynamically and avoid those user might have deleted. There are basically two approaches to achieve our goal:
 *
 *    1o) Before calling super.onSaveInstanceState() we will destroy all fragments (thanks to the FragmentStatePagerAdapter which will not keep them),
 *        and retain a list of the fragment classes name and titles. We need to keep the fully qualified class name since we will use reflection in order
 *        to reconstruct them. Then, recreate them on activity onCreate (using reflection) and if one of the items is an instance of GenericFragment,
 *        we need also to set its title, since it can be something like "Generic 1", "Generic 2" , etc (and not a const name).
 *
 *    2o) Save a list of current fragments on onSaveInstanceState() and add them back to the adapter on activity onCreate(). For this to work we need all
 *        fragments to be marked with setRetainInstance(true).
 *
 *    The difference from both approaches is that the first one will use more memory during orientation changes, but will be faster when adding
 *    them back to the adapter. On the other hand, the second approach will use much less memory, since we are storing only the fragments classes names and
 *    their titles, but it will take longer to recreate them (and also will be a little complicated to use).
 *
 *    Using one or another will depends on how many fragments a viewPager can contain. If we are dealing with a huge number of fragment, maybe
 *    the first one might not be a good choice. Overall, there are here so anyone can use that one better suits for the needs.
 *
 *    Note: These two approaches were taken from this great SO question:
 *    http://stackoverflow.com/questions/7951730/viewpager-and-fragments-whats-the-right-way-to-store-fragments-state?rq=1
 *
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private final static String TAG = ViewPagerAdapter.class.getSimpleName();

    private final List<Fragment> mTabItems = new ArrayList<>();
    private final List<String> mTabTitle = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mTabItems.get(position);
    }

    @Override
    public int getCount() {
        return mTabItems.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitle.get(position);
    }

    /**
     * This is called when notifyDataSetChanged() is called. Without this, getItem() is not triggered
     *
     * @param object
     * @return
     */
    @Override
    public int getItemPosition(Object object) {
        Log.d(TAG, "getItemPosition() - object: " + ((BaseFragment)object).getFragmentName());
        // refresh all fragments when data set changed
        return PagerAdapter.POSITION_NONE;
    }

    public void addTabPage(BaseFragment fragment) {
        Log.d(TAG, "addTabPage() - Adding fragment: " + fragment.getFragmentName());

        mTabItems.add(fragment);
        mTabTitle.add(fragment.getFragmentName());
        notifyDataSetChanged();
    }

    public void removeTabPage(int position) {
        if (!mTabItems.isEmpty() && position< mTabItems.size()) {
            Log.d(TAG, "removeTabPage() - Removing tab at position: " + position);

            mTabItems.remove(position);
            mTabTitle.remove(position);
            notifyDataSetChanged();
        }
    }

    /**
     * Used when ORIENTATION_CHANGE_METHOD flag is RETAIN_FRAGMENT
     *
     * @return
     */
    public List<Fragment> getPages() {
        return mTabItems;
    }

    /**
     * Used for both RETAIN_FRAGMENT and RECREATE_FRAGMENT orientation change  methods
     *
     * @param pages
     */
    public void setPages(List<Fragment> pages) {
        this.mTabItems.addAll(pages);
        for (Fragment fragment : pages) {
            mTabTitle.add(((BaseFragment)fragment).getFragmentName());
        }
        notifyDataSetChanged();
    }

    /**
     *  Used when ORIENTATION_CHANGE_METHOD flag is RECREATE_FRAGMENT
     *
     * @param supportFragmentManager
     */
    public void removeAllFragments(FragmentManager supportFragmentManager) {
        if ( mTabItems != null ) {
            for ( Fragment fragment : mTabItems ) {
                supportFragmentManager.beginTransaction().remove(fragment).commit();
            }
            mTabItems.clear();
            notifyDataSetChanged();
        }
    }

    /**
     * Used when ORIENTATION_CHANGE_METHOD flag is RECREATE_FRAGMENT
     *
     * @return
     */
    public List<String> getPagesClassName() {
        List<String> fragmentNames = new ArrayList<>();
        for (Fragment fragment : mTabItems) {
            fragmentNames.add(fragment.getClass().getName());

        }
        return fragmentNames;
    }

    /**
     * Used when ORIENTATION_CHANGE_METHOD flag is RECREATE_FRAGMENT
     *
     * @return
     */
    public List<String> getPageTitles() {
        return mTabTitle;
    }
}
