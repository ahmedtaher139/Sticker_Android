package com.sticker_android.controller.activities.filter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sticker_android.R;
import com.sticker_android.controller.activities.base.AppBaseActivity;
import com.sticker_android.model.User;
import com.sticker_android.model.corporateproduct.Category;
import com.sticker_android.network.ApiCall;
import com.sticker_android.network.ApiResponse;
import com.sticker_android.network.RestClient;
import com.sticker_android.utils.AppLogger;
import com.sticker_android.utils.sharedpref.AppPref;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class FanFilterActivity extends AppBaseActivity {
    private ArrayList<Category> categoryArrayList = new ArrayList<>();
    private View contentView;
    private ImageView imageClose;
    private ImageView imvSave;
    private RadioButton chkMostDownload, chkRecentUpload;
    private ListView listFilter;
    com.sticker_android.view.BottomSheetFragment.IFilter iFilter;
    private User mUserdata;
//    this.categoryArrayList = categoryArrayList;

    private CheckBox chkSelectAll;
    private AppPref appPref;
    private ArrayList<Category> categoryList = new ArrayList<>();
    private CustomListViewAdapter customListViewAdapter;
    ProgressBar progressBar;
    private RadioGroup radioGroup;
    private String type = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_filter);
        init();
        getIntentInfo();
        getuserInfo();
        setViewReferences();
        setViewListeners();
        fetchCategoryApi();
        setListViewAdaptor();
        changeStatusBarColor(getResources().getColor(R.color.colorFanText));
        showHide();
    }

    private void showHide() {

    if(type.equalsIgnoreCase("5")||type.equalsIgnoreCase("6")){
        chkMostDownload.setVisibility(View.GONE);
        chkRecentUpload.setChecked(true);
    }
    }

    private void getIntentInfo() {
        if (getIntent().getExtras() != null) {
            type = getIntent().getExtras().getString("type");
        }
    }

    private void init() {
        appPref = new AppPref(getActivity());
    }

    private void getuserInfo() {
        mUserdata = appPref.getUserInfo();
    }

    @Override
    protected void setViewListeners() {

        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                onBackPressed();
            }
        });

        imvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<Integer> categoryArray = new ArrayList<>();
                AppLogger.debug("vfdjvnjf", "nvd,fv");

                for (Category category : categoryList
                        ) {
                    if (category.isChecked) {
                        categoryArray.add(category.categoryId);
                    }
                }
                Gson gson = new Gson();
                String jsonNames = gson.toJson(categoryArray);
                AppLogger.debug("data is :", jsonNames.toString());

              /*  int selectedId = radioGroup.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                chkMostDownload = (RadioButton) contentView.findViewById(selectedId);
             */
                String filterDataName = "most_download";

                if (chkRecentUpload.isChecked()) {
                    filterDataName = "recent_upload";
                } else if (chkMostDownload.isChecked()) {
                    filterDataName = "most_download";
                }
                Bundle b = new Bundle();
                b.putParcelableArrayList("categoryList", categoryList);
                b.putString("filterBy", filterDataName);
                Intent intent = new Intent();
                intent.putExtras(b);
                setResult(RESULT_OK, intent);
                onBackPressed();

            }
        });
        listFilter.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

        chkSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (buttonView.isPressed()) {
                    if (isChecked) {
                        if (categoryList != null) {
                            //  selectAll();
                            AppLogger.debug("djncdc", "xmdvndmv" + customListViewAdapter.getCount());
                            for (int i = 0; i < customListViewAdapter.getCount(); i++) {
                                listFilter.setItemChecked(i, true);
                            }
                            customListViewAdapter.notifyDataSetChanged();
                        }
                    } else {
                        for (int i = 0; i < customListViewAdapter.getCount(); i++) {
                            listFilter.setItemChecked(i, false);
                        }
                        customListViewAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void selectAll() {

        for (Category category :
                categoryList) {
            category.isChecked = true;
        }
        if (categoryList != null) {
            ArrayList<Category> tempList = new ArrayList<>();
            tempList.addAll(categoryList);
            categoryList.clear();
            customListViewAdapter.setData(tempList);
        }
    }

    private void fetchCategoryApi() {
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
        Call<ApiResponse> apiResponseCall = RestClient.getService().apiCorporateCategoryList(mUserdata.getLanguageId(), mUserdata.getAuthrizedKey()
                , mUserdata.getId(), "corporate_category");

        apiResponseCall.enqueue(new ApiCall(getActivity()) {
            @Override
            public void onSuccess(ApiResponse apiResponse) {
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);

                if (apiResponse.status) {
                    categoryList = apiResponse.paylpad.corporateCategories;
                    customListViewAdapter.setData(categoryList);
                }

            }

            @Override
            public void onFail(Call<ApiResponse> call, Throwable t) {
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);
                setResult(RESULT_CANCELED);
                onBackPressed();
            }
        });

    }

    private void setListViewAdaptor() {

        customListViewAdapter = new CustomListViewAdapter();
        listFilter.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listFilter.setAdapter(customListViewAdapter);

    }

    @Override
    protected void setViewReferences() {
        imageClose = (ImageView) findViewById(R.id.imageClose);
        imvSave = (ImageView) findViewById(R.id.imvSave);
        chkMostDownload = (RadioButton) findViewById(R.id.chkMostDownload);
        chkRecentUpload = (RadioButton) findViewById(R.id.chkRecentUpload);
        listFilter = (ListView) findViewById(R.id.listFilter);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        chkSelectAll = (CheckBox) findViewById(R.id.chkSelectAll);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    protected boolean isValidData() {
        return false;
    }

    public class CustomListViewAdapter extends BaseAdapter {


        private final List<Category> items = new ArrayList<>();
        Context context;

        public CustomListViewAdapter() {
        }

        /*private view holder class*/
        private class ViewHolder {
            CheckBox checkBoxMultipleSelect;
            TextView txtTitle;

        }

        @Override
        public int getCount() {
            return categoryList.size();
        }

        @Override
        public Category getItem(int position) {

            return categoryList.get(position);
        }


        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            CustomListViewAdapter.ViewHolder holder = null;
            final Category rowItem = categoryList.get(position);

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_list_filter, null);
                holder = new CustomListViewAdapter.ViewHolder();
                holder.txtTitle = (TextView) convertView.findViewById(R.id.tvTitle);

                holder.checkBoxMultipleSelect = (CheckBox) convertView.findViewById(R.id.chkSelectItem);
                convertView.setTag(holder);
            } else
                holder = (CustomListViewAdapter.ViewHolder) convertView.getTag();

            // holder.checkBoxMultipleSelect.setText(rowItem.categoryName);
            //holder.txtTitle.setText(rowItem.categoryName);

            holder.checkBoxMultipleSelect.setText(rowItem.categoryName);
            final CustomListViewAdapter.ViewHolder finalHolder = holder;
            holder.checkBoxMultipleSelect.setChecked(listFilter.isItemChecked(position));
            rowItem.isChecked = listFilter.isItemChecked(position);
            holder.checkBoxMultipleSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.isPressed()) {
                        if (isChecked) {
                            chkSelectAll.setChecked(false);
                            rowItem.isChecked = true;
                        } else {
                            rowItem.isChecked = false;
                            chkSelectAll.setChecked(false);
                        }
                    }
                }
            });

            return convertView;
        }


        public void setData(ArrayList<Category> categoryArrayList) {

            if (categoryArrayList != null) {
                categoryList = categoryArrayList;
                notifyDataSetChanged();
            }
        }
    }
}
