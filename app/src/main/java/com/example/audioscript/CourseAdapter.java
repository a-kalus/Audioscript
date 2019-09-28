package com.example.audioscript;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private List<Course> courseList;
    private Context context;
    private ItemClickListener itemClickListener;


    public CourseAdapter(Context context, List list) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        courseList = list;
    }

    public CourseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View lectureView = inflater.inflate(R.layout.listitem_lectures, parent, false);

        ViewHolder viewHolder = new ViewHolder(lectureView);
        return viewHolder;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView name;
        TextView date;

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
            name = (TextView) v.findViewById(R.id.item_name);
            date = (TextView) v.findViewById(R.id.item_date);

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
    public void onBindViewHolder(@NonNull CourseAdapter.ViewHolder holder, int position) {
        Course course = courseList.get(position);

        holder.name.setText(course.getCourseName());
        holder.date.setText(""+course.getId());
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }
}
