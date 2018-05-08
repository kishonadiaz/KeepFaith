package com.example.myapplication;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class Pager extends FragmentStatePagerAdapter {

    //integer to count number of tabs
    int tabCount;

    //Constructor to the class
    public Pager(FragmentManager fm, int tabCount) {
        super(fm);
        //Initializing tab count
        this.tabCount= tabCount;
    }

    //Overriding method getItem
    @Override
    public Fragment getItem(int position) {
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
                preferences tab4 = new preferences();
                return tab4;
            case 4:
                fav tab5 = new fav();
                return tab5;
            default:
                return null;
        }
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
