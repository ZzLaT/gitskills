package com.example.personalaccounting.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.personalaccounting.R;

public class BottomNavigationItem extends LinearLayout {

    private ImageView mIconView;
    private TextView mLabelView;
    private boolean mIsActive = false;
    private boolean mIsEnabled = true;

    private Drawable mActiveIcon;
    private Drawable mInactiveIcon;
    private String mLabelText;

    public BottomNavigationItem(Context context) {
        this(context, null);
    }

    public BottomNavigationItem(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomNavigationItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.item_bottom_navigation, this, true);

        mIconView = findViewById(R.id.iv_icon);
        mLabelView = findViewById(R.id.tv_label);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationItem);
            mLabelText = a.getString(R.styleable.BottomNavigationItem_labelText);
            int activeIconResId = a.getResourceId(R.styleable.BottomNavigationItem_activeIcon, 0);
            int inactiveIconResId = a.getResourceId(R.styleable.BottomNavigationItem_inactiveIcon, 0);
            a.recycle();

            if (activeIconResId != 0) {
                mActiveIcon = ContextCompat.getDrawable(context, activeIconResId);
            }
            if (inactiveIconResId != 0) {
                mInactiveIcon = ContextCompat.getDrawable(context, inactiveIconResId);
            }

            if (mLabelText != null) {
                mLabelView.setText(mLabelText);
            }
        }

        updateState();
    }

    public void setActive(boolean active) {
        if (mIsActive != active) {
            mIsActive = active;
            updateState();
        }
    }

    public void setEnabledState(boolean enabled) {
        if (mIsEnabled != enabled) {
            mIsEnabled = enabled;
            updateState();
        }
    }

    public boolean isActive() {
        return mIsActive;
    }

    public void setActiveIcon(Drawable icon) {
        mActiveIcon = icon;
        updateState();
    }

    public void setInactiveIcon(Drawable icon) {
        mInactiveIcon = icon;
        updateState();
    }

    public void setLabelText(String text) {
        mLabelText = text;
        mLabelView.setText(text);
    }

    private void updateState() {
        if (mIsActive && mIsEnabled) {
            mIconView.setImageDrawable(mActiveIcon);
            mLabelView.setSelected(true);
            mLabelView.setEnabled(true);
            setAlpha(1.0f);
        } else if (!mIsEnabled) {
            mIconView.setImageDrawable(mInactiveIcon);
            mLabelView.setSelected(false);
            mLabelView.setEnabled(false);
            setAlpha(0.5f);
        } else {
            mIconView.setImageDrawable(mInactiveIcon);
            mLabelView.setSelected(false);
            mLabelView.setEnabled(true);
            setAlpha(1.0f);
        }
    }
}