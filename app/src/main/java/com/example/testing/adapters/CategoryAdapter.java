package com.example.testing.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.famcart.R;
import com.example.testing.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }

    public CategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvName.setText(category.getName());

        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(category.getImageUrl())
                    .placeholder(R.drawable.essentials)
                    .error(R.drawable.essentials)
                    .into(holder.ivIcon);
        } else {
            // Fallback icons if no URL
            int iconRes = R.drawable.essentials;
            String name = category.getName().toLowerCase();
            if (name.contains("dairy")) iconRes = R.drawable.breads_eggs;
            else if (name.contains("snack")) iconRes = R.drawable.snacks;
            else if (name.contains("sweet")) iconRes = R.drawable.sweet;
            else if (name.contains("pharma")) iconRes = R.drawable.pharmacy;
            else if (name.contains("beverage")) iconRes = R.drawable.beverages;
            holder.ivIcon.setImageResource(iconRes);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_category_icon);
            tvName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}
