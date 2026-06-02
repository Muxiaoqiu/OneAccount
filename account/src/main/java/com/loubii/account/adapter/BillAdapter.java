package com.loubii.account.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loubii.account.R;
import com.loubii.account.bean.AccountModel;
import com.loubii.account.util.TimeUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ItemHolder> implements StickyHeaderAdapter {

    private List<AccountModel> mAccountList;
    private OnItemClickListener mItemClickListener;

    public BillAdapter(List<AccountModel> mAccountList) {
        this.mAccountList = mAccountList;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill_list, parent, false);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemHolder h, int position) {
        h.itemView.setTag(position);
        float count = mAccountList.get(position).getCount();
        int type = mAccountList.get(position).getOutIntype();
        String note = mAccountList.get(position).getNote();
        String remark = mAccountList.get(position).getRemark();
        if (type == 1) count = -count;
        h.tvClassifyMoney.setText(count + "");
        h.tvClassify.setText(mAccountList.get(position).getDetailType());
        h.ivClassify.setImageResource(mAccountList.get(position).getPicRes());
        if (TextUtils.isEmpty(note) && TextUtils.isEmpty(remark)) {
            h.tvClassifyDescribe.setVisibility(View.GONE);
        } else
            h.tvClassifyDescribe.setText(note + "," + remark);
    }

    @Override
    public int getItemCount() { return mAccountList.size(); }

    @Override
    public long getHeaderId(int position) {
        return getDay(mAccountList.get(position).getTime());
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stick_header, parent, false);
        return new HeaderHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        HeaderHolder h = (HeaderHolder) holder;
        String time = TimeUtil.date2String(mAccountList.get(position).getTime(), "MM月dd日");
        String week = TimeUtil.getWeekByDate(mAccountList.get(position).getTime());
        h.tvStickyDay.setText(time);
        h.tvStickyWeek.setText(week);
        float sumExpend = 0f, sumIncome = 0f;
        for (int i = position; i < mAccountList.size(); i++) {
            Date date = mAccountList.get(i).getTime();
            if (getDay(date) == getHeaderId(position)) {
                int t = mAccountList.get(i).getOutIntype();
                if (t == 1) sumExpend += mAccountList.get(i).getCount();
                if (t == 2) sumIncome += mAccountList.get(i).getCount();
            } else break;
        }
        h.tvStickyExpend.setText("支出：" + sumExpend);
        h.tvStickyIncome.setText("收入：" + sumIncome);
    }

    private long getDay(Date time) {
        Calendar c = Calendar.getInstance(); c.setTime(time);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public interface OnItemClickListener { void onItemClick(View view, int position); }
    public void setOnItemClickListener(OnItemClickListener l) { this.mItemClickListener = l; }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivClassify; TextView tvClassify, tvClassifyDescribe, tvClassifyMoney; View itemBill;
        ItemHolder(View v) {
            super(v);
            ivClassify = v.findViewById(R.id.iv_classify);
            tvClassify = v.findViewById(R.id.tv_classify);
            tvClassifyDescribe = v.findViewById(R.id.tv_classify_describe);
            tvClassifyMoney = v.findViewById(R.id.tv_classify_money);
            itemBill = v.findViewById(R.id.item_bill);
            v.setOnClickListener(this);
        }
        @Override public void onClick(View v) { if (mItemClickListener != null) mItemClickListener.onItemClick(v, (int)v.getTag()); }
    }

    class HeaderHolder extends RecyclerView.ViewHolder {
        TextView tvStickyDay, tvStickyWeek, tvStickyExpend, tvStickyIncome;
        HeaderHolder(View v) {
            super(v);
            tvStickyDay = v.findViewById(R.id.tv_sticky_day);
            tvStickyWeek = v.findViewById(R.id.tv_sticky_week);
            tvStickyExpend = v.findViewById(R.id.tv_sticky_expend);
            tvStickyIncome = v.findViewById(R.id.tv_sticky_income);
        }
    }
}
