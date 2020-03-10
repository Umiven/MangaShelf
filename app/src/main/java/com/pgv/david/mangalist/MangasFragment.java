package com.pgv.david.mangalist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MangasFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {
    private Spinner spinner;
    private RecyclerView rvMangaList;
    private AdapterLists recyclerAdapter;
    private FirebaseFirestore firestore;
    private LinearLayout progressBar;
    private SwipeRefreshLayout swipeRefresh;

    public MangasFragment() {}

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_mangas, container, false);
        this.progressBar = rootView.findViewById(R.id.mangasFragmentLoading);
        this.swipeRefresh = rootView.findViewById(R.id.swipeRefreshMangas);
        swipeRefresh.setOnRefreshListener(this);
        this.firestore = FirebaseFirestore.getInstance();
        // Spinner
        this.spinner = rootView.findViewById(R.id.spinnerMangas);

        // Firestore mangas
        this.rvMangaList = rootView.findViewById(R.id.rvMangasLayout);
        this.rvMangaList.hasFixedSize();
        getMangas();
        this.rvMangaList.setLayoutManager(new GridLayoutManager(rootView.getContext(),2));
        this.rvMangaList.addItemDecoration(new DividerItemDecoration(rootView.getContext(),DividerItemDecoration.HORIZONTAL));

        return rootView;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch ((String) parent.getItemAtPosition(position)) {
            case "Any": recyclerAdapter.getFilter().filter("Any"); break;
            case "Manga": recyclerAdapter.getFilter().filter("Manga"); break;
            case "Light novel": recyclerAdapter.getFilter().filter("Light novel"); break;
        }
    }

    private void getMangas() {
        this.progressBar.setVisibility(View.VISIBLE);
        firestore.collection("mangas")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Manga> mangaList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                mangaList.add(document.toObject(Manga.class));
                            }
                            createRecyclerViewAdapter(mangaList);
                        } else {
                            System.out.println("Error getting documents: " +
                                    task.getException());
                        }
                    }
                });
    }

    private void createRecyclerViewAdapter (ArrayList<Manga> list) {
        this.recyclerAdapter = new AdapterLists(list,"MangasFragment");
        this.rvMangaList.setAdapter(recyclerAdapter);
        createSpinnerAdapter();
    }

    private void createSpinnerAdapter() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(),R.array.mangaType,R.layout.spinner_layout);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        this.progressBar.setVisibility(View.GONE);
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void onRefresh() {
        getMangas();
        swipeRefresh.setRefreshing(false);
    }
}
