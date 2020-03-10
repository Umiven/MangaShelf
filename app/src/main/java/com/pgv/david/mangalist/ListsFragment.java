package com.pgv.david.mangalist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import java.util.ArrayList;
import java.util.List;

public class ListsFragment extends android.support.v4.app.Fragment {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;

    public ListsFragment() {}

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_lists, container, false);

        this.viewPager = rootView.findViewById(R.id.mangaListsPager);
        this.viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(new CompletedFragment(),"Completed");
        viewPagerAdapter.addFragment(new ReadingFragment(),"Reading");
        viewPagerAdapter.addFragment(new PlanToReadFragment(),"Plan to read");
        viewPager.setAdapter(viewPagerAdapter);

        this.tabLayout = rootView.findViewById(R.id.mangaListsTabs);
        tabLayout.setupWithViewPager(this.viewPager);

        return rootView;
    }
}

class ViewPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragmentList = new ArrayList<>();
    private List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String title) {
        this.mFragmentList.add(fragment);
        this.mFragmentTitleList.add(title);
    }

    @Override
    public Fragment getItem(int i) {
        return this.mFragmentList.get(i);
    }

    @Override
    public int getCount() {
        return this.mFragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return this.mFragmentTitleList.get(position);
    }
}