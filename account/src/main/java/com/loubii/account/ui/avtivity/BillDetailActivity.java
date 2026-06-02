package com.loubii.account.ui.avtivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.loubii.account.R;
import com.loubii.account.adapter.BillSwipeAdapter;
import com.loubii.account.adapter.StickyHeaderItemDecoration;
import com.loubii.account.bean.AccountModel;
import com.loubii.account.constants.Config;
import com.loubii.account.constants.Extra;
import com.loubii.account.db.AccountModelDao;
import com.loubii.account.event.AccountChangeEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class BillDetailActivity extends BaseActivity {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.ultimate_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.ll_title_return) FrameLayout mLlTitleReturn;
    @BindView(R.id.rb_expend) RadioButton mRbExpend;
    @BindView(R.id.rb_income) RadioButton mRbIncome;
    @BindView(R.id.rg_type) RadioGroup mRgType;
    private BillSwipeAdapter mBillSwipeAdapter;
    private Date mDate;
    private List<AccountModel> mAccountList = new ArrayList<>();
    private int mAccountType;
    private boolean hasDelete = false;
    private StickyHeaderItemDecoration mStickyDecoration;

    @Override protected void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    protected void initData() {
        long time = getIntent().getLongExtra(Extra.ACCOUNT_DATE, 0l);
        mAccountType = getIntent().getIntExtra(Extra.ACCOUNT_TYPE, 0);
        if (time != 0l) { mDate = new Date(time); mAccountList.addAll(getList(0, mDate, 1)); }
    }

    @Override protected int getLayoutId() { return R.layout.activity_bill_detail; }

    @Override protected void initView() { setListener(); initRecyclerView(); initRadioGroup(); }

    private void initRadioGroup() { if (mAccountType == 2) mRbIncome.setChecked(true); }

    private void setListener() {
        mRgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mDate != null) {
                    if (checkedId == R.id.rb_expend) changeList(1);
                    else if (checkedId == R.id.rb_income) changeList(2);
                }
            }
        });
    }

    private void changeList(int outInType) {
        mAccountList.clear(); mAccountList.addAll(getList(0, mDate, outInType));
        mBillSwipeAdapter.notifyDataSetChanged();
        if (mStickyDecoration != null) mStickyDecoration.invalidateHeaders();
    }

    private void initRecyclerView() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mBillSwipeAdapter = new BillSwipeAdapter(mAccountList);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder t) { return false; }
            @Override public void onSwiped(RecyclerView.ViewHolder vh, int dir) { mBillSwipeAdapter.removeItem(vh.getAdapterPosition()); }
        });
        touchHelper.attachToRecyclerView(mRecyclerView);
        mStickyDecoration = new StickyHeaderItemDecoration(mBillSwipeAdapter);
        mRecyclerView.addItemDecoration(mStickyDecoration);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() { mSwipeRefresh.setRefreshing(false); }
        });
        mRecyclerView.setAdapter(mBillSwipeAdapter);
        mBillSwipeAdapter.setOnDeleteListener(new BillSwipeAdapter.OnDeleteListener() {
            @Override public void onDelete() { hasDelete = true; }
        });
    }

    private List<AccountModel> getList(int offSet, Date d, int outInType) {
        return mDbManager.queryBuilder()
                .where(AccountModelDao.Properties.Time.between(
                        com.loubii.account.util.TimeUtil.getFirstDayOfMonth(d),
                        com.loubii.account.util.TimeUtil.getEndDayOfMonth(d)),
                        AccountModelDao.Properties.OutIntype.eq(outInType))
                .orderAsc(AccountModelDao.Properties.Time).offset(offSet*Config.LIST_LOAD_NUM).limit(Config.LIST_LOAD_NUM).list();
    }

    @OnClick(R.id.ll_title_return) public void onViewClicked() { onBackPressed(); }

    @Override public void onBackPressed() {
        if (hasDelete) {
            Intent intent = new Intent(); intent.putExtra(Extra.ACCOUNT_HAS_CHANGE, true);
            EventBus.getDefault().post(new AccountChangeEvent(intent));
        }
        super.onBackPressed();
    }
}
