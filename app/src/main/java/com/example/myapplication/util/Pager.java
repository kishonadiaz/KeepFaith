package com.example.myapplication.util;


import android.app.Fragment;


import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.example.myapplication.fragments.fav;
import com.example.myapplication.fragments.preferences;
import com.example.myapplication.fragments.topPost;
import com.example.myapplication.fragments.userPost;
import com.example.myapplication.fragments.writePost;

import java.util.ArrayList;

public class Pager extends FragmentStatePagerAdapter {

    //integer to count number of tabs
    int tabCount;
    FragmentManager fragmentManager;
    private ArrayList<Fragment> fragmentArrayList = new ArrayList<>();

    //Constructor to the class
    public Pager(android.support.v4.app.FragmentManager fm, int tabCount) {
        super(fm);
        this.fragmentManager = fm;
        //Initializing tab count
        this.tabCount= tabCount;
    }

    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    //Overriding method getItem
    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        //Returning the current tabs
        switch (position) {
            case 0:
                userPost tab1 = new userPost();
                return tab1;
            case 1:
                topPost tab2 = new topPost();
                return tab2;
            case 2:
                writePost tab3 = new writePost();
                return tab3;
            case 3:
                fav tab4 = new fav();

                return tab4;
            case 4:
                preferences tab5 = new preferences();
                return tab5;
            default:
                return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    //Overriden method getCount to get the number of tabs
    @Override
    public int getCount() {
        return tabCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Users";
            case 1:
                return "Top Post";
            case 2:
                return "Post";
            case 3:
                return "Fav";
            case 4:
                return "Pref";
        }
        return null;
    }

}
