package com.example.audioscript;

import android.view.View;

public interface ItemClickListener {
    void onItemClick(int pos);
    void onItemOptionsClick(int pos, View v);
}
