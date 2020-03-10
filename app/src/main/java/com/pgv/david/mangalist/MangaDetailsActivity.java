package com.pgv.david.mangalist;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MangaDetailsActivity extends AppCompatActivity implements OnCompleteListener<DocumentSnapshot> {
    private Manga manga;
    private String fragment;
    private ImageView cover;
    private TextView author;
    private TextView status;
    private TextView type;
    private TextView genres;
    private TextView synopsis;
    private ProgressBar progressBar;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String mangaIsInList;
    private User currentUser;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manga_details);
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
        // ActionBar
        this.actionBar = getSupportActionBar();
        this.actionBar.setDisplayHomeAsUpEnabled(true);

        this.mangaIsInList = null;
        this.author = findViewById(R.id.tvDetailsAuthor);
        this.status = findViewById(R.id.tvDetailsStatus);
        this.type = findViewById(R.id.tvDetailsType);
        this.genres = findViewById(R.id.tvDetailsGenres);
        this.synopsis = findViewById(R.id.tvDetailsSynopsis);
        this.cover = findViewById(R.id.mangaDetailsImg);
        this.progressBar = findViewById(R.id.progressBarOperations);

        getMangaInfo();
        setMangaInfo();
    }

    /*
     * Si se viene del fragmento de las listar, se
     * muestra el boton de editar.
     * Si se viene del fragmento que muestra todos
     * los mangas de la base de datos, se
     * muestra el boton de añadir.
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (fragment.equals("MangasFragment")) {
            getMenuInflater().inflate(R.menu.menu_add_manga,menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_edit_manga,menu);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_manga:
                addMangaToList();
                return true;
            case R.id.menu_edit_manga:
                editManga();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getMangaInfo () {
        Intent intent = getIntent();
        this.fragment = intent.getStringExtra("fragment");
        this.manga = (Manga) intent.getSerializableExtra("manga");
    }

    private void setMangaInfo () {
        Glide.with(MangaDetailsActivity.this)
                .load(manga.getCover())
                .into(this.cover);
        this.actionBar.setTitle(manga.getTitle());
        this.author.setText(manga.getAuthor());
        this.status.setText(manga.getStatus());
        this.type.setText(manga.getType());
        ArrayList<String> genresList = manga.getGenres();
        String genres = "";
        for (String genre : genresList) {
            genres += genre + " ";
        }
        genres = genres.trim();
        genres = genres.replaceAll("\\s+",", ");
        this.genres.setText(genres);
        this.synopsis.setText(manga.getSynopsis());
    }

    /*
    * Muestra Dialog que te pregunta a qué lista quieres
    * añadir el manga.
    * */
    public void addMangaToList() {
        getUser();
        final String[] lists = {"Completed","Reading","Plan to read"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add manga to:");
        builder.setItems(lists, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mangaIsInList == null) {
                    switch (lists[which]) {
                        case "Completed":
                            addMangaTo("Completed");
                            break;
                        case "Reading":
                            addMangaTo("Reading");
                            break;
                        case "Plan to read":
                            addMangaTo("Plan to read");
                            break;
                    }
                } else {
                    Toast.makeText(MangaDetailsActivity.this, "Manga already in list '" + mangaIsInList + "'.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void getUser() {
        firebaseFirestore.collection("users")
                .document(firebaseAuth.getCurrentUser().getUid())
                .get().addOnCompleteListener(this);
    }

    @Override
    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            this.currentUser =  document.toObject(User.class);
            checkManga(currentUser);
        }
    }

    /*
    * Si el manga que tenemos seleccionado se encuentra
    * en alguna de las listas del usuario, guardamos el nombre
    * de dicha lista en una variable.
    * */
    private void checkManga(User user) {
        if(isMangaInList(user.getPlanToRead())) {
            this.mangaIsInList = "Plan to read";
        } else if(isMangaInList(user.getReading())) {
            this.mangaIsInList = "Reading";
        } else if(isMangaInList(user.getCompleted())) {
            this.mangaIsInList = "Completed";
        } else {
            this.mangaIsInList = null;
        }
    }

    private boolean isMangaInList(ArrayList<String> list) {
        for(String mangaList : list) {
            if (mangaList.equals(manga.getId())) return true;
        }
        return false;
    }

    private void addMangaTo(String list) {
        progressBar.setVisibility(View.VISIBLE);
        switch(list) {
            case "Completed":
                currentUser.getCompleted().add(manga.getId());
                break;
            case "Reading":
                currentUser.getReading().add(manga.getId());
                break;
            case "Plan to read":
                currentUser.getPlanToRead().add(manga.getId());
                break;
        }
        uploadUser(currentUser);
    }

    private void uploadUser(User user) {
        firebaseFirestore.collection("users").document(firebaseAuth.getCurrentUser().getUid()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MangaDetailsActivity.this, "Operation completed successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MangaDetailsActivity.this, "Oops, error communicating with server", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /*
    * Dialog que pregunta si quieremos cambiar
    * el manga de lista, o borrarlo de la actual.
    * */
    public void editManga () {
        getUser();
        final String[] lists = {"Switch manga to another list","Delete manga from this list"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(lists, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (lists[which]) {
                    case "Switch manga to another list":
                        deleteManga();
                        switchManga();
                        break;
                    case "Delete manga from this list":
                        deleteManga();
                        uploadUser(currentUser);
                        refreshRecycler(mangaIsInList);
                        finish();
                        break;
                }
            }
        });
        builder.show();
    }

    /*
    * Si el manga se ha borrado o cambiado de lista,
    * actualizamos el RecyclerView correspondiente.
    * */
    private void refreshRecycler(String recyclerToRefresh) {
        switch (recyclerToRefresh) {
            case "Completed":
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("refreshCompleted"));
                break;
            case "Reading":
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("refreshReading"));
                break;
            case "Plan to read":
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("refreshPlanToRead"));
                break;
        }
    }
    
    private void switchManga() {
        final String[] lists = {"Completed","Reading","Plan to read"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Switch manga to:");
        builder.setItems(lists, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (lists[which]) {
                    case "Completed":
                        addMangaTo("Completed");
                        break;
                    case "Reading":
                        addMangaTo("Reading");
                        break;
                    case "Plan to read":
                        addMangaTo("Plan to read");
                        break;
                }
                refreshRecycler(mangaIsInList);
                refreshRecycler(lists[which]);
            }
        });
        builder.show();
    }
    
    private void deleteManga() {
        switch (mangaIsInList) {
            case "Plan to read":
                currentUser.getPlanToRead().remove(manga.getId());
                break;
            case "Reading":
                currentUser.getReading().remove(manga.getId());
                break;
            case "Completed":
                currentUser.getCompleted().remove(manga.getId());
                break;
        }
    }

    /*
    * Flecha ActionBar
    * */
    @Override
    public boolean onSupportNavigateUp() {
        this.finish();
        return true;
    }
}
