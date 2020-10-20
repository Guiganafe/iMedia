package com.example.imedia.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imedia.Firebase.FirebaseService;
import com.example.imedia.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LogIn extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputLayout edt_email, edt_senha;
    private Button btn_continuar, btn_loginApple, btn_loginGoogle;
    private TextView tv_cadastro;
    private String userID;


    private static final int RC_SIGN_IN = 101;
    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    /**
     * Instancia do firebase
     */
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        /**
         * Conexão com os componentes de layout
         */
        inicializarComponentes();

        /**
         * Gerencia todos os cliques da tela
         */
        onClick();

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth = FirebaseService.getFirebaseAuth();
    }

    private void onClick() {
        tv_cadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cadastroIntent = new Intent(LogIn.this, CreateAccount.class);
                startActivity(cadastroIntent);
                finish();
            }
        });

        btn_continuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logarUsuario();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btn_loginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            final FirebaseUser userGoogle = firebaseAuth.getCurrentUser();

                            userID = FirebaseService.getFirebaseAuth().getCurrentUser().getUid();
                            db.collection("users").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()){
                                        Toast.makeText(LogIn.this, "Login realizado com sucesso", Toast.LENGTH_LONG).show();
                                    }else{
                                        userID = FirebaseService.getFirebaseAuth().getCurrentUser().getUid();
                                        DocumentReference documentReference = db.collection("users").document(userID);
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("email", userGoogle.getEmail());
                                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "onSuccess: usuário salvo no firestore: " + userID);
                                            }
                                        });
                                    }
                                }
                            });

                            updateUI(userGoogle);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        Toast.makeText(LogIn.this, "Login realizado com sucesso", Toast.LENGTH_LONG).show();
        Intent completarPerfilIntent = new Intent(LogIn.this, MainActivity.class);
        startActivity(completarPerfilIntent);
        finish();
    }


    private void logarUsuario() {
        String email, senha;

        email = edt_email.getEditText().getText().toString();
        senha = edt_senha.getEditText().getText().toString();

        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(senha)) {
            edt_email.setError("Insira um e-mail");
            edt_senha.setError("Insira uma senha");
            return;
        }else if(TextUtils.isEmpty(email)){
            edt_email.setError("Insira um e-mail");
            return;
        }else if(TextUtils.isEmpty(senha)){
            edt_senha.setError("Insira uma senha");
            return;
        }

        //progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(LogIn.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(LogIn.this, "Login realizado com sucesso", Toast.LENGTH_SHORT).show();
                    Intent completarPerfilIntent = new Intent(LogIn.this, MainActivity.class);
                    startActivity(completarPerfilIntent);
                    finish();
                }else{
                    Toast.makeText(LogIn.this, "Falha no login", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void inicializarComponentes() {
        edt_email = (TextInputLayout) findViewById(R.id.edt_email);
        edt_senha = (TextInputLayout) findViewById(R.id.edt_senha);
        btn_continuar = (Button) findViewById(R.id.btn_continuar);
        btn_loginGoogle = (Button) findViewById(R.id.btn_loginGoogle);
        tv_cadastro = (TextView) findViewById(R.id.tv_cadastro);
    }
}