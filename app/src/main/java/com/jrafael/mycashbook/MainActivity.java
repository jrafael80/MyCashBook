package com.jrafael.mycashbook;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jrafael.mycashbook.dummy.DummyContent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends BaseActivity
        implements ResumenFragment.OnListFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        //TODO esto no queda muy elegante.
        setOnListenerGetResumen((ResumenFragment) mSectionsPagerAdapter.mFragments[0]);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tabLayout.getTabAt(1).select();

            }
        });

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 1) fab.hide();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) fab.show();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) MainActivity.this.getResultsFromApi();

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        getResultsFromApi();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class AppendRowFragment extends Fragment {
        public  EditText mIdEdit;
        public  EditText mContentEdit;
        public  EditText mDetailsEdit;

        MainActivity that;
        public AppendRowFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static AppendRowFragment newInstance(MainActivity that) {
            AppendRowFragment fragment = new AppendRowFragment();
            fragment.that = that;
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_append_row, container, false);
            mIdEdit = (EditText) view.findViewById(R.id.editId);
            mContentEdit = (EditText) view.findViewById(R.id.editContent);
            mDetailsEdit = (EditText) view.findViewById(R.id.editDetails);

            Button submit   = (Button) view.findViewById(R.id.button);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    that.appendRow(new Object[]{new SimpleDateFormat("dd/MM/yyyy kk:mm").format(new Date()), mIdEdit.getText().toString(), mContentEdit.getText().toString(), mDetailsEdit.getText().toString()});
                    Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });

            return view;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        Fragment[] mFragments = new Fragment[] {
                ResumenFragment.newInstance()
                , AppendRowFragment.newInstance(MainActivity.this)
        };
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return mFragments[position];
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return mFragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.label_resumen);
                case 1:
                    return getString(R.string.label_append_row);
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
