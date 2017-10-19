package shadow.homeauto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";

    private Button buttonPin11,buttonPin12,buttonPin13;
    private EditText editTextIPAddress,editTextPortNumber;

    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        buttonPin11 = (Button)findViewById(R.id.buttonPin11);
        buttonPin12 = (Button)findViewById(R.id.buttonPin12);
        buttonPin13 = (Button)findViewById(R.id.buttonPin13);

        editTextIPAddress = (EditText)findViewById(R.id.editTextIPAddress);
        editTextPortNumber = (EditText)findViewById(R.id.editTextPortNumber);

        buttonPin11.setOnClickListener(this);
        buttonPin12.setOnClickListener(this);
        buttonPin13.setOnClickListener(this);

        editTextIPAddress.setText(sharedPreferences.getString(PREF_IP,""));
        editTextPortNumber.setText(sharedPreferences.getString(PREF_PORT,""));
    }

    @Override
    public void onClick(View view) {

        String parameterValue = "";
        String ipAddress = editTextIPAddress.getText().toString().trim();
        String portNumber = editTextPortNumber.getText().toString().trim();

        editor.putString(PREF_IP,ipAddress);
        editor.putString(PREF_PORT,portNumber);
        editor.commit();

        if(view.getId()==buttonPin11.getId())
        {
            parameterValue = "11";
        }
        else if(view.getId()==buttonPin12.getId())
        {
            parameterValue = "12";
        }
        else
        {
            parameterValue = "13";
        }

        if(ipAddress.length()>0 && portNumber.length()>0){
            new HttpRequestAsyncTask(
                    view.getContext(),parameterValue,ipAddress,portNumber,"pin"
            ).execute();
        }
    }

    public String sendRequest(String parameterValue, String ipAddress, String portNumber, String parameterName){
        String serverResponse = "ERROR";

        try{

            HttpClient httpclient = new DefaultHttpClient();
            URI website = new URI("http://"+ipAddress+":"+portNumber+"/?"+parameterName+"="+parameterValue);
            HttpGet getRequest = new HttpGet();
            getRequest.setURI(website);
            HttpResponse response = httpclient.execute(getRequest);

            InputStream content = null;
            content = response.getEntity().getContent();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    content
            ));
            serverResponse = in.readLine();
            content.close();
        }catch (ClientProtocolException e){
            serverResponse = e.getMessage();
            e.printStackTrace();
        }catch (IOException e){
            serverResponse = e.getMessage();
            e.printStackTrace();
        }catch (URISyntaxException e){
            serverResponse = e.getMessage();
            e.printStackTrace();
        }

        return serverResponse;
    }

    private class HttpRequestAsyncTask extends AsyncTask<Void,Void,Void>{

        private String requestReply,ipAddress, portNumber;
        private Context context;
        private AlertDialog alertDialog;
        private String parameter;
        private String parameterValue;

        public HttpRequestAsyncTask(Context context, String parameterValue, String ipAddress, String portNumber, String parameter){
            this.context = context;

            alertDialog = new AlertDialog.Builder(this.context)
                    .setTitle("HTTP Response From IP address")
                    .setCancelable(true)
                    .create();

            this.ipAddress = ipAddress;
            this.parameterValue = parameterValue;
            this.portNumber = portNumber;
            this.parameter = parameter;

        }

        @Override
        protected Void doInBackground(Void... voids) {
            alertDialog.setMessage("Data sent, waiting for reply from server...");
            if(!alertDialog.isShowing())
            {
                alertDialog.show();
            }
            requestReply = sendRequest(parameterValue,ipAddress,portNumber,parameter);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            alertDialog.setMessage(requestReply);
            if(!alertDialog.isShowing()){
                alertDialog.show();
            }
        }

        @Override
        protected void onPreExecute() {
            alertDialog.setMessage("Sending Data to server... please wait");
            if(!alertDialog.isShowing()){
                alertDialog.show();
            }
        }
    }
}
