package com.loubii.account.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loubii.account.R;
import com.loubii.account.app.AccountApplication;
import com.loubii.account.bean.AccountModel;
import com.loubii.account.db.database.DBManager;
import com.loubii.account.util.TimeUtil;
import com.loubii.account.util.ToastUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BillSwipeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyHeaderAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;

    private List<AccountModel> mAccountList;
    private OnDeleteListener mDeleteListener;

    public BillSwipeAdapter(List<AccountModel> mAccountList) {
        this.mAccountList = mAccountList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || getHeaderId(position) != getHeaderId(position - 1))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_stick_header, parent, false);
            return new HeaderHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill_list_swipe, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemHolder) {
            ItemHolder h = (ItemHolder) holder;
            float count = mAccountList.get(position).getCount();
            String note = mAccountList.get(position).getNote();
            String remark = mAccountList.get(position).getRemark();
            h.tvClassifyMoney.setText(count + "");
            h.tvClassify.setText(mAccountList.get(position).getDetailType());
            h.ivClassify.setImageResource(mAccountList.get(position).getPicRes());
            if (TextUtils.isEmpty(note) && TextUtils.isEmpty(remark)) {
                h.tvClassifyDescribe.setVisibility(View.GONE);
            } else {
                h.tvClassifyDescribe.setText(note + "," + remark);
            }
        } else if (holder instanceof HeaderHolder) {
            HeaderHolder h = (HeaderHolder) holder;
            String time = TimeUtil.date2String(mAccountList.get(position).getTime(), "MM月dd日");
            String week = TimeUtil.getWeekByDate(mAccountList.get(position).getTime());
            h.tvStickyDay.setText(time);
            h.tvStickyWeek.setText(week);
            float sumMoney = 0f;
            for (int i = position; i < mAccountList.size(); i++) {
                Date date = mAccountList.get(i).getTime();
                if (getDay(date) == getHeaderId(position)) {
                    sumMoney += mAccountList.get(i).getCount();
                } else break;
            }
            int type = mAccountList.get(position).getOutIntype();
            String strType = (type == 1) ? "支出：" + sumMoney : "收入：" + sumMoney;
            h.tvStickyExpend.setVisibility(View.GONE);
            h.tvStickyIncome.setText(strType);
        }
    }

    @Override
    public int getItemCount() { return mAccountList.size(); }

    public void removeItem(int position) {
        DBManager dbManager = AccountApplication.getDbManager();
        boolean isDelete = dbManager.delete(mAccountList.get(position));
        if (isDelete) {
            mAccountList.remove(position);
            notifyItemRemoved(position);
            if (position < mAccountList.size())
                notifyItemRangeChanged(position, mAccountList.size() - position);
            if (mDeleteListener != null) mDeleteListener.onDelete();
            ToastUtil.showShort("删除条目:" + position);
        } else
            ToastUtil.showShort("删除失败");
    }

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
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int pos) {
        HeaderHolder h = (HeaderHolder) holder;
        String time = TimeUtil.date2String(mAccountList.get(pos).getTime(), "MM月dd日");
        String week = TimeUtil.getWeekByDate(mAccountList.get(pos).getTime());
        h.tvStickyDay.setText(time);
        h.tvStickyWeek.setText(week);
        float sumMoney = 0f;
        for (int i = pos; i < mAccountList.size(); i++) {
            Date date = mAccountList.get(i).getTime();
            if (getDay(date) == getHeaderId(pos))
                sumMoney += mAccountList.get(i).getCount();
            else break;
        }
        int type = mAccountList.get(pos).getOutIntype();
        h.tvStickyExpend.setVisibility(View.GONE);
        h.tvStickyIncome.setText((type == 1) ? "支出：" + sumMoney : "收入：" + sumMoney);
    }

    private long getDay(Date time) {
        Calendar c = Calendar.getInstance(); c.setTime(time);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        ImageView ivClassify; TextView tvClassify, tvClassifyDescribe, tvClassifyMoney; LinearLayout llDelete;
        ItemHolder(View v) {
            super(v);
            ivClassify = v.findViewById(R.id.iv_classify);
            tvClassify = v.findViewById(R.id.tv_classify);
            tvClassifyDescribe = v.findViewById(R.id.tv_classify_describe);
            tvClassifyMoney = v.findViewById(R.id.tv_classify_money);
            llDelete = v.findViewById(R.id.ll_delete);
        }
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

    public interface OnDeleteListener { void onDelete(); }
    public void setOnDeleteListener(OnDeleteListener l) { this.mDeleteListener = l; }
}
