package com.sticker_android.controller.fragment.corporate.contest;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.sticker_android.controller.activities.corporate.home.CorporateHomeActivity;
import com.sticker_android.controller.adaptors.ContestOngoingListAdapter;
import com.sticker_android.controller.fragment.base.BaseFragment;
import com.sticker_android.model.User;
import com.sticker_android.model.corporateproduct.Product;
import com.sticker_android.model.interfaces.NetworkPopupEventListener;
import com.sticker_android.network.ApiCall;
import com.sticker_android.network.ApiResponse;
import com.sticker_android.network.RestClient;
import com.sticker_android.utils.Utils;
import com.sticker_android.utils.fragmentinterface.UpdateToolbarTitle;
import com.sticker_android.utils.sharedpref.AppPref;

import java.util.ArrayList;

import retrofit2.Call;

/**
 * A simple {@link Fragment} subclass.
 */
public class CorporateContestOngoingFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {


    private CorporateHomeActivity mHostActivity;

    private RecyclerView recOngoingContestCorp;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout llNoDataFound;
    private AppPref appPref;
    private User mUserdata;
    private TextView tvNoAdsUploaded;
    private LinearLayoutManager mLayoutManager;

    private Context mContext;
    private ContestOngoingListAdapter mAdapter;
    private RelativeLayout rlContent;
    private LinearLayout llLoaderView;
    private RelativeLayout rlConnectionContainer;
    private TextView txtNoDataFoundTitle, txtNoDataFoundContent;
    private ArrayList<Product> mProductList;
    private static final String TAG = CorporateContestOngoingFragment.class.getSimpleName();
    private View view;

    private UpdateToolbarTitle mUpdateToolbarCallback;

    public CorporateContestOngoingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_corporate_contest_ongoing, container, false);
        init();
        getuserInfo();
        setViewReferences(view);
        setViewListeners();
        mAdapter = new ContestOngoingListAdapter(getActivity());
        llNoDataFound.setVisibility(View.GONE);
        mProductList = new ArrayList<>();
        getContestApi();
        recOngoingContestCorp.setAdapter(mAdapter);
        recyclerViewLayout();
        mUpdateToolbarCallback.updateToolbarTitle(getString(R.string.txt_ongoing_contest));
        return view;
    }


    private void init() {

        appPref = new AppPref(getActivity());
    }

    private void getuserInfo() {
        mUserdata = appPref.getUserInfo();
    }


    /**
     * Method is used to set the layout on recycler view
     */
    private void recyclerViewLayout() {
        recOngoingContestCorp.hasFixedSize();

        mLayoutManager = new LinearLayoutManager(getContext());

        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recOngoingContestCorp.setLayoutManager(mLayoutManager);
    }


    @Override
    protected void setViewListeners() {
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected void setViewReferences(View view) {

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        recOngoingContestCorp = (RecyclerView) view.findViewById(R.id.recOngoingContestCorp);
        tvNoAdsUploaded = (TextView) view.findViewById(R.id.tvNoAdsUploaded);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mHostActivity = (CorporateHomeActivity) context;
        try {
            mUpdateToolbarCallback = (UpdateToolbarTitle) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement UpdateToolbarTitle");
        }
    }

    @Override
    public void onRefresh() {
        if (Utils.isConnectedToInternet(mHostActivity)) {
            getContestApi();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            Utils.showToastMessage(mHostActivity, getString(R.string.pls_check_ur_internet_connection));
        }

    }

    private void getContestApi() {
        swipeRefreshLayout.setRefreshing(true);
        Call<ApiResponse> apiResponseCall = RestClient.getService().getUserContestList(mUserdata.getLanguageId(), mUserdata.getAuthrizedKey(), mUserdata.getId(), "contest_list");
        apiResponseCall.enqueue(new ApiCall(getActivity()) {
            @Override
            public void onSuccess(ApiResponse apiResponse) {
                swipeRefreshLayout.setRefreshing(false);
                if (apiResponse.status) {
                    mAdapter.setData(apiResponse.paylpad.ongoingContests);
                if(apiResponse.paylpad.ongoingContests ==null){
                    llNoDataFound.setVisibility(View.VISIBLE);
                    showNoDataFound();
                    txtNoDataFoundContent.setText(R.string.txt_no_onging_contest_found);
                }else{
                    llNoDataFound.setVisibility(View.GONE);
                    rlContent.setVisibility(View.VISIBLE);
                }
                }
            }

            @Override
            public void onFail(Call<ApiResponse> call, Throwable t) {
                txtNoDataFoundContent.setVisibility(View.GONE);
                llNoDataFound.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (!call.isCanceled() && (t instanceof java.net.ConnectException ||
                        t instanceof java.net.SocketTimeoutException ||
                        t instanceof java.net.SocketException ||
                        t instanceof java.net.UnknownHostException)) {

                    mHostActivity.manageNoInternetConnectionLayout(mContext, rlConnectionContainer, new NetworkPopupEventListener() {
                            @Override
                            public void onOkClickListener(int reqCode) {
                                rlContent.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onRetryClickListener(int reqCode) {
                                getContestApi();
                            }
                        }, 0);
                    } else {
                        Utils.showToastMessage(mHostActivity, getString(R.string.pls_check_ur_internet_connection));
                    }
                }

        });
    }

    private void showNoDataFound() {
        llNoDataFound.setVisibility(View.VISIBLE);
        txtNoDataFoundTitle.setText("");
    }


}
