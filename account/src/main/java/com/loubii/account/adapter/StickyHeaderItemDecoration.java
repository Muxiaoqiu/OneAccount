package com.loubii.account.adapter;

import android.graphics.Canvas;
import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class StickyHeaderItemDecoration extends RecyclerView.ItemDecoration {

    private final StickyHeaderAdapter adapter;
    private View stickyHeaderView;
    private int currentHeaderPos = -1;
    private int headerHeight;

    public StickyHeaderItemDecoration(StickyHeaderAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) return;

        // First item always gets header offset; subsequent items get it at group boundaries
        if (position == 0 || adapter.getHeaderId(position) != adapter.getHeaderId(position - 1)) {
            if (stickyHeaderView == null) {
                RecyclerView.ViewHolder holder = adapter.onCreateHeaderViewHolder(parent);
                adapter.onBindHeaderViewHolder(holder, position);
                stickyHeaderView = holder.itemView;
                measureHeader(stickyHeaderView, parent);
                headerHeight = stickyHeaderView.getMeasuredHeight();
            }
            outRect.top = headerHeight;
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        if (parent.getChildCount() == 0 || parent.getAdapter() == null) return;

        int firstVisiblePos = parent.getChildAdapterPosition(parent.getChildAt(0));
        if (firstVisiblePos == RecyclerView.NO_POSITION) return;

        long headerId = adapter.getHeaderId(firstVisiblePos);

        if (currentHeaderPos != firstVisiblePos || stickyHeaderView == null) {
            currentHeaderPos = firstVisiblePos;
            RecyclerView.ViewHolder holder = adapter.onCreateHeaderViewHolder(parent);
            adapter.onBindHeaderViewHolder(holder, firstVisiblePos);
            stickyHeaderView = holder.itemView;
            measureHeader(stickyHeaderView, parent);
            headerHeight = stickyHeaderView.getMeasuredHeight();
        }

        // Find the next header position to determine when the current header should be pushed up
        int pushUpOffset = 0;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int childPos = parent.getChildAdapterPosition(child);
            if (childPos != RecyclerView.NO_POSITION && adapter.getHeaderId(childPos) != headerId) {
                int top = child.getTop();
                if (top < headerHeight) {
                    pushUpOffset = top - headerHeight;
                }
                break;
            }
        }

        c.save();
        c.translate(parent.getPaddingLeft(), pushUpOffset);
        stickyHeaderView.draw(c);
        c.restore();
    }

    private void measureHeader(View view, RecyclerView parent) {
        int widthSpec = View.MeasureSpec.makeMeasureSpec(
                parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight(),
                View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthSpec, heightSpec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    public void invalidateHeaders() {
        stickyHeaderView = null;
        currentHeaderPos = -1;
        headerHeight = 0;
    }
}
