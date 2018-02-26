package com.mythcon.savr.ngelih.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mythcon.savr.ngelih.Interface.ItemClickListener;
import com.mythcon.savr.ngelih.R;

/**
 * Created by SAVR on 26/02/2018.
 */

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtOrderId, txtOrderStatus, txtOrderAddress, txtOrderPhone;

    private ItemClickListener itemClickListener;

    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
        txtOrderAddress = itemView.findViewById(R.id.order_address);

        //itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onclick(v,getAdapterPosition(),false);
    }
}
