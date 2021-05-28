package com.example.chat_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.chat_app.fragments.ChatsFragment;
import com.example.chat_app.fragments.Contacts_Fragment;
import com.example.chat_app.fragments.Group_Fragment;
import com.example.chat_app.fragments.Request_Fragment;
import com.example.chat_app.fragments.Status_Fragment;


public class TabAccessAdaptor extends FragmentPagerAdapter {
    public TabAccessAdaptor( FragmentManager fm) {
        super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                ChatsFragment chatsFragment=new ChatsFragment();
                return chatsFragment;
            case 1:
                Group_Fragment group_fragment=new Group_Fragment();
                return group_fragment;
            case 2:
                Contacts_Fragment contacts_fragment=new Contacts_Fragment();
                return contacts_fragment;
            case 3:
                Status_Fragment requestsFragment = new Status_Fragment();
                return requestsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        //return super.getPageTitle(position);
        switch (position){
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Contacts";
            case 3:
                return "Status";
        }
        return null;
    }
}
