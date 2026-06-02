package com.loubii.account.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.appbar.AppBarLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.loubii.account.R;
import com.loubii.account.adapter.BillAdapter;
import com.loubii.account.adapter.StickyHeaderItemDecoration;
import com.loubii.account.bean.AccountModel;
import com.loubii.account.constants.Config;
import com.loubii.account.constants.Extra;
import com.loubii.account.db.AccountModelDao;
import com.loubii.account.db.database.DBManager;
import com.loubii.account.db.database.DbHelper;
import com.loubii.account.event.AccountChangeEvent;
import com.loubii.account.ui.avtivity.BillDetailActivity;
import com.loubii.account.ui.avtivity.BudgetActivity;
import com.loubii.account.ui.avtivity.CalendarActivity;
import com.loubii.account.ui.avtivity.NewContractActivity;
import com.loubii.account.util.TimeUtil;
import com.loubii.account.util.ToastUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class FragmentBill extends BaseEventFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final int CONTRACT_NOT = 0, CONTRACT_ING = 1, CONTRACT_FINISH = 2;
    private boolean mIsFirst = true;
    @BindView(R.id.tv_budget_month) TextView mTvBudgetMonth;
    @BindView(R.id.tv_budget_month_describe) TextView mTvBudgetMonthDescribe;
    @BindView(R.id.tv_expend) TextView mTvExpend;
    @BindView(R.id.tv_expend_describe) TextView mTvExpendDescribe;
    @BindView(R.id.tv_income) TextView mTvIncome;
    @BindView(R.id.tv_income_describe) TextView mTvIncomeDescribe;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.ll_title_contract) FrameLayout mLlTitleContract;
    @BindView(R.id.tv_title_time) TextView mTvTitleTime;
    @BindView(R.id.ll_title_left) FrameLayout mLlTitleLeft;
    @BindView(R.id.ll_title_right) FrameLayout mLlTitleRight;
    @BindView(R.id.ultimate_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.app_bar) AppBarLayout mAppBar;

    private String mParam1;
    private BillAdapter mBillAdapter;
    private DBManager<AccountModel, Long> mDbManager;
    private List<AccountModel> mAccountList = new ArrayList<>();
    private Date mCurrentDate;
    private StickyHeaderItemDecoration mStickyDecoration;

    public FragmentBill() {}

    public static FragmentBill newInstance(String param1) {
        FragmentBill f = new FragmentBill();
        Bundle args = new Bundle(); args.putString(ARG_PARAM1, param1);
        f.setArguments(args); return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbManager = DbHelper.getInstance().author();
        if (getArguments() != null) mParam1 = getArguments().getString(ARG_PARAM1);
    }

    @Override protected int getLayoutId() { return R.layout.fragment_bill; }

    @Override protected void initData() { initTitleData(); }

    private void initTitleData() {
        Calendar calendar = Calendar.getInstance();
        mTvTitleTime.setText(new SimpleDateFormat("MM月dd日").format(calendar.getTime()));
        mCurrentDate = calendar.getTime();
        mAccountList.addAll(getAccountList(0, mCurrentDate));
    }

    @Override protected void initView(View view) { initToolbar(); setTitleView(); initRecycleView(); }

    private void setTitleView() {
        mTvExpendDescribe.setText(mCurrentDate.getMonth()+1+"月支出");
        mTvIncomeDescribe.setText(mCurrentDate.getMonth()+1+"月收入");
        mTvBudgetMonthDescribe.setText(mCurrentDate.getMonth()+1+"月预算");
        if (!mAccountList.isEmpty()) {
            float se=0, si=0;
            for (AccountModel a : mAccountList) {
                if (a.getOutIntype()==1) se+=a.getCount();
                if (a.getOutIntype()==2) si+=a.getCount();
            }
            mTvExpend.setText(String.valueOf(se)); mTvIncome.setText(String.valueOf(si));
        } else { mTvExpend.setText("——"); mTvIncome.setText("——"); }
    }

    private void initRecycleView() {
        final LinearLayoutManager lm = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(lm);
        mBillAdapter = new BillAdapter(mAccountList);
        mBillAdapter.setOnItemClickListener(new BillAdapter.OnItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                Intent i = new Intent(context, CalendarActivity.class);
                i.putExtra(Extra.ACCOUNT_DATE, mCurrentDate.getTime());
                getActivity().startActivity(i);
            }
        });
        mStickyDecoration = new StickyHeaderItemDecoration(mBillAdapter);
        mRecyclerView.addItemDecoration(mStickyDecoration);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() { changeList(0); setTitleView(); mSwipeRefresh.setRefreshing(false); ToastUtil.showShort("数据已更新"); }
                }, 1000);
            }
        });
        mRecyclerView.setAdapter(mBillAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrollStateChanged(RecyclerView rv, int newState) {
                if (newState==RecyclerView.SCROLL_STATE_IDLE && lm.findFirstCompletelyVisibleItemPosition()==0)
                    mAppBar.setExpanded(true);
            }
        });
        mIsFirst = false;
    }

    private void initToolbar() { ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar); }

    private List<AccountModel> getAccountList(int offSet, Date d) {
        return mDbManager.queryBuilder()
                .where(AccountModelDao.Properties.Time.between(TimeUtil.getFirstDayOfMonth(d), TimeUtil.getEndDayOfMonth(d)))
                .orderAsc(AccountModelDao.Properties.Time).offset(offSet*Config.LIST_LOAD_NUM).limit(Config.LIST_LOAD_NUM).list();
    }

    @OnClick({R.id.ll_title_contract,R.id.tv_title_time,R.id.ll_title_left,R.id.ll_title_right,
            R.id.ll_expend_detail,R.id.ll_income_detail,R.id.tv_budget_month,R.id.tv_budget_month_describe})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_title_contract:
                if (getContractState()==CONTRACT_NOT) startActivity(new Intent(context, NewContractActivity.class));
                if (getContractState()==CONTRACT_ING) startActivity(new Intent(context, CalendarActivity.class));
                break;
            case R.id.ll_title_left: changeList(-1); setTitleView(); break;
            case R.id.ll_title_right: changeList(1); setTitleView(); break;
            case R.id.ll_expend_detail: { Intent i=new Intent(context,BillDetailActivity.class); i.putExtra(Extra.ACCOUNT_DATE,mCurrentDate.getTime()); i.putExtra(Extra.ACCOUNT_TYPE,1); startActivity(i); break; }
            case R.id.ll_income_detail: { Intent i=new Intent(context,BillDetailActivity.class); i.putExtra(Extra.ACCOUNT_DATE,mCurrentDate.getTime()); i.putExtra(Extra.ACCOUNT_TYPE,2); startActivity(i); break; }
            case R.id.tv_budget_month: case R.id.tv_budget_month_describe: startActivity(new Intent(context,BudgetActivity.class)); break;
        }
    }

    private void changeList(int monthDistance) {
        if (monthDistance>0 && TimeUtil.date2String(new Date(),"yy年MM月").equals(TimeUtil.date2String(mCurrentDate,"yy年MM月"))) return;
        if (monthDistance!=0) { mCurrentDate=TimeUtil.getMonthAgo(mCurrentDate,monthDistance); mTvTitleTime.setText(TimeUtil.date2String(mCurrentDate,"yy年MM月")); }
        List<AccountModel> accList=getAccountList(0,mCurrentDate);
        if (accList!=null) { mAccountList.clear(); mAccountList.addAll(accList); mBillAdapter.notifyDataSetChanged(); if (mStickyDecoration!=null) mStickyDecoration.invalidateHeaders(); }
    }

    private int getContractState() { return CONTRACT_ING; }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(AccountChangeEvent event) {
        if (event.getMessage().getBooleanExtra(Extra.ACCOUNT_HAS_CHANGE, false)) { changeList(0); setTitleView(); }
    }
}
