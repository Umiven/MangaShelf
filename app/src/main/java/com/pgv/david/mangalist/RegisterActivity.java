package com.pgv.david.mangalist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import static com.bumptech.glide.request.RequestOptions.circleCropTransform;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

public class RegisterActivity extends AppCompatActivity implements TextWatcher, CompoundButton.OnCheckedChangeListener{
    private TextInputLayout tilNick;
    private EditText etNick;
    private TextInputLayout tilUser;
    private EditText etUser;
    private TextInputLayout tilAge;
    private EditText etAge;
    private TextInputLayout tilPassword;
    private EditText etPassword;
    private TextView tvPasswordStrength;
    private EditText etRepeatPassword;
    private ProgressBar pbPasswordStrength;
    private CheckBox chkShowPassword;
    private ImageView imgRegisterIcon;
    private RadioGroup rgGender;
    private LinearLayout progressBar;
    private Uri localAvatarRef;
    private Zxcvbn passwordStrength;
    private Strength strengthLevel;
    private User user;
    private String pass;
    private static int GALLERY = 1;
    // Firebase
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Evita que el teclado aparezca nada mas entrar
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        // References
        this.tilNick = findViewById(R.id.tilRegisterNick);
        this.etNick = findViewById(R.id.etRegisterNick);
        this.tilUser = findViewById(R.id.tilRegisterUser);
        this.etUser = findViewById(R.id.etRegisterUser);
        this.tilAge = findViewById(R.id.tilRegisterAge);
        this.etAge = findViewById(R.id.etRegisterAge);
        this.tilPassword = findViewById(R.id.tilRegisterPassword);
        this.etPassword = findViewById(R.id.etRegisterPassword);
        this.tvPasswordStrength = findViewById(R.id.tvRegisterPasswordStrength);
        this.etRepeatPassword = findViewById(R.id.etRegisterRepeatPassword);
        this. pbPasswordStrength = findViewById(R.id.pbRegisterPasswordStrength);
        this.chkShowPassword = findViewById(R.id.chkRegisterShowPassword);
        this.imgRegisterIcon = findViewById(R.id.imgRegisterIcon);
        this.rgGender = findViewById(R.id.rgRegisterGender);
        this.progressBar = findViewById(R.id.registerOngoing);
        this.user = new User();
        // Password Strength
        this.passwordStrength = new Zxcvbn();
        strengthLevel = passwordStrength.measure("");
        etPassword.addTextChangedListener(this);
        // Checkbox
        chkShowPassword.setOnCheckedChangeListener(this);
        //Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        // Back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {}

    @Override
    public void afterTextChanged(Editable s)
    {
        String password = s.toString();

        strengthLevel = passwordStrength.measure(password);
        tvPasswordStrength.setVisibility(View.VISIBLE);
        switch (strengthLevel.getScore())
        {
            case 0:
                pbPasswordStrength.setProgress(0);
                tvPasswordStrength.setText(R.string.passwordStrengthPoor);
                break;
            case 1:
                pbPasswordStrength.setProgress(1);
                tvPasswordStrength.setText(R.string.passwordStrengthLow);
                break;
            case 2:
                pbPasswordStrength.setProgress(2);
                tvPasswordStrength.setText(R.string.passwordStrengthMedium);
                break;
            case 3:
                pbPasswordStrength.setProgress(3);
                tvPasswordStrength.setText(R.string.passwordStrengthStrong);
                break;
            case 4:
                pbPasswordStrength.setProgress(4);
                tvPasswordStrength.setText(R.string.passwordStrengthVeryStrong);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked)
        {
            etPassword.setTransformationMethod(null);
            etRepeatPassword.setTransformationMethod(null);
        } else
        {
            etPassword.setTransformationMethod(new PasswordTransformationMethod());
            etRepeatPassword.setTransformationMethod(new PasswordTransformationMethod());
        }
    }

    public void register(View view) {
        setGender();
        boolean isValidNick = isValidNick();
        boolean isValidAge = isValidAge();
        boolean isValidUser = isValidUser();
        boolean isValidPassword = isValidPassword();
        if (isValidNick && isValidUser
                && isValidPassword && isValidAge) {
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(user.getEmail(),this.pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this,"Error signin up user",Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    } else {
                        // Add user data to database
                        firebaseFirestore.collection("users").document(firebaseAuth.getCurrentUser().getUid()).set(user);
                        // Upload avatar image to storage
                        uploadAvatar();
                        // Register success
                        Toast.makeText(RegisterActivity.this,"User registered!",Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        firebaseAuth.signOut();
                        finish();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        overridePendingTransition(R.transition.transition_fade_in,R.transition.transition_fade_out);
                    }
                }
            });
        }
    }

    private void uploadAvatar() {
        if (localAvatarRef != null) {
            StorageReference storageRef = firebaseStorage.getReference();
            StorageReference databaseAvatarRef = storageRef.child("images/" + "avatars/" +
                    firebaseAuth.getCurrentUser().getUid() + "/" + firebaseAuth.getCurrentUser().getUid() + "_avatar");
            databaseAvatarRef.putFile(localAvatarRef);
        }
    }

    private void setGender() {
        int selectedGender = rgGender.getCheckedRadioButtonId();
        RadioButton gender = findViewById(selectedGender);
        user.setGender(gender.getText().toString());
    }

    private boolean isValidNick()
    {
        String nick = etNick.getText().toString();
        if (nick.equals(""))
        {
            tilNick.setErrorEnabled(true);
            tilNick.setError("Please input a nick");
            return false;
        } else
        {
            tilNick.setErrorEnabled(false);
            user.setNick(nick);
            return true;
        }
    }

    private boolean isValidAge() {
        String age = etAge.getText().toString();
        if (age.equals("")) {
            tilAge.setErrorEnabled(true);
            tilAge.setError("Please input an age");
            return false;
        } else {
            if (Integer.parseInt(age) > 99 || Integer.parseInt(age) < 1) {
                tilAge.setErrorEnabled(true);
                tilAge.setError("Please input an age between 1 and 99");
                return false;
            } else {
                tilAge.setErrorEnabled(false);
                user.setAge(age);
                return true;
            }
        }
    }

    private boolean isValidUser()
    {
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(etUser.getText()).matches())
        {
            tilUser.setErrorEnabled(false);
            this.user.setEmail(etUser.getText().toString());
            return true;
        } else
        {
            tilUser.setErrorEnabled(false);
            tilUser.setError("Email not valid");
            return false;
        }
    }

    private boolean isValidPassword()
    {
        String pass = etPassword.getText().toString();
        String repeatPass = etRepeatPassword.getText().toString();

        if (pass.equals(repeatPass))
        {
            if (strengthLevel.getScore() < 2)
            {
                tilPassword.setErrorEnabled(true);
                tilPassword.setError("Password strength too low");
                return false;
            } else
            {
                tilPassword.setErrorEnabled(false);
                this.pass = pass;
                return true;
            }

        } else
        {
            tilPassword.setErrorEnabled(true);
            tilPassword.setError("Passwords do not match");
            return false;
        }
    }

    public void loadImage(View view) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode ,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == GALLERY) {
            if (data != null) {
                localAvatarRef = data.getData();
                Glide.with(this)
                        .load(localAvatarRef)
                        .apply(circleCropTransform())
                        .into(this.imgRegisterIcon);
            }
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
