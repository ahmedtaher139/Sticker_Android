package com.sticker_android.controller.fragment.fan.fandownloads;

import android.content.Context;
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
import com.sticker_android.controller.activities.fan.home.FanHomeActivity;
import com.sticker_android.controller.adaptors.FanDownloadListAdaptor;
import com.sticker_android.controller.fragment.base.BaseFragment;
import com.sticker_android.controller.fragment.fan.fanhome.FanHomeStickerFragment;
import com.sticker_android.model.User;
import com.sticker_android.model.contest.FanContestDownload;
import com.sticker_android.model.enums.DesignType;
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

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;

/**
 * Created by user on 24/4/18.
 */

public class FanDownloadStickerFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

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
    private ArrayList<FanContestDownload> mStickerList;
    private User mLoggedUser;

    private int mCurrentPage = 0;
    private int PAGE_LIMIT;
    private FanDownloadListAdaptor mAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_design_item_list, container, false);
        init();
        PAGE_LIMIT = getActivity().getResources().getInteger(R.integer.designed_item_page_limit);
        setViewReferences(view);
        setViewListeners();
        initRecyclerView();
        mAdapter = new FanDownloadListAdaptor(getActivity());

        rcDesignList.setAdapter(mAdapter);
        llNoDataFound.setVisibility(View.GONE);
        mStickerList = new ArrayList<>();
        mCurrentPage = 0;
        getDesignFromServer(false, "");
        setRecScrollListener();
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

    public void filterData(String query) {

        mStickerList.clear();
        mAdapter.setData(mStickerList);
        mCurrentPage = 0;
        getDesignFromServer(false, query);
    }

    private void showNoDataFound() {
        llNoDataFound.setVisibility(View.VISIBLE);
        txtNoDataFoundTitle.setText("");
    }

    @Override
    public void onRefresh() {
        mCurrentPage=0;
        if (Utils.isConnectedToInternet(mHostActivity)) {
            getDesignFromServer(true, "");
        } else {
            swipeRefresh.setRefreshing(false);
            Utils.showToastMessage(mHostActivity, getString(R.string.pls_check_ur_internet_connection));
        }
        FanDownloadFragment parentFrag = ((FanDownloadFragment) FanDownloadStickerFragment.this.getParentFragment());
        if(parentFrag!=null)
            parentFrag.closeSearch();
    }

    public void setRecScrollListener() {

        rcDesignList.addOnScrollListener(new PaginationScrollListener(mLinearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                AppLogger.debug(TAG, "Load more items");

                if (mStickerList.size() >= PAGE_LIMIT) {
                    AppLogger.debug(TAG, "page limit" + PAGE_LIMIT + " list size" + mStickerList.size());
                    getDesignFromServer(false, "");
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


    private void getDesignFromServer(final boolean isRefreshing, final String searchKeyword) {
     //   Toast.makeText(getActivity(), "inside defigner", Toast.LENGTH_SHORT).show();

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
        Call<ApiResponse> apiResponseCall = RestClient.getService().getFanDownloads(mLoggedUser.getLanguageId(), mLoggedUser.getAuthrizedKey(), mLoggedUser.getId(),
                index, limit, DesignType.stickers.getType().toLowerCase(Locale.ENGLISH), "fan_download_list", searchKeyword);

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

                                    if (payload.fanDownloadList != null && payload.fanDownloadList.size() != 0) {
                                        mStickerList.clear();
                                        mStickerList.addAll(payload.fanDownloadList);
                                        llNoDataFound.setVisibility(View.GONE);
                                        rcDesignList.setVisibility(View.VISIBLE);
                                        mAdapter.setData(mStickerList);

                                        mCurrentPage = 0;
                                        mCurrentPage++;
                                    } else {
                                        mStickerList.clear();
                                        mAdapter.setData(mStickerList);
                                        if (searchKeyword.length() != 0) {
                                            txtNoDataFoundContent.setText(getString(R.string.no_search_found));
                                        } else {
                                            txtNoDataFoundContent.setText(R.string.txt_no_downloaded_stickers_found);
                                        }
                                        showNoDataFound();
                                    }
                                } else {

                                    if (mCurrentPage == 0) {
                                        mStickerList.clear();
                                        if (payload.fanDownloadList != null) {
                                            mStickerList.addAll(payload.fanDownloadList);
                                        }

                                        if (mStickerList.size() != 0) {
                                            llNoDataFound.setVisibility(View.GONE);
                                            rcDesignList.setVisibility(View.VISIBLE);
                                            mAdapter.setData(mStickerList);
                                        } else {
                                            showNoDataFound();
                                            if (searchKeyword.length() != 0) {
                                                txtNoDataFoundContent.setText(getString(R.string.no_search_found));
                                            } else {
                                                txtNoDataFoundContent.setText(R.string.txt_no_downloaded_stickers_found);
                                            }
                                            rcDesignList.setVisibility(View.GONE);
                                        }
                                    } else {
                                        AppLogger.error(TAG, "Remove loader...");
                                        mAdapter.removeLoader();
                                        if (payload.fanDownloadList != null && payload.fanDownloadList.size() != 0) {
                                            mStickerList.addAll(payload.fanDownloadList);
                                            mAdapter.setData(mStickerList);
                                        }
                                    }

                                    if (payload.fanDownloadList != null && payload.fanDownloadList.size() != 0) {
                                        mCurrentPage++;
                                    }
                                }
                                AppLogger.error(TAG, "item list size => " + mStickerList.size());

                            } else if (mStickerList == null || (mStickerList != null && mStickerList.size() == 0)) {
                                if (searchKeyword.length() != 0) {
                                    txtNoDataFoundContent.setText(getString(R.string.no_search_found));
                                } else {
                                    txtNoDataFoundContent.setText(R.string.txt_no_downloaded_stickers_found);
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
                                    getDesignFromServer(isRefreshing, searchKeyword);
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

    public void refreshApi() {
        mCurrentPage=0;
        if (Utils.isConnectedToInternet(mHostActivity)) {
            getDesignFromServer(true, "");
        } else {
            swipeRefresh.setRefreshing(false);
            Utils.showToastMessage(mHostActivity, getString(R.string.pls_check_ur_internet_connection));
        }
    }
}
