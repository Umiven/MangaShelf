package com.pgv.david.mangalist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.request.RequestOptions.circleCropTransform;

public class ProfileFragment extends android.support.v4.app.Fragment implements OnCompleteListener<DocumentSnapshot>, OnSuccessListener<Uri>{
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private LinearLayout lytProfile;
    private LinearLayout progressBar;
    private ImageView avatar;
    private TextView tvWelcome;
    private TextView tvEmail;
    private TextView tvGender;
    private TextView tvAge;
    private PieChart mangasChart;
    private User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        this.lytProfile = rootView.findViewById(R.id.lytProfile);
        this.avatar = rootView.findViewById(R.id.imgProfileIcon);
        this.tvWelcome = rootView.findViewById(R.id.tvWelcome);
        this.tvEmail = rootView.findViewById(R.id.tvProfileEmail);
        this.tvGender = rootView.findViewById(R.id.tvProfileGender);
        this.tvAge = rootView.findViewById(R.id.tvProfileAge);
        this.mangasChart = rootView.findViewById(R.id.mangasChart);
        this.progressBar = rootView.findViewById(R.id.profileLoading);
        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        progressBar.setVisibility(View.VISIBLE);
        lytProfile.setVisibility(View.GONE);
        this.getUser();
        return rootView;
    }

    private void getUser() {
        DocumentReference docRef = firebaseFirestore.collection("users")
                .document(firebaseAuth.getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(this);
    }

    @Override
    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            this.currentUser =  document.toObject(User.class);
            getUserData();
        } else {
            Toast.makeText(getContext(), "Error loading user from server.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            lytProfile.setVisibility(View.VISIBLE);
        }
    }

    public void getUserData() {
        String welcome = "Welcome back " + currentUser.getNick() + "!";
        tvWelcome.setText(welcome);
        tvEmail.setText(currentUser.getEmail());
        tvGender.setText(currentUser.getGender());
        tvAge.setText(currentUser.getAge());
        loadAvatar();
        setChartStyle();
    }

    private void loadAvatar() {
        StorageReference storageRef = firebaseStorage.getReference();
        StorageReference databaseAvatarRef = storageRef.child("images/" + "avatars/" +
                firebaseAuth.getCurrentUser().getUid() + "/" + firebaseAuth.getCurrentUser().getUid() + "_avatar");
        databaseAvatarRef.getDownloadUrl().addOnSuccessListener(this);
        progressBar.setVisibility(View.GONE);
        lytProfile.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSuccess(Uri uri) {
        Glide.with(this)
                .load(uri)
                .apply(circleCropTransform())
                .into(avatar);
    }

    private void setChartStyle() {
        PieData pieData = setChartData();
        pieData.setValueFormatter(new MyValueFormatter());
        // Center pie
        mangasChart.setHoleRadius(0.f);
        mangasChart.setTransparentCircleRadius(0.f);
        // Desc
        Description description = mangasChart.getDescription();
        description.setText("");
        mangasChart.setDescription(description);
        // Legend
        Legend legend = mangasChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setXEntrySpace(12.0f);
        legend.setTextSize(14.0f);
        // Labels
        mangasChart.setDrawEntryLabels(false);
        // Set data
        mangasChart.setData(pieData);
        mangasChart.invalidate();
    }

    private PieData setChartData() {
        List<PieEntry> entries = new ArrayList<>();
        int nCompleted = currentUser.getCompleted().size();
        int nReading = currentUser.getReading().size();
        int nPlanToRead = currentUser.getPlanToRead().size();

        if (nCompleted > 0 && nReading == 0 && nPlanToRead == 0) {
            entries.add(new PieEntry(nCompleted,"Completed"));
        } else if (nCompleted == 0 && nReading > 0 && nPlanToRead == 0) {
            entries.add(new PieEntry(nReading,"Reading"));
        } else if (nCompleted == 0 && nReading == 0 && nPlanToRead > 0) {
            entries.add(new PieEntry(nPlanToRead,"Plan to read"));
        } else if (nCompleted > 0 && nReading > 0 && nPlanToRead == 0) {
            entries.add(new PieEntry(nCompleted,"Completed"));
            entries.add(new PieEntry(nReading,"Reading"));
        } else if (nCompleted > 0 && nReading == 0 && nPlanToRead > 0) {
            entries.add(new PieEntry(nCompleted,"Completed"));
            entries.add(new PieEntry(nPlanToRead,"Plan to read"));
        } else if (nCompleted == 0 && nReading > 0 && nPlanToRead > 0) {
            entries.add(new PieEntry(nReading,"Reading"));
            entries.add(new PieEntry(nPlanToRead,"Plan to read"));
        } else if (nCompleted > 0 && nReading > 0 && nPlanToRead > 0) {
            entries.add(new PieEntry(nCompleted,"Completed"));
            entries.add(new PieEntry(nReading,"Reading"));
            entries.add(new PieEntry(nPlanToRead,"Plan to read"));
        } else {
            mangasChart.setVisibility(View.GONE);
        }

        PieData pieData = new PieData();
        PieDataSet dataSet = new PieDataSet(entries,"");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(17.0f);
        pieData.setDataSet(dataSet);
        return pieData;
    }
}

class MyValueFormatter implements IValueFormatter {
    private DecimalFormat mFormat;

    public MyValueFormatter() {
        this.mFormat = new DecimalFormat("###,###,##0");
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return mFormat.format(value);
    }
}
