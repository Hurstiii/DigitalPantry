package com.hurst.digitalpantry.ui.main.Session;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.hurst.digitalpantry.R;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;

public class SwipeSessionItemCallback extends ItemTouchHelper.Callback {

    private Drawable rightIcon;
    private Drawable leftIcon;
    private Drawable icon;
    private ColorDrawable posBackground;
    private ColorDrawable negBackground;
    private ColorDrawable background;
    private MySessionRecyclerViewAdapter mAdapter;

    public SwipeSessionItemCallback(MySessionRecyclerViewAdapter adapter) {
        mAdapter = adapter;
        leftIcon = ContextCompat.getDrawable(mAdapter.getContext(), R.drawable.ic_remove_black_24dp);
        rightIcon = ContextCompat.getDrawable(mAdapter.getContext(), R.drawable.ic_add_black_24dp);
        icon = rightIcon; // left and right icon must have same intrinsic size
        posBackground = new ColorDrawable(ContextCompat.getColor(mAdapter.getContext(), R.color.blueHighlight));
        negBackground = new ColorDrawable(ContextCompat.getColor(mAdapter.getContext(), R.color.pinkHighlight));
        background = negBackground;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeFlag(ACTION_STATE_SWIPE, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.2f;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        mAdapter.onSwipe(position, direction);

    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        // restrict the movement of the view on the X direction
        float change = dX;
        float maxChange = viewHolder.itemView.getWidth() * 0.3f;
        if(change > maxChange) {
            change = maxChange;
        } else if (change < -maxChange) {
            change = -maxChange;
        }
        super.onChildDraw(c, recyclerView, viewHolder, change, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + iconMargin;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > 0) { // Swiping to the right
            icon = rightIcon;
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = iconLeft + icon.getIntrinsicWidth();
            icon.setBounds(
                    iconLeft,
                    iconTop,
                    iconRight,
                    iconBottom
            );

            background = posBackground;
            background.setBounds(
                    itemView.getLeft(),
                    itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                    itemView.getBottom()
            );
        } else if (dX < 0) { // Swiping to the left
            icon = leftIcon;
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(
                    iconLeft,
                    iconTop,
                    iconRight,
                    iconBottom
            );

            background = negBackground;
            background.setBounds(
                    itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(),
                    itemView.getRight(),
                    itemView.getBottom()
            );
        } else { // View is un-swiped
            background = negBackground;
            background.setBounds(0,0,0,0);
            icon.setBounds(0,0,0,0);
        }

        background.draw(c);
        icon.draw(c);
    }
}
