package com.rajaselvan.calendar;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by rajaselvan on 10/12/17.
 */

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder> {
    private List<EventModel> values;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtDate;
        public TextView txtMonth;
        public TextView txtSummary;
        public TextView txtTime;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            txtDate = (TextView) v.findViewById(R.id.tv_date);
            txtMonth = (TextView) v.findViewById(R.id.tv_month);
            txtSummary = (TextView) v.findViewById(R.id.tv_summary);
            txtTime = (TextView) v.findViewById(R.id.tv_time);
        }
    }

    public void add(int position, EventModel item) {
        values.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }

    public EventListAdapter(List<EventModel> myDataset) {
        values = myDataset;
    }

    @Override
    public EventListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v = inflater.inflate(R.layout.activity_event_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(EventListAdapter.ViewHolder holder, int position) {
        final EventModel model = values.get(position);
        holder.txtDate.setText(getDateInFormat("d", new Date(model.getEventStartDateTime().getValue())));
        holder.txtMonth.setText(getDateInFormat("MMM", new Date(model.getEventStartDateTime().getValue())));
        holder.txtSummary.setText(model.getEventSummary());
        holder.txtTime.setText(getDateInFormat("hh:mm", new Date(model.getEventStartDateTime().getValue())));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }


    private String getDateInFormat(String format, Date date){
        String result = new SimpleDateFormat(format).format(date);
        return result;
    }
}
