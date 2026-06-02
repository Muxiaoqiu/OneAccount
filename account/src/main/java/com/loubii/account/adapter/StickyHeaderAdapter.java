package com.loubii.account.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

public interface StickyHeaderAdapter {
    long getHeaderId(int position);
    RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent);
    void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position);
}
