package edu.dartmouth.cs65.dartmouthnaps;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CommentAdapter extends BaseAdapter {
    private static ArrayList<Comment> comments;

    private LayoutInflater mInflater;

    public CommentAdapter(Context photosFragment, ArrayList<Comment> results){
        comments = results;
        mInflater = LayoutInflater.from(photosFragment);
    }

    @Override
    public int getCount() {
        return comments.size();
    }

    @Override
    public Object getItem(int arg0) {
        return comments.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.fragment_comment, null);
            holder = new ViewHolder();
            holder.author = convertView.findViewById(R.id.comment_author);
            holder.dateTime = convertView.findViewById(R.id.comment_date_time);
            holder.location = convertView.findViewById(R.id.comment_location);
            holder.body = convertView.findViewById(R.id.comment_body);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.author.setText(comments.get(position).getAuthor());
        holder.dateTime.setText("" + comments.get(position).getDateTime());
        holder.location.setText(comments.get(position).getLocation());
        holder.body.setText(comments.get(position).getBody());

        return convertView;
    }

    static class ViewHolder{
        TextView author, dateTime, location, body;
    }
}