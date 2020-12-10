package com.example.lab13pushnotifications.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.example.lab13pushnotifications.Model.Person;
import com.example.lab13pushnotifications.R;
import com.example.lab13pushnotifications.app.Config;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //variable para guardar valor de variables
    final String ALERT_STATE = "alert_state";
    final String MESSAGE_ALERT = "message_alert";
    final String TITLE_ALERT = "title_alert";

    //variables de entorno
    private boolean isAlertDisplayed = false;
    private String messageAlert = "";
    private String titleAlert = "";

    EditText edtName;
    EditText edtAge;
    TextView tvQuantity;
    Button btnSavePerson;

    DatabaseReference mDatabase;

    Person person;

    int id = 0;

    AwesomeValidation validation;

    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        person = new Person();

        edtName = (EditText)findViewById(R.id.edtName);
        edtAge = (EditText)findViewById(R.id.edtAge);
        btnSavePerson = (Button)findViewById(R.id.btnSavePerson);
        tvQuantity = (TextView)findViewById(R.id.tvQuantity);

        progress = new ProgressDialog(this);
        progress.setCancelable(false);

        if (savedInstanceState != null) {
            isAlertDisplayed = savedInstanceState.getBoolean(ALERT_STATE);
            messageAlert = savedInstanceState.getString(MESSAGE_ALERT);
            titleAlert = savedInstanceState.getString(TITLE_ALERT);

            if (isAlertDisplayed){
                showAlert(messageAlert,titleAlert);
            }
        }

        validation = new AwesomeValidation(ValidationStyle.COLORATION);
        validation.addValidation(this,R.id.edtName, RegexTemplate.NOT_EMPTY,R.string.errorEmptyName);
        validation.addValidation(this,R.id.edtAge, RegexTemplate.NOT_EMPTY,R.string.errorEmptyAger);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Person");

        FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

        btnSavePerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validation.validate()){
                    person.setName(edtName.getText().toString());
                    person.setAge(Integer.parseInt(edtAge.getText().toString()));
                    mDatabase.child(String.valueOf(id+1)).setValue(person).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            notifyTopic();
                            hideProgress();
                            showAlert("Registro exitoso","Aviso");
                            limpiarInputs();
                        }
                    });
                }
            }
        });

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    id = (int) snapshot.getChildrenCount();
                    tvQuantity.setText(getString(R.string.textQuantity) + id);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void limpiarInputs() {
        edtName.setText("");
        edtAge.setText("");
    }

    private void notifyTopic() {
        RequestQueue myReqyest = Volley.newRequestQueue(getApplicationContext());
        JSONObject json = new JSONObject();

        final String message = getString(R.string.message);

        try {
            json.put("to","/topics/" + Config.TOPIC_GLOBAL);
            JSONObject notification = new JSONObject();
            notification.put(Config.TITLE,getString(R.string.newRegister));
            notification.put(Config.DETAIL,edtName.getText().toString() + " " + message);

            json.put("data", notification);
            String URL = Config.URL;
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,URL,json,null,null){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String,String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key="+Config.TOKEN);
                    return header;
                }
            };
            myReqyest.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void hideProgress(){
        if(progress.isShowing()){
            progress.dismiss();
        }
    }

    public void showProgress(String mensaje){
        progress.setMessage(mensaje);
        progress.show();
    }

    private void showAlert(String message, String title) {
        isAlertDisplayed =true;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setCancelable(false);

        messageAlert = message;
        titleAlert = title;

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isAlertDisplayed=false;
            }
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }
}