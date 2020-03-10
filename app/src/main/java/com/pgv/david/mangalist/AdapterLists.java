package com.pgv.david.mangalist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AdapterLists extends RecyclerView.Adapter<AdapterLists.MangasViewHolder> implements View.OnClickListener {

    private ArrayList<Manga> dataList;
    private ArrayList<Manga> dataFilterList;
    private DataFilter dataFilter;
    private String fragment;

    public AdapterLists(ArrayList<Manga> data, String fragment) {
        this.dataList = new ArrayList<>();
        this.dataFilterList = data;
        this.fragment = fragment;
    }

    public AdapterLists.MangasViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.lists_layout,parent,false);
        itemView.setOnClickListener(this);
        return new AdapterLists.MangasViewHolder(itemView);
    }

    public void onBindViewHolder(AdapterLists.MangasViewHolder holder, int position) {
        holder.bindMangasRecyclerView(dataList.get(position));
    }

    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onClick(View v) {
        RecyclerView recyclerView = (RecyclerView) v.getParent();
        Context context = recyclerView.getContext();
        int position = recyclerView.getChildAdapterPosition(v);
        Manga manga = this.dataList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("manga",manga);
        bundle.putString("fragment",fragment);
        Intent intent = new Intent(context,MangaDetailsActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
        Activity activity = (Activity) context;
        activity.overridePendingTransition(R.transition.transition_fade_in,R.transition.transition_fade_out);
    }

    public Filter getFilter() {
        if(dataFilter == null) {
            dataFilter = new DataFilter();
        }
        return dataFilter;
    }

    /**************************************/
    private class DataFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String mangaType = constraint.toString();
            FilterResults results = new FilterResults();
            ArrayList<Manga> filterList = new ArrayList<>();

            if(mangaType.equals("Any")) {
                for(int i = 0; i < dataFilterList.size();i++) {
                    filterList.add(dataFilterList.get(i));
                }
            } else {
                for(int i = 0; i < dataFilterList.size();i++) {
                    Manga manga = dataFilterList.get(i);
                    if(mangaType.equals(manga.getType())) {
                        filterList.add(manga);
                    }
                }
            }

            results.count = filterList.size();
            results.values = filterList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            dataList = (ArrayList<Manga>) results.values;
            notifyDataSetChanged();
        }
    }
    /**************************************/

    public static class MangasViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView title;
        private Context context;


        public MangasViewHolder(View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.imgMangaLists);
            this.title = itemView.findViewById(R.id.titleMangaLists);
            this.context = itemView.getContext();
        }

        public void bindMangasRecyclerView(Manga manga) {
            Glide.with(this.context)
                    .load(manga.getCover())
                    .into(this.image);
            this.title.setText(manga.getTitle());
        }
    }
}
