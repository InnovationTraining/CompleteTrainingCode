package com.training.hms.iap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.iap.entity.ProductInfo;
import com.training.hms.R;
import com.training.hms.util.CollectionUtil;

import java.util.List;

public class IapAdapter extends RecyclerView.Adapter<IapAdapter.ItemViewHolder> {

    private final List<ProductInfo> mItems;
    private IRecyclerItemListener listener;

    public IapAdapter(List<ProductInfo> mItems) {
        this.mItems = mItems;
    }

    public void setListener(IRecyclerItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.iap_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        ProductInfo productInfo = mItems.get(position);
        holder.name.setText(productInfo.getProductName());
        holder.price.setText(productInfo.getPrice());
        holder.id.setText(productInfo.getProductId());
        holder.type.setText(getPriceType(productInfo.getPriceType()));
        if (listener != null) {
            holder.buy.setOnClickListener(v -> listener.onItemClick(holder.buy, position));
        }
    }

    private String getPriceType(int type) {
        if (type == 0) {
            return "消耗型商品";
        }
        if (type == 1) {
            return "非消耗型商品";
        }
        return "订阅型商品";
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.isEmpty(mItems) ? 0 : mItems.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView name;
        AppCompatTextView price;
        AppCompatTextView id;
        AppCompatTextView type;
        AppCompatButton buy;

        ItemViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.iap_item_name);
            price = itemView.findViewById(R.id.iap_item_price);
            id = itemView.findViewById(R.id.iap_item_id);
            type = itemView.findViewById(R.id.iap_item_type);
            buy = itemView.findViewById(R.id.iap_item_buy);
        }
    }
}