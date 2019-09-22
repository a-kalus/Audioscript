package com.example.audioscript;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

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

    public LectureAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View lectureView = inflater.inflate(R.layout.listitem_lectures, parent, false);

        ViewHolder viewHolder = new ViewHolder(lectureView);
        return viewHolder;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView taskName;
        TextView taskDate;

        public ViewHolder(View v) {
            super(v);
/*
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("RecyclerView", "onClick：" + getAdapterPosition());
                }
            });
*/          v.setOnClickListener(this);
            taskName = (TextView) v.findViewById(R.id.lecture_name);
            taskDate = (TextView) v.findViewById(R.id.lecture_date);

        }

        @Override
        public void onClick(View view) {
            //Log.d("RecyclerView", "onClick：" + getAdapterPosition());
            itemClickListener.onItemClick(getAdapterPosition());

        }
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
    
    @Override
    public void onBindViewHolder(@NonNull LectureAdapter.ViewHolder holder, int position) {
        LectureItem task = lectureList.get(position);

        holder.taskName.setText(task.getName());
        holder.taskDate.setText(task.getFormattedDate());
    }

    @Override
    public int getItemCount() {
        return lectureList.size();
    }
}
