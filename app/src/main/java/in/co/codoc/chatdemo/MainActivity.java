package in.co.codoc.chatdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    EditText expert_idView;
    EditText report_idView;
    EditText user_idView;
    String report_id;
    String chatUrl = "http://52.55.139.178:3001";
    Socket mSocket;
    TextView textView;
    String user_id;
    String expert_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.rmessage);
        user_idView = (EditText) findViewById(R.id.user_id);
        expert_idView = (EditText) findViewById(R.id.expert_id);
        report_idView = (EditText) findViewById(R.id.report_id);
    }




    /*public  void connect(View v){
        try {
            mSocket = IO.socket(chatUrl);
        }catch (Exception e){
            e.printStackTrace();
        }
        mSocket.io().reconnectionAttempts(0);
        mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("private_message", onNewMessage);
        mSocket.on("login",onLogin);
        mSocket.connect();
    }*/
    public  void connect(View v){
        user_id = user_idView.getText().toString();
        expert_id = expert_idView.getText().toString();
        report_id = report_idView.getText().toString();
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("user_id",user_id);
        i.putExtra("expert_id",expert_id);
        i.putExtra("report_id",report_id);
        startActivity(i);

    }
}
