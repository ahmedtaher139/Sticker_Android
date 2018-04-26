package com.sticker_android.controller.fragment.fan.fanhome;


import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sticker_android.R;
import com.sticker_android.controller.fragment.base.BaseFragment;
import com.sticker_android.model.User;
import com.sticker_android.model.corporateproduct.Category;
import com.sticker_android.model.enums.DesignType;
import com.sticker_android.network.ApiCall;
import com.sticker_android.network.ApiResponse;
import com.sticker_android.network.RestClient;
import com.sticker_android.utils.ProgressDialogHandler;
import com.sticker_android.utils.Utils;
import com.sticker_android.utils.sharedpref.AppPref;
import com.sticker_android.view.BottomSheetFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;

/**
 * A simple {@link Fragment} subclass.
 */
public class FanHomeFragment extends BaseFragment implements SearchView.OnQueryTextListener {

    private TabLayout tabLayout;
    private AppPref appPref;
    private User mUserdata;
    private RelativeLayout rlFragmentContainer;
    private int index;
    private Call<ApiResponse> apiResponseCall;
    private SearchView searchView;
    private MenuItem item;
    private FragmentManager mFragmentManager;
    private MenuItem itemFilter;
    private ArrayList<Category> categoryList=new ArrayList<>();


    public FanHomeFragment() {
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
        View view = inflater.inflate(R.layout.fragment_fan_home, container, false);
        init();
        getuserInfo();
        setViewReferences(view);
        setViewListeners();
        mFragmentManager = getChildFragmentManager();
        addTabsDynamically();
        setSelectedTabColor();
        setBackground();
        tabLayout.setMinimumWidth(200);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setSelectedTabIndicatorColor(Color.TRANSPARENT);
        setHasOptionsMenu(true);
        replaceFragment(new FanFilter());
        fetchCategoryApi();
        return view;
    }


    private void fetchCategoryApi() {

        Call<ApiResponse> apiResponseCall = RestClient.getService().apiCorporateCategoryList(mUserdata.getLanguageId(), mUserdata.getAuthrizedKey()
                , mUserdata.getId(), "corporate_category");

        apiResponseCall.enqueue(new ApiCall(getActivity()) {
            @Override
            public void onSuccess(ApiResponse apiResponse) {
                if (apiResponse.status) {
                    categoryList = apiResponse.paylpad.corporateCategories;

                }

            }

            @Override
            public void onFail(Call<ApiResponse> call, Throwable t) {

            }
        });

    }

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
                if (itemFilter != null) {
                    itemFilter.setVisible(true);
                }

                switch (tab.getPosition()) {

                    case 0:
                        replaceFragment(new FanFilter());
                        break;
                    case 1:
                        if (itemFilter != null) {
                            itemFilter.setVisible(false);
                        }
                        replaceFragment(new FanContestFragment());
                        break;
                    case 2:
                        replaceFragment(new FanHomeStickerFragment());
                        break;
                    case 3:
                        replaceFragment(new FanHomeGifFragment());
                        break;
                    case 4:
                        replaceFragment(new FanHomeEmojiFragment());
                        break;
                    case 5:
                        replaceFragment(new FanHomeAdsFragment());
                        break;
                    case 6:
                        replaceFragment(new FanHomeProductsFragment());
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
        inflater.inflate(R.menu.fan_home_screen, menu);
        item = menu.findItem(R.id.search);
        itemFilter = menu.findItem(R.id.filter);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        //  setSearchTextColour(searchView);
        setSearchIcons(searchView);
        searchView.setOnQueryTextListener(this);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQueryHint("Search " + Utils.capitlizeText(getSelectedType()) + " by name");

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

    private void searchViewExpandListener(MenuItem item) {

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (itemFilter != null) {
                    itemFilter.setVisible(false);
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Write your code here
                if (itemFilter != null) {
                    itemFilter.setVisible(true);
                }
                Fragment fragment = getChildFragmentManager().findFragmentById(R.id.container_fan_home);
                if (fragment instanceof FanContestFragment) {
                    if (itemFilter != null)
                        itemFilter.setVisible(false);
                }
                return true;
            }
        });
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

    /**
     * Method is used to get the type of posted product     *
     *
     * @return rerurns the type
     */
    public String getSelectedType() {
        String type = DesignType.stickers.getType();
        if (tabLayout.getSelectedTabPosition() == 0) {
            type = DesignType.filter.getType();
        } else if (tabLayout.getSelectedTabPosition() == 1) {
            type = DesignType.contest.getType();
        } else if (tabLayout.getSelectedTabPosition() == 2) {
            type = DesignType.stickers.getType();
        } else if (tabLayout.getSelectedTabPosition() == 3) {
            type = DesignType.gif.getType().toUpperCase(Locale.US);
        } else if (tabLayout.getSelectedTabPosition() == 4) {
            type = DesignType.emoji.getType();
        } else if (tabLayout.getSelectedTabPosition() == 5) {
            type = DesignType.ads.getType();
        } else if (tabLayout.getSelectedTabPosition() == 6) {
            type = DesignType.products.getType();
        }
        return type;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:

                break;
            case R.id.filter:

                showBottomSheetDialogFragment();

                Toast.makeText(getActivity(), "Under development", Toast.LENGTH_SHORT).show();

                if (tabLayout.getSelectedTabPosition() == 0) {
                    Fragment fragment = getChildFragmentManager().findFragmentById(R.id.container_fan_home);
                    if (fragment instanceof FanHomeStickerFragment) {
                        Toast.makeText(getActivity(), "Under development", Toast.LENGTH_SHORT).show();
                        //
                        // ((FanHomeStickerFragment) fragment).searchData();
                    }


                }
                break;
        }
        return true;
    }

    public void showBottomSheetDialogFragment() {

        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(categoryList, new BottomSheetFragment.IFilter() {
            @Override
            public void selectedCategory(Category[] categories) {

            }
        }, getActivity());
        bottomSheetFragment.show(getChildFragmentManager(), "filter data");
    }

    private void setSelectedTabColor() {
        tabLayout.setTabTextColors(Color.parseColor("#77FFFFFF"), Color.WHITE);
    }

    private void setBackground() {
        tabLayout.setBackground(getResources().getDrawable(R.drawable.side_nav_fan));
    }

    public void addTabsDynamically() {


        TabLayout.Tab filterTab = tabLayout.newTab();
        filterTab.setText(R.string.txt_filter); // set the Text for the first Tab
        tabLayout.addTab(filterTab);

        TabLayout.Tab contestTab = tabLayout.newTab();
        contestTab.setText(R.string.txt_contest); // set the Text for the first Tab
        tabLayout.addTab(contestTab);


        TabLayout.Tab stickerTab = tabLayout.newTab();
        stickerTab.setText(getString(R.string.stickers)); // set the Text for the first Tab
        tabLayout.addTab(stickerTab);

        TabLayout.Tab gifTab = tabLayout.newTab();
        gifTab.setText(getString(R.string.gif)); // set the Text for the first Tab
        tabLayout.addTab(gifTab);

        TabLayout.Tab emojiTab = tabLayout.newTab();
        emojiTab.setText(getString(R.string.emoji)); // set the Text for the first Tab
        tabLayout.addTab(emojiTab);

        TabLayout.Tab AdsTab = tabLayout.newTab();
        AdsTab.setText(getString(R.string.txt_ads_frag)); // set the Text for the first Tab
        tabLayout.addTab(AdsTab);

        TabLayout.Tab productsTab = tabLayout.newTab();
        productsTab.setText(getString(R.string.txt_products_frag)); // set the Text for the first Tab
        tabLayout.addTab(productsTab);

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
    public boolean onQueryTextSubmit(String query) {
        //   Toast.makeText(getApplicationContext(),"wjcj",Toast.LENGTH_SHORT).show();
        if (query.isEmpty()) {
            Utils.showToast(getActivity(), "Search cannot be empty.");
        } else {
            searchResult(query);
        }
        searchView.setIconified(false);
        searchView.clearFocus();
        MenuItemCompat.collapseActionView(item);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void searchResult(String query) {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.container_fan_home);
        if (fragment instanceof FanHomeStickerFragment) {
            ((FanHomeStickerFragment) fragment).searchData(query.trim());
        }
        if (fragment instanceof FanHomeEmojiFragment) {
            ((FanHomeEmojiFragment) fragment).searchData(query.trim());
        }
        if (fragment instanceof FanHomeGifFragment) {
            ((FanHomeGifFragment) fragment).searchData(query.trim());
        }
        if (fragment instanceof FanHomeAdsFragment) {
            ((FanHomeAdsFragment) fragment).searchData(query.trim());
        }
        if (fragment instanceof FanHomeProductsFragment) {
            ((FanHomeProductsFragment) fragment).searchData(query.trim());
        }
        if (fragment instanceof FanContestFragment) {
            ((FanContestFragment) fragment).searchData(query.trim());
        }


    }
}