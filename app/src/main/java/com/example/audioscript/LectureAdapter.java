package com.example.audioscript;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
/*
 * Adapter class for the population of the Lectures Activities' RecyclerView
 */
public class LectureAdapter extends RecyclerView.Adapter<LectureAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private List<LectureItem> lectureList;
    private Context context;
    private ItemClickListener itemClickListener;


    public LectureAdapter(Context context, List list) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        lectureList = list;
    }

    @NonNull
    public LectureAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View lectureView = inflater.inflate(R.layout.list_item, parent, false);

        return new ViewHolder(lectureView);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView date;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            name = (TextView) v.findViewById(R.id.item_name);
            date = (TextView) v.findViewById(R.id.item_date);
            ImageButton moreButton = (ImageButton) v.findViewById(R.id.options_btn);
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickListener.onItemOptionsClick(getAdapterPosition(), view);
                }
            });
        }

        @Override
        public void onClick(View view) {
            //Log.d("RecyclerView", "onClick：" + getAdapterPosition());
            itemClickListener.onItemClick(getAdapterPosition());

        }
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull LectureAdapter.ViewHolder holder, int position) {
        LectureItem item = lectureList.get(position);

        holder.name.setText(item.getName());
        holder.date.setText(item.getFormattedDate());
    }

    @Override
    public int getItemCount() {
        return lectureList.size();
    }
}
