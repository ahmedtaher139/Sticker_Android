package com.sticker_android.controller.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sticker_android.R;
import com.sticker_android.controller.fragment.base.BaseFragment;
import com.sticker_android.model.User;
import com.sticker_android.network.ApiCall;
import com.sticker_android.network.ApiResponse;
import com.sticker_android.network.RestClient;
import com.sticker_android.utils.sharedpref.AppPref;

import retrofit2.Call;


public class TermsAndConditionFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView tvTerms;
    private AppPref appPref;
    private User user;
    final String mimeType = "text/html";
    final String encoding = "UTF-8";
    private WebView webView;
    ProgressBar pgr;
    public TermsAndConditionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TermsAndConditionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TermsAndConditionFragment newInstance(String param1, String param2) {
        TermsAndConditionFragment fragment = new TermsAndConditionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_terms_and_condition, container, false);
        init();
        setViewReferences(view);
        getuserData();
      //  setWebView();
        getTermsConditionData();
        return view;
    }

    private void setWebView() {

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClint(pgr));
        //webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
    }

    public static String getHtmlData(String bodyHTML) {
        String head = "<head><style>img{max-width: 100%; width:auto; height: auto;}iframe{max-width: 100%; width:auto; height: auto;}div{max-width: 100%; width:auto; height: auto;}table{max-width: 100%; width:auto; height: auto;}</style></head>";
        return "<html>" + head + "<body>" + bodyHTML + "</body></html>";
    }

    private void getuserData() {
        user = appPref.getUserInfo();
    }

    private void init() {
        appPref = new AppPref(getActivity());
        appPref.getUserInfo();

    }

    private void getTermsConditionData() {
        pgr.setVisibility(View.VISIBLE);
        Call<ApiResponse> apiResponseCall = RestClient.getService().apiGetContent("2");
        apiResponseCall.enqueue(new ApiCall(getActivity()) {
            @Override
            public void onSuccess(ApiResponse apiResponse) {
                pgr.setVisibility(View.GONE);
                if (apiResponse.status) {
                    //tvTerms.setText(apiResponse.paylpad.getData().getInfoText());
                    webView.loadDataWithBaseURL("", apiResponse.paylpad.getData().getInfoText(), mimeType, encoding, "");
                   // getHtmlData(apiResponse.paylpad.getData().getInfoText());
                }
            }

            @Override
            public void onFail(Call<ApiResponse> call, Throwable t) {
                pgr.setVisibility(View.GONE);
            }
        });
    }


    @Override
    protected void setViewListeners() {

    }

    @Override
    protected void setViewReferences(View view) {
        tvTerms = (TextView) view.findViewById(R.id.tv_terms_conditions);
        webView = (WebView) view.findViewById(R.id.webView);
        pgr  =(ProgressBar)view.findViewById(R.id.pgr);
    }

    @Override
    protected boolean isValidData() {
        return false;
    }



    public class MyWebViewClint extends WebViewClient {

        private ProgressBar llLoader;

        public MyWebViewClint(ProgressBar loader) {
            llLoader = loader;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (llLoader != null) llLoader.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (llLoader != null) llLoader.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (llLoader != null) llLoader.setVisibility(View.GONE);
        }

    }
}
