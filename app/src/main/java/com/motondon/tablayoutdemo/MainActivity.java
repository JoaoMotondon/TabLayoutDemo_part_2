package com.motondon.tablayoutdemo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.motondon.tablayoutdemo_part_2.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This project is intended to demonstrate how to use Material Design TabLayout.
 *
 * It creates some pre-defined fragments and adds them to a viewPager. Also it demonstrates how to add/remove tabs
 * dynamically
 *
 * It also shows how to handle orientation change in order to keep new tabs that might be created by the user and
 * skip those that were deleted. For this to happen we implemented two different approaches:
 *
 *   1) Hold a reference for all existent fragment on onSaveInstanceState method and add them back to the adapter on
 *      onCreate() method.
 *
 *   2) Remove all existent fragments prior to change rotate (on onSaveInstanceState), then hold a list of all fragments
 *      class names which will be used on onCreate() to recreate them by using reflection.
 *
 * For both approaches, all the hard work are done on onSaveInstanceState() and onCreate() methods. Also there are a couple
 * of helper methods in the ViewPagerAdapter class.
 *
 * See ViewPagerAdapter class scope comments for details.
 *
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Used when ORIENTATION_CHANGE_METHOD flag is RETAIN_FRAGMENT. See ViewPagerAdapter class scope comments for details.
    private static final String CURRENT_PAGES = "CURRENT_PAGES";

    // Used when ORIENTATION_CHANGE_METHOD flag is RECREATE_FRAGMENT. See ViewPagerAdapter class scope comments for details.
    private static final String CURRENT_PAGES_CLASS_NAMES = "CURRENT_PAGES_CLASS_NAMES";
    private static final String CURRENT_PAGES_TITLES = "CURRENT_PAGES_TITLES";

    // This enum is used to decide what orientation method we will use.
    public enum OrientationChangeMethod {
        RECREATE_FRAGMENT,
        RETAIN_FRAGMENT,
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Change this attribute in order to test both orientation change methods.
    //
    ///////////////////////////////////////////////////////////////////////////
    public static final OrientationChangeMethod ORIENTATION_CHANGE_METHOD = OrientationChangeMethod.RETAIN_FRAGMENT;

    // This app limits to create up to four new tabs. These attributes are used to control it.
    private static final int MAX_NUMBER_OF_NEW_TABS = 4;
    private static int genericFragmentCount = 0;

    @BindView(R.id.tabanim_toolbar) Toolbar toolbar;
    @BindView(R.id.tabanim_tabs) TabLayout tabLayout;
    @BindView(R.id.tabanim_viewpager) ViewPager viewPager;
    
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        if (savedInstanceState == null) {

            // For this example we will just add two fragments on it.
            viewPagerAdapter.addTabPage(new ChatFragment());
            viewPagerAdapter.addTabPage(new WalkFragment());

            // And set the first one as selected.
            TabLayout.Tab tab = tabLayout.getTabAt(0);
            tab.select();

        } else {

            // After a screen rotate, we check which orientation change method is configured. Depends on the method, we will retrieve specifics attributes
            // from the bundle.
            if (ORIENTATION_CHANGE_METHOD == OrientationChangeMethod.RETAIN_FRAGMENT) {

                // If using RETAIN_FRAGMENT which as the name implies will retaining all current fragments, all we need is to add them back to the adapter.
                // See ViewPagerAdapter class scope comments for details.
                List<Fragment> currentPages = (List<Fragment>) savedInstanceState.getSerializable(CURRENT_PAGES);
                if (currentPages.size() > 0) {
                    viewPagerAdapter.setPages(currentPages);
                }
            } else {

                // When using RECREATE_FRAGMENT, since we removed all fragments before change the orientation, we need now to reconstruct them.  Note that for the
                // fragments that were created dynamically we need also an extra step in oder to set their titles which might be something like  "Generic 1", "Generic 2".
                // See ViewPagerAdapter class scope comments for details.
                List<String> currentPagesClassNames = (List<String>) savedInstanceState.getSerializable(CURRENT_PAGES_CLASS_NAMES);
                List<String> currentPagesTitles = (List<String>) savedInstanceState.getSerializable(CURRENT_PAGES_TITLES);

                if (currentPagesClassNames.size() > 0) {

                    // This is the list used later to add the fragments to the adapter
                    List<Fragment> fragmentList = new ArrayList<>();

                    // For each existent fragment prior the orientation change, we will create a new instance of it by using reflection. This is why we stored the
                    // fully qualified class name. Note that if something wrong happens during the class creation, we are just printing the stack. In a real application we should
                    // account for that in a better way.
                    for (int x = 0; x < currentPagesClassNames.size(); x++) {

                        try {
                            String className = currentPagesClassNames.get(x);
                            String title = currentPagesTitles.get(x);

                            Class cls = Class.forName(className);
                            Object obj = cls.newInstance();
                            if (obj instanceof GenericFragment) {
                                ((GenericFragment) obj).setFragmentName(title);
                            }
                            fragmentList.add((Fragment) obj);

                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    // Finally add all just created fragments to the adapter.
                    viewPagerAdapter.setPages(fragmentList);
                }
            }
        }

        // If there is at least one tab, re-create long-click listener to it
        if (tabLayout.getChildCount() > 0) {
            LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);

            for (int i = 0; i < tabStrip.getChildCount(); i++) {
                // re-create the long-click listener
                addMenuLongClickListener(i);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if (ORIENTATION_CHANGE_METHOD == OrientationChangeMethod.RETAIN_FRAGMENT) {

            // When using RETAIN_FRAGMENT method, ust store a list of all current fragment in order to be able to add them back to the adapter after
            // the orientation change. See ViewPagerAdapter class scope comments for details.
            savedInstanceState.putSerializable(CURRENT_PAGES, (Serializable) viewPagerAdapter.getPages());

        } else {

            // When using RECREATE_FRAGMENT method, which will recreate all the current fragments instead of retain them we need to store the full fragment classes names
            // since we will  use reflection on onCreate() method. See ViewPagerAdapter class scope comments for details.
            savedInstanceState.putSerializable(CURRENT_PAGES_CLASS_NAMES, (Serializable) viewPagerAdapter.getPagesClassName());
            // Note that if user created a new fragment dynamically its title will be something like "Generic 1", "Generic 2", so we need to also keep a list of all
            // fragments title in order to be able to reconstruct them accordingly.
            savedInstanceState.putSerializable(CURRENT_PAGES_TITLES, (Serializable) viewPagerAdapter.getPageTitles());
            // Now, after save all needed information, remove all fragments from the adapter. On this approach, they will be recreated later on onCreate() method.
            viewPagerAdapter.removeAllFragments(getSupportFragmentManager());
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected() - itemId: " + item.getItemId());

        switch (item.getItemId()) {
            case R.id.menu_add_new_tab:
                Log.d(TAG, "onOptionsItemSelected() - menu_add_new_tab");
                addTab();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * As the name implies, this method created a long-click listener for the given tabIndex
     *
     * @param tabIndex
     */
    private void addMenuLongClickListener(final int tabIndex) {
        if (tabLayout.getChildCount() == 0) return;

        LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);

        View child = tabStrip.getChildAt(tabIndex);
        // First clean up any listener that might be setup previously
        child.setOnLongClickListener(null);

        child.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_remove_tab:
                                showDeleteDialog(tabIndex);
                                return true;
                            default:
                                return true;
                        }
                    }
                });
                popupMenu.getMenu().add(1, R.id.menu_remove_tab, 1, "Remove");
                popupMenu.show();

                return true;
            }
        });
    }

    /**
     * If user chooses to remove a tab, shows a confirm dialog. If user confirms to remove it, delegates it to the deleteTab() method.
     *
     * @param tabIndex
     */
    private void showDeleteDialog(final int tabIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this tab?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "showDeleteDialog::setPositiveButton() - Deleting tab index: " + tabIndex + "...");
                deleteTab(tabIndex);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    /**
     * This method will add a tab dynamically. Note it will take care of create instances of the GenericFragment class as well as
     * mount the fragment title in order to show the number of the fragment. This makes easy to confirm fragments are being
     * retained/recreated after a screen rotate accordingly.
     *
     * Note we are limiting in 4 new tabs. If you want more, just change MAX_NUMBER_OF_NEW_TABS attribute.
     *
     */
    private void addTab() {
        Log.d(TAG, "addTab()");

        // Lets add some boundaries here. Not really complex, but just to avoid flood of tabs.
        if (genericFragmentCount >= MAX_NUMBER_OF_NEW_TABS) {
            Toast.makeText(getApplicationContext(), "Max number of fragments reached.", Toast.LENGTH_SHORT).show();
            return;
        }

        genericFragmentCount++;

        // When adding a new tab, we will use a special fragment class called GenericFragment.
        // Right after creating an instance of it, call its mountFragmentName() method which will change its
        // name to something like "Generic Fragment 1", "Generic Fragment 2", etc. Then, add it to the viewPagerAdapter
        GenericFragment genericFragment = new GenericFragment();
        genericFragment.mountFragmentName(genericFragmentCount);
        viewPagerAdapter.addTabPage(genericFragment);

        // Also re-create long-click listener for all tabs, including the just created one.
        // TODO: Here we could improve a little, by instead of re-create long-click listener for all tabs, just create for the new one.
        if (tabLayout.getChildCount() > 0) {
            LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);

            for (int i = 0; i < tabStrip.getChildCount(); i++) {
                // re-create the long-click listener
                addMenuLongClickListener(i);
            }
        }

        Log.d(TAG, "addTab() - Tab: " + genericFragment.getFragmentName() + " was added successfully");
        Toast.makeText(getApplicationContext(), "Tab: " + genericFragment.getFragmentName() + " was added successfully", Toast.LENGTH_SHORT).show();
    }

    /**
     * Remove a tab from the adapter and recreate all long-click listeners.
     *
     * @param tabIndex
     */
    private void deleteTab(int tabIndex) {
        Log.d(TAG, "deleteTab() - tabIndex: " + tabIndex);

        // Delete a tab from the adapter based on its index
        viewPagerAdapter.removeTabPage(tabIndex);

        // After delete a tab, we need to re-create long-click listener for all tabs.
        // TODO: Here we could improve a little, by instead of re-create long-click listener for all tabs, just create for the new one.
        if (tabLayout.getChildCount() > 0) {
            LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);

            for (int i = 0; i < tabStrip.getChildCount(); i++) {
                // re-create the long-click listener
                addMenuLongClickListener(i);
            }
        }
    }
}
