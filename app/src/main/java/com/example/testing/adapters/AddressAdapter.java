package com.example.testing.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famcart.R;
import com.example.testing.models.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    private List<Address> addresses = new ArrayList<>();
    private final OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onEdit(Address address);
        void onDelete(Address address);
        void onSetDefault(Address address);
    }

    public AddressAdapter(OnAddressActionListener listener) {
        this.listener = listener;
    }

    public void updateItems(List<Address> newAddresses) {
        this.addresses = newAddresses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Address address = addresses.get(position);
        holder.tvTag.setText(address.getTag());
        holder.tvFullAddress.setText(address.getFullAddress());

        if (address.isDefault()) {
            holder.tvDefaultBadge.setVisibility(View.VISIBLE);
            holder.btnSetDefault.setVisibility(View.GONE);
        } else {
            holder.tvDefaultBadge.setVisibility(View.GONE);
            holder.btnSetDefault.setVisibility(View.VISIBLE);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(address));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(address));
        holder.btnSetDefault.setOnClickListener(v -> listener.onSetDefault(address));
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTag, tvFullAddress, tvDefaultBadge, btnSetDefault;
        ImageView btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTag = itemView.findViewById(R.id.tv_address_tag);
            tvFullAddress = itemView.findViewById(R.id.tv_full_address);
            tvDefaultBadge = itemView.findViewById(R.id.tv_default_badge);
            btnSetDefault = itemView.findViewById(R.id.btn_set_default);
            btnEdit = itemView.findViewById(R.id.btn_edit_address);
            btnDelete = itemView.findViewById(R.id.btn_delete_address);
        }
    }
}
