package com.pgv.david.mangalist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Distribution;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CompletedFragment extends android.support.v4.app.Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView rvCompletedMangaList;
    private AdapterLists recyclerAdapter;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private TextView placeholder;
    private LinearLayout progressBar;
    private ArrayList<String> mangasIds;
    private SwipeRefreshLayout refreshSwipe;

    public CompletedFragment() {}

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getUser();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,new IntentFilter("refreshCompleted"));
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_completed, container, false);

        this.progressBar = rootView.findViewById(R.id.completedLoading);

        this.refreshSwipe = rootView.findViewById(R.id.swipeRefreshCompleted);
        refreshSwipe.setOnRefreshListener(this);
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.mangasIds = new ArrayList<>();

        this.placeholder = rootView.findViewById(R.id.completedPlaceholder);
        this.rvCompletedMangaList = rootView.findViewById(R.id.rvCompletedMangasLayout);
        this.rvCompletedMangaList.hasFixedSize();
        getUser();
        this.rvCompletedMangaList.setLayoutManager(new GridLayoutManager(rootView.getContext(),2));
        this.rvCompletedMangaList.addItemDecoration(new DividerItemDecoration(rootView.getContext(),DividerItemDecoration.HORIZONTAL));
        return rootView;
    }

    private void getUser() {
        this.progressBar.setVisibility(View.VISIBLE);
        firebaseFirestore.collection("users")
                .document(firebaseAuth.getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    User user = document.toObject(User.class);
                    if (user.getCompleted().size() > 0) {
                        placeholder.setVisibility(View.GONE);
                    } else {
                        placeholder.setVisibility(View.VISIBLE);
                    }
                    mangasIds = user.getCompleted();
                    getMangas();
                }
            }
        });
    }

    private void getMangas() {
        firebaseFirestore.collection("mangas")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Manga> mangaList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                mangaList.add(document.toObject(Manga.class));
                            }
                            selectMangasInList(mangaList);
                        } else {
                            System.out.println("Error getting documents: " +
                                    task.getException());
                        }
                    }
                });
    }

    private void selectMangasInList(ArrayList<Manga> mangaList) {
        ArrayList<Manga> mangasToAdd = new ArrayList<>();
        for (Manga manga : mangaList) {
            if (this.mangasIds.contains(manga.getId())) {
                mangasToAdd.add(manga);
            }
        }
        createRecyclerViewAdapter(mangasToAdd);
    }

    private void createRecyclerViewAdapter (ArrayList<Manga> mangasToAdd) {
        this.recyclerAdapter = new AdapterLists(mangasToAdd,"CompletedFragment");
        recyclerAdapter.getFilter().filter("Any");
        this.rvCompletedMangaList.setAdapter(recyclerAdapter);
        this.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        getUser();
        refreshSwipe.setRefreshing(false);
    }
}
