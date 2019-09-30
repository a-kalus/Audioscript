package com.example.audioscript;

import android.view.View;
/*
 * An Interface enabling item clicks in the recycler views
 */
public interface ItemClickListener {
    void onItemClick(int pos);
    void onItemOptionsClick(int pos, View v);
}
