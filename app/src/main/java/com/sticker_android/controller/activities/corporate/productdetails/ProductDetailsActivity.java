package com.sticker_android.controller.activities.corporate.productdetails;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sticker_android.R;
import com.sticker_android.constant.AppConstant;
import com.sticker_android.controller.activities.base.AppBaseActivity;
import com.sticker_android.controller.activities.common.comments.CommentsActivity;
import com.sticker_android.controller.activities.corporate.RenewAdandProductActivity;
import com.sticker_android.model.User;
import com.sticker_android.model.corporateproduct.Product;
import com.sticker_android.model.enums.ProductStatus;
import com.sticker_android.network.ApiCall;
import com.sticker_android.network.ApiResponse;
import com.sticker_android.network.RestClient;
import com.sticker_android.utils.ProgressDialogHandler;
import com.sticker_android.utils.Utils;
import com.sticker_android.utils.helper.TimeUtility;
import com.sticker_android.utils.sharedpref.AppPref;

import retrofit2.Call;

public class ProductDetailsActivity extends AppBaseActivity {

    private Toolbar toolbar;
    private AppPref appPref;
    private User mUserData;
    private Product productObj;

    public ImageView imvOfAds;
    public TextView tvProductTitle, tvStatus, tvDesciption, tvTime;
    public CheckBox checkboxLike, checkboxShare;
    public ImageButton imvBtnEditRemove;

    TimeUtility timeUtility = new TimeUtility();
    private ProgressBar pgrImage;
    private TextView tvFeatured;
    private RelativeLayout rlJustificationHolder;
    private TextView txtViewMoreComment, txtRecentComments, edtJustification, txtYourComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        init();
        getProductData();
        setSupportActionBar(toolbar);
        setToolbarBackground();
        setViewReferences();
        setViewListeners();
        setProductDetails();
        toolbar.setNavigationIcon(R.drawable.back_arrow_small);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        measureImageWidthHeight();
        changeStatusBarColor(getResources().getColor(R.color.colorstatusBarCorporate));
        if (productObj != null)
            manageStatus();
    }


    private void manageStatus() {


        if (productObj.productStatus == 3) {
            rlJustificationHolder.setVisibility(View.VISIBLE);
            edtJustification.setVisibility(View.GONE);
            txtYourComment.setVisibility(View.GONE);
            setAdminCommentData();
        } else {
            rlJustificationHolder.setVisibility(View.GONE);
        }
    }

    private void setAdminCommentData() {
        if (productObj.rejectionList != null) {
            if (productObj.rejectionList.size() > 0) {

                txtRecentComments.setText("" + productObj.rejectionList.get(productObj.rejectionList.size() - 1).description);
            }
            if (productObj.rejectionList.size() > 1) {
                txtViewMoreComment.setVisibility(View.VISIBLE);
                txtViewMoreComment.setTextColor(getResources().getColor(R.color.colorCorporateText));
            } else {
                txtViewMoreComment.setVisibility(View.GONE);

            }
        }
    }

    private void measureImageWidthHeight() {

        ViewTreeObserver vto = imvOfAds.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                imvOfAds.getViewTreeObserver().removeOnPreDrawListener(this);
                int finalWidth = imvOfAds.getMeasuredWidth();
                int height = finalWidth * 3 / 5;
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imvOfAds.getLayoutParams();

                //      LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imvOfAds.getLayoutParams();
                layoutParams.height = height;
                imvOfAds.setLayoutParams(layoutParams);
                return true;
            }
        });
    }


    private void setProductDetails() {

        checkboxLike.setText(Utils.format(productObj.statics.likeCount));
        checkboxShare.setText(Utils.format(productObj.statics.shareCount));

        int status = productObj.productStatus;
        if (status == ProductStatus.REJECTED.getStatus()) {
            tvStatus.setTextColor(Color.RED);
            tvStatus.setText(R.string.rejected);
        } else if (status == ProductStatus.EXPIRED.getStatus()) {
            tvStatus.setTextColor(Color.RED);
            tvStatus.setText(R.string.expired);
        } else if (status == ProductStatus.APPROVED.getStatus()) {
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.colorHomeGreen));
            tvStatus.setText(R.string.approved);
        } else {
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.colorCorporateText));
            tvStatus.setText(R.string.pending);
        }

        if (productObj.isFeatured > 0) {
            tvFeatured.setVisibility(View.VISIBLE);
        } else {
            tvFeatured.setVisibility(View.GONE);

        }
       /* String status = "Ongoing";
        if (productObj.getIsExpired() > 0) {
            tvStatus.setTextColor(Color.RED);
            status = "Expired";{"status":false,"authrized_status":"1
        } else {
            tvStatus.setTextColor(getResources().getColor(R.color.colorHomeGreen));

        }*/

        if (productObj.statics.likeCount > 0) {
            checkboxLike.setChecked(true);
            checkboxLike.setButtonDrawable(getResources().getDrawable(R.drawable.ic_hand));
        } else {
            checkboxLike.setChecked(false);
            checkboxLike.setButtonDrawable(getResources().getDrawable(R.drawable.ic_like));

        }
        //  tvStatus.setText(status);
        imvBtnEditRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v);
            }
        });
        tvProductTitle.setText(productObj.getProductname());
        tvDesciption.setText(productObj.getDescription());
        tvTime.setText(timeUtility.covertTimeToText(Utils.convertToCurrentTimeZone(productObj.getCreatedTime()), getActivity()));
        pgrImage.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(productObj.getImagePath()).fitCenter()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        pgrImage.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imvOfAds);


    }

    /**
     * Method is used to set the toolbar background
     */
    private void setToolbarBackground() {
        toolbar.setBackground(getResources().getDrawable(R.drawable.corporate_header_hdpi));
    }


    private void init() {
        appPref = new AppPref(this);
        mUserData = appPref.getUserInfo();
    }

    private void setToolBarTitle(String type) {
        TextView textView = (TextView) toolbar.findViewById(R.id.tvToolbar);
        if (type.equalsIgnoreCase("ads")) {
            textView.setText(R.string.txt_ads_details);
        } else
            textView.setText(R.string.txt_product_details);
        toolbar.setTitle("");
    }


    private void getProductData() {

        if (getIntent().getExtras() != null) {

            productObj = getIntent().getExtras().getParcelable(AppConstant.PRODUCT_OBJ_KEY);
            if (productObj != null)
                setToolBarTitle(productObj.getType());
        }
    }


    @Override
    protected void setViewListeners() {
        txtViewMoreComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CommentsActivity.class);
                Bundle bundle = new Bundle();

                bundle.putParcelable(AppConstant.PRODUCT_OBJ_COMMENTS, productObj);
                intent.putExtras(bundle);
                startActivityForResult(intent, 101);
                getActivity().overridePendingTransition(R.anim.activity_animation_enter,
                        R.anim.activity_animation_exit);

            }
        });
    }

    @Override
    protected void setViewReferences() {

        imvOfAds = (ImageView) findViewById(R.id.imvOfAds);
        tvProductTitle = (TextView) findViewById(R.id.tv_add_product_title);
        tvStatus = (TextView) findViewById(R.id.tv_add_product_status);
        tvDesciption = (TextView) findViewById(R.id.tv_add_product_item_description);
        checkboxLike = (CheckBox) findViewById(R.id.checkboxLike);
        checkboxShare = (CheckBox) findViewById(R.id.checkboxShare);
        imvBtnEditRemove = (ImageButton) findViewById(R.id.imvBtnEditRemove);
        tvTime = (TextView) findViewById(R.id.tvTime);
        pgrImage = (ProgressBar) findViewById(R.id.pgrImage);
        tvFeatured = (TextView) findViewById(R.id.tvFeatured);
        rlJustificationHolder = (RelativeLayout) findViewById(R.id.rlJustificationHolder);
        edtJustification = (TextView) findViewById(R.id.edtJustification);
        txtViewMoreComment = (TextView) findViewById(R.id.txtViewMoreComment);
        txtRecentComments = (TextView) findViewById(R.id.txtRecentComments);
        txtYourComment = (TextView) findViewById(R.id.txtYourComment);


    }

    @Override
    protected boolean isValidData() {
        return false;
    }

    /**
     * Method is used to show the popup with edit and delete option
     *
     * @param v view on which click is perfomed
     */
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.edit_remove_product, popup.getMenu());
        popup.show();
        showHideEdit(popup);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Utils.hideKeyboard(getActivity());
                switch (item.getItemId()) {
                    case R.id.edit:
                        moveToActivity("Edit");
                        break;
                    case R.id.remove:
                        Utils.deleteDialog(getString(R.string.txt_are_you_sure), getActivity(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeProductApi();
                            }
                        });

                        break;
                    case R.id.repost:
                        moveToActivity("Repost");
                        break;
                    case R.id.reSubmit:
                        moveToActivity("Edit");
                        break;

                }
                return false;
            }
        });
    }


    /**
     * Method is used to remove the product
     */
    private void removeProductApi() {
        final ProgressDialogHandler progressDialogHandler = new ProgressDialogHandler(this);
        progressDialogHandler.show();
        Call<ApiResponse> apiResponseCall = RestClient.getService().apiDeleteProduct(mUserData.getLanguageId(), mUserData.getAuthrizedKey(), mUserData.getId(),
                String.valueOf(productObj.getProductid()));

        apiResponseCall.enqueue(new ApiCall(getActivity()) {
            @Override
            public void onSuccess(ApiResponse apiResponse) {
                progressDialogHandler.hide();
                if (apiResponse.status) {
                    Utils.showToast(getActivity(), getString(R.string.txt_delete_resources));
                    setResult(RESULT_OK);
                    onBackPressed();
                }
            }

            @Override
            public void onFail(Call<ApiResponse> call, Throwable t) {
                progressDialogHandler.hide();
            }
        });
    }


    private void showHideEdit(PopupMenu popup) {

        Menu popupMenu = popup.getMenu();
        if (productObj.getIsExpired() > 0) {
            popupMenu.findItem(R.id.repost).setVisible(true);
            popupMenu.findItem(R.id.edit).setVisible(false);
        } else {
            popupMenu.findItem(R.id.edit).setVisible(true);
            popupMenu.findItem(R.id.repost).setVisible(false);
        }
        if (productObj.productStatus == 3) {
            popupMenu.findItem(R.id.repost).setVisible(false);
            popupMenu.findItem(R.id.edit).setVisible(false);
            popupMenu.findItem(R.id.reSubmit).setVisible(true);
        }

    }

    private void moveToActivity(String type) {

        Bundle bundle = new Bundle();

        bundle.putParcelable(AppConstant.PRODUCT_OBJ_KEY, productObj);
        bundle.putString("edit", type);
        Intent intent = new Intent(getActivity(), RenewAdandProductActivity.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, 1011);

        getActivity().overridePendingTransition(R.anim.activity_animation_enter,
                R.anim.activity_animation_exit);
    }

   /* private void moveToActivity() {

            Bundle bundle = new Bundle();

            bundle.putParcelable(AppConstant.PRODUCT_OBJ_KEY, productObj);

            Intent intent = new Intent(getActivity(), RenewAdandProductActivity.class);

            intent.putExtras(bundle);

            startActivityForResult(intent,1011);

            getActivity().overridePendingTransition(R.anim.activity_animation_enter,
                    R.anim.activity_animation_exit);
        }
*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1011:
                    setResult(RESULT_OK);
                    onBackPressed();
                    break;
            }
        }
    }


}
