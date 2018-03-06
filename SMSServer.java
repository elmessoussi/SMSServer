package com.nomDeVotreDomaine.SMSServer;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class SMSServer extends AppCompatActivity {

    // global variable
    private Button sendSMSButton;
    private TextView numberOfMessages;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_SMSServer);

        sendSMSButton = findViewById(R.id.sendSMSButton);
        numberOfMessages = findViewById(R.id.NumberMessages);
        progressBar = findViewById(R.id.progressBar);

        View.OnClickListener onClickListener = new View.OnClickListener(){
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @RequiresApi(api = Build.VERSION_CODES.DONUT)
            @Override
            public void onClick(View view) {

                sendSMSButton.setClickable(false);
                Context context = getApplicationContext();
                Toast.makeText(context,"Envoi des SMS en Cours",Toast.LENGTH_LONG).show();
                DummyTask siginActivity= new DummyTask();
                siginActivity.execute();
                sendSMSButton.setClickable(true);
                //System.out.println(responseStr);
            }
        };

        sendSMSButton.setOnClickListener(onClickListener);

    }

    private class DummyTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = getApplicationContext();
            String link = "http://www.nomDeVotreDomaine.com/smsList.php";  // link to the php page producing the list of sms to send in json format
            StringBuilder sb = null;
            try{
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                sb = new StringBuilder();
                String line;

                // Read Server Response
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }
                JSONArray obj = null;
                try {
                    obj = new JSONArray(sb.toString().substring(3));
                    String numTel = new String();
                    String TextMsg = new String();
                    for (int i = 0; i < obj.length(); i++)
                    {
                        numTel = obj.getJSONObject(i).getString("numPhone");
                        TextMsg = obj.getJSONObject(i).getString("text");
                        SmsManager smsManager = SmsManager.getDefault();
                        String SENT = "SMS_SENT";
                        String DELIVERED = "SMS_DELIVERED";
                        ArrayList<String> parts =smsManager.divideMessage(TextMsg);
                        int numParts = parts.size();

                        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                        ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();

                        for (int j = 0; j < numParts; j++) {
                            sentIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0));
                            deliveryIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0));
                        }
                        smsManager.sendMultipartTextMessage(numTel, null, parts, sentIntents, deliveryIntents);
                        updateView("Envoi en cours du message : "+i+1,i,obj.length());

                    }
                    updateView("Fin d'envoi : Total = "+obj.length(),1,1);

                } catch (JSONException e) {
                    updateView("Aucun message trouvé",1,1);
                }
            } catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

        private void updateView(final String value, final int numCourant, final int nbrTotal){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    numberOfMessages.setText(value);
                    progressBar.setProgress(Math.round(((float)numCourant/nbrTotal)*100));
                }
            });
        }

    }
}
