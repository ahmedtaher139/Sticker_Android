package com.sticker_android.controller.fragment.fan.fanhome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sticker_android.R;
import com.sticker_android.constant.AppConstant;
import com.sticker_android.controller.activities.fan.home.FanHomeActivity;
import com.sticker_android.controller.activities.fan.home.details.FanDetailsActivity;
import com.sticker_android.controller.adaptors.DesignListAdapter;
import com.sticker_android.controller.adaptors.FanListAdaptor;
import com.sticker_android.controller.fragment.base.BaseFragment;
import com.sticker_android.model.User;
import com.sticker_android.model.corporateproduct.Product;
import com.sticker_android.model.interfaces.MessageEventListener;
import com.sticker_android.model.interfaces.NetworkPopupEventListener;
import com.sticker_android.model.payload.Payload;
import com.sticker_android.network.ApiCall;
import com.sticker_android.network.ApiResponse;
import com.sticker_android.network.RestClient;
import com.sticker_android.utils.AppLogger;
import com.sticker_android.utils.Utils;
import com.sticker_android.utils.helper.PaginationScrollListener;
import com.sticker_android.utils.sharedpref.AppPref;
import com.sticker_android.view.EndlessRecyclerViewScrollListener;

import java.util.ArrayList;

import retrofit2.Call;

/**
 * Created by user on 19/4/18.
 */

public class FanHomeAdsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView rcDesignList;
    private LinearLayout llNoDataFound;
    private SwipeRefreshLayout swipeRefresh;
    private RelativeLayout rlContent;
    private LinearLayout llLoaderView;
    private RelativeLayout rlConnectionContainer;
    private TextView txtNoDataFoundTitle, txtNoDataFoundContent;

    private final String TAG = FanHomeStickerFragment.class.getSimpleName();
    private Context mContext;
    private FanHomeActivity mHostActivity;

    private View inflatedView;
    private LinearLayoutManager mLinearLayoutManager;
    private ArrayList<Product> mAdsList;
    private User mLoggedUser;

    private int mCurrentPage = 0;
    private int PAGE_LIMIT;
    private FanListAdaptor mAdapter;
    String filterData = "";
    private String categories = "";
    private String filterdata = "";
    private EndlessRecyclerViewScrollListener scrollListener2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fan_home_common, container, false);
        init();
        PAGE_LIMIT = getActivity().getResources().getInteger(R.integer.designed_item_page_limit);
        setViewReferences(view);
        setViewListeners();
        initRecyclerView();
        mAdapter = new FanListAdaptor(getActivity());
        rcDesignList.setAdapter(mAdapter);
        llNoDataFound.setVisibility(View.GONE);
        mAdsList = new ArrayList<>();
        mCurrentPage = 0;
        getAdsFromServer(false, "", "");
        //setRecScrollListener();
        setScrollListener();
        mAdapter.setOnProductClickListener(new DesignListAdapter.OnProductItemClickListener() {
            @Override
            public void onProductItemClick(Product product) {
                Intent intent = new Intent(getActivity(), FanDetailsActivity.class);
                intent.putExtra(AppConstant.PRODUCT, product);
               getActivity().startActivityForResult(intent, 333);
                getActivity().overridePendingTransition(R.anim.activity_animation_enter,
                        R.anim.activity_animation_exit);

            }
        });
        return view;
    }

    private void init() {
        mLoggedUser = new AppPref(getActivity()).getUserInfo();
    }


    @Override
    protected void setViewListeners() {
        swipeRefresh.setOnRefreshListener(this);
    }

    @Override
    protected void setViewReferences(View view) {

        rcDesignList = (RecyclerView) view.findViewById(R.id.rcItemList);
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        rlContent = (RelativeLayout) view.findViewById(R.id.rlContent);
        llNoDataFound = (LinearLayout) view.findViewById(R.id.llNoDataFound);
        txtNoDataFoundTitle = (TextView) view.findViewById(R.id.txtNoDataFoundTitle);
        txtNoDataFoundContent = (TextView) view.findViewById(R.id.txtNoDataFoundContent);
        rlConnectionContainer = (RelativeLayout) view.findViewById(R.id.rlConnectionContainer);
        llLoaderView = (LinearLayout) view.findViewById(R.id.llLoader);

    }

    @Override
    protected boolean isValidData() {
        return false;
    }


    private void initRecyclerView() {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        rcDesignList.setHasFixedSize(true);

        mLinearLayoutManager = new LinearLayoutManager(mContext);
        // use a linear layout manager
        rcDesignList.setLayoutManager(mLinearLayoutManager);
        //rcDesignList.addItemDecoration(new VerticalSpaceItemDecoration((int) getResources().getDimension(R.dimen.margin_5)));
        rcDesignList.setNestedScrollingEnabled(true);
    }

    public void searchData(String query) {

        mAdsList.clear();
        mAdapter.setData(mAdsList);
        mCurrentPage = 0;
        scrollListener2.resetState();
        getAdsFromServer(false, query, "");
    }

    private void showNoDataFound() {
        llNoDataFound.setVisibility(View.VISIBLE);
        txtNoDataFoundTitle.setText("");
    }

    @Override
    public void onRefresh() {
        mCurrentPage = 0;
        categories = "";
        if (Utils.isConnectedToInternet(mHostActivity)) {
            scrollListener2.resetState();
            getAdsFromServer(true, "", "");
        } else {
            swipeRefresh.setRefreshing(false);
            Utils.showToastMessage(mHostActivity, getString(R.string.pls_check_ur_internet_connection));
        }
        FanHomeFragment parentFrag = ((FanHomeFragment) FanHomeAdsFragment.this.getParentFragment());
        if (parentFrag != null)
            parentFrag.closeSearch();
    }


    private void setScrollListener() {

        scrollListener2 = new EndlessRecyclerViewScrollListener(mLinearLayoutManager) {
            @Override
            public int getFooterViewType(int defaultNoFooterViewType) {

                return 0;
            }

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                getAdsFromServer(false, "", categories);
                mAdapter.addLoader();
            }
        };
        // Adds the scroll listener to RecyclerView
        rcDesignList.addOnScrollListener(scrollListener2);
    }

    public void setRecScrollListener() {

        rcDesignList.addOnScrollListener(new PaginationScrollListener(mLinearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                AppLogger.debug(TAG, "Load more items");

                if (mAdsList.size() >= PAGE_LIMIT) {
                    AppLogger.debug(TAG, "page limit" + PAGE_LIMIT + " list size" + mAdsList.size());
                    getAdsFromServer(false, "", categories);
                    mAdapter.addLoader();
                }
            }

            @Override
            public int getTotalPageCount() {
                return 0;//not required
            }

            @Override
            public int getThresholdValue() {
                return PAGE_LIMIT / 2;
            }

            @Override
            public boolean isLastPage() {
                return false;
            }

            @Override
            public boolean isLoading() {
                return mAdapter.isLoaderVisible;
            }
        });
    }


    private void getAdsFromServer(final boolean isRefreshing, final String searchKeyword, final String categories) {

        //remove wi-fi symbol when response got
        if (rlConnectionContainer != null && rlConnectionContainer.getChildCount() > 0) {
            rlConnectionContainer.removeAllViews();
        }

        if (mCurrentPage == 0 && !isRefreshing) {
            llLoaderView.setVisibility(View.VISIBLE);
        }

        llNoDataFound.setVisibility(View.GONE);
        int index = 0;
        int limit = PAGE_LIMIT;

        if (isRefreshing) {
            index = 0;
        } else if (mCurrentPage != -1) {
            index = mCurrentPage * PAGE_LIMIT;
        }

        if (PAGE_LIMIT != -1) {
            limit = PAGE_LIMIT;
        }
        Call<ApiResponse> apiResponseCall = RestClient.getService().getFanHomeProductList(mLoggedUser.getLanguageId(), mLoggedUser.getAuthrizedKey(), mLoggedUser.getId(),
                index, limit, "ads", "all_product_list", searchKeyword, categories, filterData);

        /*Call<ApiResponse> apiResponseCall = RestClient.getService().getFanHomeProductList(mLoggedUser.getLanguageId(), mLoggedUser.getAuthrizedKey(), mLoggedUser.getId(),
                index, limit, DesignType.stickers.getType().toLowerCase(Locale.ENGLISH), "all_product_list", searchKeyword);
        */
        apiResponseCall.enqueue(new ApiCall(getActivity(), 1) {
            @Override
            public void onSuccess(ApiResponse apiResponse) {

                if (isAdded() && getActivity() != null) {
                    llLoaderView.setVisibility(View.GONE);
                    rlContent.setVisibility(View.VISIBLE);
                    swipeRefresh.setRefreshing(false);

                    //remove wi-fi symbol when response got
                    if (rlConnectionContainer != null && rlConnectionContainer.getChildCount() > 0) {
                        rlConnectionContainer.removeAllViews();
                    }

                    try {
                        if (apiResponse.status) {
                            Payload payload = apiResponse.paylpad;

                            if (payload != null) {

                                if (isRefreshing) {

                                    if (payload.productListAll != null && payload.productListAll.size() != 0) {
                                        mAdsList.clear();
                                        mAdsList.addAll(payload.productListAll);

                                        llNoDataFound.setVisibility(View.GONE);
                                        rcDesignList.setVisibility(View.VISIBLE);
                                        mAdapter.setData(mAdsList);

                                        mCurrentPage = 0;
                                        mCurrentPage++;
                                    } else {
                                        mAdsList.clear();
                                        mAdapter.setData(mAdsList);
                                        if (searchKeyword.length() != 0) {
                                            txtNoDataFoundContent.setText(getString(R.string.no_search_found));
                                        } else {
                                            txtNoDataFoundContent.setText(R.string.no_ads_uploaded_yet);
                                        }
                                        showNoDataFound();
                                    }
                                } else {

                                    if (mCurrentPage == 0) {
                                        mAdsList.clear();
                                        if (payload.productListAll != null) {
                                            mAdsList.addAll(payload.productListAll);
                                        }

                                        if (mAdsList.size() != 0) {
                                            llNoDataFound.setVisibility(View.GONE);
                                            rcDesignList.setVisibility(View.VISIBLE);
                                            mAdapter.setData(mAdsList);
                                        } else {
                                            showNoDataFound();
                                            if (searchKeyword.length() != 0) {
                                                txtNoDataFoundContent.setText(getString(R.string.no_search_found));
                                            } else {
                                                txtNoDataFoundContent.setText(R.string.no_ads_uploaded_yet);
                                            }
                                            rcDesignList.setVisibility(View.GONE);
                                        }
                                    } else {
                                        AppLogger.error(TAG, "Remove loader...");
                                        mAdapter.removeLoader();
                                        if (payload.productListAll != null && payload.productListAll.size() != 0) {
                                            mAdsList.addAll(payload.productListAll);
                                            mAdapter.setData(mAdsList);
                                        }
                                    }

                                    if (payload.productListAll != null && payload.productListAll.size() != 0) {
                                        mCurrentPage++;
                                    }
                                }
                                AppLogger.error(TAG, "item list size => " + mAdsList.size());

                            } else if (mAdsList == null || (mAdsList != null && mAdsList.size() == 0)) {
                                if (searchKeyword.length() != 0) {
                                    txtNoDataFoundContent.setText(getString(R.string.no_search_found));
                                } else {
                                    txtNoDataFoundContent.setText(R.string.no_ads_uploaded_yet);
                                }
                                showNoDataFound();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Utils.showAlertMessage(mContext, new MessageEventListener() {
                            @Override
                            public void onOkClickListener(int reqCode) {

                            }
                        }, getString(R.string.server_unreachable), getString(R.string.oops), 0);
                    }
                }

            }

            @Override
            public void onFail(final Call<ApiResponse> call, Throwable t) {

                if (isAdded() && getActivity() != null) {
                    llLoaderView.setVisibility(View.GONE);
                    mAdapter.removeLoader();
                    swipeRefresh.setRefreshing(false);

                    if (mCurrentPage == 0) {
                        rlContent.setVisibility(View.GONE);
                    } else {
                        rlContent.setVisibility(View.VISIBLE);
                    }
                    if (!call.isCanceled() && (t instanceof java.net.ConnectException ||
                            t instanceof java.net.SocketTimeoutException ||
                            t instanceof java.net.SocketException ||
                            t instanceof java.net.UnknownHostException)) {

                        if (mCurrentPage == 0) {
                            mHostActivity.manageNoInternetConnectionLayout(mContext, rlConnectionContainer, new NetworkPopupEventListener() {
                                @Override
                                public void onOkClickListener(int reqCode) {
                                    rlContent.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onRetryClickListener(int reqCode) {
                                    getAdsFromServer(isRefreshing, searchKeyword, categories);
                                }
                            }, 0);
                        } else {
                            Utils.showToastMessage(mHostActivity, getString(R.string.pls_check_ur_internet_connection));
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mHostActivity = (FanHomeActivity) context;
    }

    public void filterData(String categories, String filterdata) {
        this.categories = categories;
        mCurrentPage = 0;
        if (Utils.isConnectedToInternet(mHostActivity)) {
            mAdsList.clear();
            this.filterdata = filterdata;
            scrollListener2.resetState();
            getAdsFromServer(false, "", categories);
        } else {
            swipeRefresh.setRefreshing(false);
            Utils.showToastMessage(mHostActivity, getString(R.string.pls_check_ur_internet_connection));
        }

    }

    public void refreshApi() {
        mCurrentPage = 0;
        categories = "";
        if (Utils.isConnectedToInternet(mHostActivity)) {
            scrollListener2.resetState();
            getAdsFromServer(true, "", "");
        } else {
            swipeRefresh.setRefreshing(false);
            Utils.showToastMessage(mHostActivity, getString(R.string.pls_check_ur_internet_connection));
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==333){
            AppLogger.debug(TAG,"on activity result called +0 fragfme");
            refreshApi();
        }

    }
}
