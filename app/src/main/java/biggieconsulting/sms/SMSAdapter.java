package biggieconsulting.sms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Author: mzm
 * Created on 14/07/2018 AD
 */
public class SMSAdapter extends RecyclerView.Adapter<SMSAdapter.ViewHolder> {
    private Context mContext;
    private List<Bean> mDataList;
    private ClickListener clickListener;

    public SMSAdapter(Context context, List<Bean> dataList) {
        this.mContext = context;
        this.mDataList = dataList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_s_m_s_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Bean entity = mDataList.get(position);
        holder.lblNumber.setText(entity.getAddress());
        Log.w("body", entity.getBody());
        holder.lblMsg.setText(entity.getBody());
    }

    public void setOnClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView lblNumber, lblMsg;

        ViewHolder(View itemView) {
            super(itemView);
            lblNumber = itemView.findViewById(R.id.lblNumber);
            lblMsg = itemView.findViewById(R.id.lblMsg);
        }
    }

    public interface ClickListener {
        void onClick(View view, int position);
    }

}
