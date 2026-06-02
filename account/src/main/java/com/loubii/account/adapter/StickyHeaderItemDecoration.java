package com.loubii.account.adapter;

import android.graphics.Canvas;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class StickyHeaderItemDecoration extends RecyclerView.ItemDecoration {

    private final StickyHeaderAdapter adapter;
    private View stickyHeaderView;
    private int currentHeaderPos = -1;

    public StickyHeaderItemDecoration(StickyHeaderAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        if (parent.getChildCount() == 0 || parent.getAdapter() == null) return;

        int firstVisiblePos = ((RecyclerView.LayoutParams) parent.getChildAt(0).getLayoutParams()).getViewAdapterPosition();
        if (firstVisiblePos == RecyclerView.NO_POSITION) return;

        long headerId = adapter.getHeaderId(firstVisiblePos);

        if (currentHeaderPos != firstVisiblePos || stickyHeaderView == null) {
            currentHeaderPos = firstVisiblePos;
            RecyclerView.ViewHolder holder = adapter.onCreateHeaderViewHolder(parent);
            adapter.onBindHeaderViewHolder(holder, firstVisiblePos);
            stickyHeaderView = holder.itemView;
            measureView(stickyHeaderView, parent);
        }

        int contactPoint = stickyHeaderView.getMeasuredHeight();
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int childPos = parent.getChildAdapterPosition(child);
            if (childPos != RecyclerView.NO_POSITION && adapter.getHeaderId(childPos) != headerId) {
                int top = child.getTop() - contactPoint;
                if (top < 0) {
                    c.translate(0, top);
                }
                break;
            }
        }

        c.save();
        c.translate(parent.getPaddingLeft(), 0);
        stickyHeaderView.draw(c);
        c.restore();
    }

    private void measureView(View view, RecyclerView parent) {
        int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthSpec, heightSpec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    public void invalidateHeaders() {
        stickyHeaderView = null;
        currentHeaderPos = -1;
    }
}
