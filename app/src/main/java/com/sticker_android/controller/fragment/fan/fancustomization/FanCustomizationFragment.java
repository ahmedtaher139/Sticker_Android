package com.sticker_android.controller.fragment.fan.fancustomization;


import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sticker_android.R;
import com.sticker_android.controller.fragment.base.BaseFragment;
import com.sticker_android.model.User;
import com.sticker_android.model.enums.DesignType;
import com.sticker_android.network.ApiResponse;
import com.sticker_android.utils.Utils;
import com.sticker_android.utils.sharedpref.AppPref;

import java.lang.reflect.Field;

import retrofit2.Call;

/**
 * A simple {@link Fragment} subclass.
 */
public class FanCustomizationFragment extends BaseFragment implements SearchView.OnQueryTextListener {


    private TabLayout tabLayout;
    private AppPref appPref;
    private User mUserdata;
    private RelativeLayout rlFragmentContainer;
    private int index;
    private Call<ApiResponse> apiResponseCall;
    private SearchView searchView;
    private MenuItem item;

    public FanCustomizationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fan_customization, container, false);
        init();
        getuserInfo();
        setViewReferences(view);
        setViewListeners();
        addTabsDynamically();
        setSelectedTabColor();
        setBackground();
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setSelectedTabIndicatorColor(Color.TRANSPARENT);
        setHasOptionsMenu(true);
        replaceFragment(new FanCustomizationStickersFragment());
        return view;
    }

    /*  @Override
      public void onPrepareOptionsMenu(Menu menu) {
          MenuItem item = menu.findItem(R.id.action_settings);
          item.setVisible(false);
      }


  */
    private void init() {
        appPref = new AppPref(getActivity());
    }

    private void getuserInfo() {
        mUserdata = appPref.getUserInfo();
    }


    @Override
    protected void setViewListeners() {

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (item != null)
                    MenuItemCompat.collapseActionView(item);
                switch (tab.getPosition()) {
                    case 0:
                        replaceFragment(new FanCustomizationStickersFragment());
                        break;
                    case 1:
                        replaceFragment(new FanCustomizationEmojiFragment());
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void setViewReferences(View view) {
        tabLayout = (TabLayout) view.findViewById(R.id.act_landing_tab);
        rlFragmentContainer = (RelativeLayout) view.findViewById(R.id.rlFragmentContainer);

    }

    @Override
    protected boolean isValidData() {
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.corporate_menu, menu);
        item = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        //  setSearchTextColour(searchView);
        setSearchIcons(searchView);
        searchView.setOnQueryTextListener(this);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        Configuration config = getResources().getConfiguration();
        final boolean isLeftToRight;
        isLeftToRight = config.getLayoutDirection() != View.LAYOUT_DIRECTION_RTL;
        if (!isLeftToRight) {
            View xIcon = ((ViewGroup) searchView.getChildAt(0)).getChildAt(2);
            xIcon.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  searchView.setQueryHint("Search " + Utils.capitlizeText(getSelectedType()) + " by name");
                setQueryHintText(searchView);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                return false;
            }
        });

        searchViewExpandListener(item);
    }
    private void setQueryHintText(SearchView searchView) {

        if (tabLayout.getSelectedTabPosition() == 0) {
            searchView.setQueryHint(getString(R.string.txt_search_stickers_by_name));

        } else if (tabLayout.getSelectedTabPosition() == 2) {
            //type = DesignType.gif.getType().toUpperCase(Locale.US);
            searchView.setQueryHint(getString(R.string.txt_search_gif_by_name));

        } else if (tabLayout.getSelectedTabPosition() == 1) {
            searchView.setQueryHint(getString(R.string.txt_search_emoji_by_name));
        }
    }
    /**
     * Method is used to get the type of posted product     *
     *
     * @return rerurns the type
     */
    public String getSelectedType() {
        String type = DesignType.stickers.getType();

        if (tabLayout.getSelectedTabPosition() == 0) {
            type = DesignType.stickers.getType();

        } else if (tabLayout.getSelectedTabPosition() == 1) {
            type = DesignType.emoji.getType();
        }
        return type;
    }

    private void setSelectedTabColor() {
        tabLayout.setTabTextColors(Color.parseColor("#77FFFFFF"), Color.WHITE);
    }

    private void setBackground() {
        tabLayout.setBackground(getResources().getDrawable(R.drawable.side_nav_fan));
    }

    public void addTabsDynamically() {

        TabLayout.Tab stickerTab = tabLayout.newTab();
        stickerTab.setText(getString(R.string.stickers)); // set the Text for the first Tab
        tabLayout.addTab(stickerTab);
        TabLayout.Tab emojiTab = tabLayout.newTab();
        emojiTab.setText(getString(R.string.emoji)); // set the Text for the first Tab
        tabLayout.addTab(emojiTab);
        Utils.setTabLayoutDivider(tabLayout, getActivity());
    }

    /**
     * replace existing fragment of container
     *
     * @param fragment
     */
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction =
                getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container_fan_home,
                fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:

                break;

        }
        return true;
    }


    private void searchViewExpandListener(MenuItem item) {

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Write your code here
                Fragment fragment = getChildFragmentManager().findFragmentById(R.id.container_fan_home);
                if (fragment instanceof FanCustomizationEmojiFragment) {
                    ((FanCustomizationEmojiFragment) fragment).refreshApi();
                } else if (fragment instanceof FanCustomizationStickersFragment)
                    ((FanCustomizationStickersFragment) fragment).refreshApi();

                return true;

            }
        });
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void setSearchIcons(SearchView searchView) {
        try {
            Field searchField = SearchView.class.getDeclaredField("mCloseButton");
            searchField.setAccessible(true);
            ImageView closeBtn = (ImageView) searchField.get(searchView);
            closeBtn.setImageResource(R.drawable.close_search);

        } catch (NoSuchFieldException e) {

        } catch (IllegalAccessException e) {
        }

    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        //   Toast.makeText(getApplicationContext(),"wjcj",Toast.LENGTH_SHORT).show();
        if (query.isEmpty()) {
            Utils.showToast(getActivity(), "Search cannot be empty.");
        } else {
            searchResult(query);
        }
        searchView.setIconified(false);
        searchView.clearFocus();
      //  MenuItemCompat.collapseActionView(item);

        return true;
    }

    private void searchResult(String query) {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.container_fan_home);
        if (fragment instanceof FanCustomizationStickersFragment) {
            ((FanCustomizationStickersFragment) fragment).filterData(query.trim());
        }
        if (fragment instanceof FanCustomizationEmojiFragment) {
            ((FanCustomizationEmojiFragment) fragment).filterData(query.trim());
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment f = getChildFragmentManager().findFragmentById(R.id.container_fan_home);

        if (resultCode == getActivity().RESULT_OK) {
            if(f instanceof FanCustomizationStickersFragment){
                ((FanCustomizationStickersFragment)f).onActivityResult(requestCode, resultCode, data);
            }
            if(f instanceof FanCustomizationEmojiFragment){
                ((FanCustomizationStickersFragment)f).onActivityResult(requestCode, resultCode, data);
            }
        }

    }


    public void closeSearch() {
        if(item!=null)
            MenuItemCompat.collapseActionView(item);
    }
}
