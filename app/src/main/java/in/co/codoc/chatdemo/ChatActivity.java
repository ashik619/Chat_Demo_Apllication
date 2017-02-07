package in.co.codoc.chatdemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.ybq.android.spinkit.SpinKitView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    String report_id;
    String expert_id;
    String user_id;
    Socket mSocket;
    String chatUrl = "http://52.55.139.178:3001";
    ListView listView;
    EditText sendMsgView;
    String sendMsg;
    ArrayList<Message> messages;
    UsersAdapter adapter;
    String filePath = "/storage/emulated/0/WhatsApp/Media/WhatsApp Images/IMG-20170201-WA0003.jpg";
    boolean textMsgFlag = false;
    boolean captureFlag;
    final CharSequence[] AdbItems = {
            "Gallery", "Camera",
    };
    private static final int RESULT_LOAD_IMG = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    Uri selectedImage;
    String fname;
    boolean cropFlag;
    String root = Environment.getExternalStorageDirectory().toString();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        sendMsgView = (EditText) findViewById(R.id.message);
        getIntentData();
        initiateSocket();
    }
    void initiateSocket(){
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
        mSocket.connect();


    }
    void getIntentData(){
        Intent i = getIntent();
        report_id = i.getStringExtra("report_id");
        expert_id = i.getStringExtra("expert_id");
        user_id = i.getStringExtra("user_id");
        cropFlag = i.getBooleanExtra("cropped",false);
        if(cropFlag){
            filePath = i.getStringExtra("file");
        }

    }
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ChatActivity.this,
                            "Connected to chat server", Toast.LENGTH_SHORT).show();
                    System.out.println("fuck id"+mSocket.id());
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("user_id", user_id);
                        jsonObject.put("expert_id", expert_id);
                        jsonObject.put("report_id", report_id);
                        mSocket.emit("login", jsonObject, new Ack() {
                            @Override
                            public void call(final Object... args1) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        System.out.println("login ack ");
                                        JSONArray data = (JSONArray) args1[0];
                                        populateMessages(data);
                                    }});
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ChatActivity.this,
                            "DisConnected to chat server", Toast.LENGTH_SHORT).show();
                    //initiateSocket();
                }
            });
        }
    };
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ChatActivity.this,
                            "Error Connectting to chat server", Toast.LENGTH_SHORT).show();
                    //initiateSocket();
                }
            });
        }
    };
    public class UsersAdapter extends ArrayAdapter<Message> {
        public UsersAdapter(Context context, ArrayList<Message> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final Message message = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_item, parent, false);
            }
            // Lookup view for data population
            final LinearLayout background = (LinearLayout) convertView.findViewById(R.id.bg);
            final TextView messageView = (TextView) convertView.findViewById(R.id.message);
            final ImageView imageView = (ImageView)  convertView.findViewById(R.id.image);
            final SpinKitView  loader = (SpinKitView) convertView.findViewById(R.id.spin_kit);
            final TextView timeView = (TextView) convertView.findViewById(R.id.time);
            loader.setVisibility(View.GONE);
            // final ImageView otherProfile = (ImageView)  convertView.findViewById(R.id.docImage);
            String from = message.from;
            String to = message.to;
            String msg = message.msg;
            String trimmedImageUrl = msg.replaceAll(Pattern.quote("\\"),"");
            int type = message.type;

            if(from.matches(user_id)){
                background.setBackgroundResource(R.drawable.message_bg_right);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                //layoutParams.setMarginStart(150);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                background.setLayoutParams(layoutParams);
            }else {
                background.setBackgroundResource(R.drawable.message_bg_left);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                //layoutParams.setMarginEnd(150);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                background.setLayoutParams(layoutParams);
            }
            if(type == 0) {
                Long timeStp = Long.valueOf(message.timeStamp);
                String time = TimeSpentManager.setTimeAgo(ChatActivity.this,timeStp);
                messageView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                messageView.setText(msg);
                timeView.setText(time);
            }else if(type == 1){
                Long timeStp = Long.valueOf(message.timeStamp);
                String time = TimeSpentManager.setTimeAgo(ChatActivity.this,timeStp);
                imageView.setVisibility(View.VISIBLE);
                messageView.setVisibility(View.GONE);
                timeView.setText(time);
                Glide.with(ChatActivity.this)
                        .load(trimmedImageUrl)
                        .into(imageView);
            }else if(type == 8){
                loader.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                messageView.setVisibility(View.GONE);
            }
            background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                }
            });
            return convertView;
        }
    }
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    sendMsgView.setText("", TextView.BufferType.EDITABLE);
                    JSONObject data = (JSONObject) args[0];
                    System.out.println("new msg" + data.toString());
                    appendMessage(data);
                }
            });
        }
    };
    void populateMessages(JSONArray jsonArray){
        listView = (ListView) findViewById(R.id.message_list);
        messages = Message.fromJson(jsonArray);
        adapter = new UsersAdapter(this, messages);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        if(cropFlag){
            //uploadmage();
            //appendLoader();
        }

    }
    void appendMessage(JSONObject message){
        Message message1 = new Message(message);
        messages.add(message1);
        adapter.notifyDataSetChanged();
    }
    void appendLoader(){
        JSONObject loader = new JSONObject();
        try {
            loader.put("type",8);
            loader.put("to_id",expert_id);
            loader.put("from_id",user_id);
            loader.put("msg","xyz");
            loader.put("created_mili","617191");
            Message message1 = new Message(loader);
            messages.add(message1);
            adapter.notifyDataSetChanged();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    void removeLoader(){
        int count = adapter.getCount();
        adapter.remove(adapter.getItem(count - 1));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
       /*mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("new message", onNewMessage);*/
    }
    public  void send(View v){
        sendMsg = sendMsgView.getText().toString();
        if(!sendMsg.matches("")) {
            try {
                JSONObject message = new JSONObject();
                message.put("report_id", report_id);
                message.put("expert_id", expert_id);
                message.put("user_id",user_id);
                message.put("msg", sendMsg);
                textMsgFlag = true;
                mSocket.emit("private_message", message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void image(View v){
        showDialog();
    }
    void showDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.attachment_dialog);
        dialog.show();
        LinearLayout gallery = (LinearLayout) dialog.findViewById(R.id.gallery);
        LinearLayout camera = (LinearLayout) dialog.findViewById(R.id.camera);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                activeGallery();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                activeTakePhoto();
            }
        });
    }

    void showADBdialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make your selection");
        builder.setItems(AdbItems, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
                if(item==0){
                    activeGallery();
                }
                else if(item == 1)
                {
                    activeTakePhoto();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void activeTakePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }
    private void activeGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }
    @Override protected void onActivityResult(int requestCode, int resultCode,
                                              Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_LOAD_IMG:
                if (requestCode == RESULT_LOAD_IMG &&
                        resultCode == RESULT_OK && null != data) {
                    selectedImage = data.getData();
                    captureFlag = false;
                    resize(selectedImage);
                    System.out.println("file "+fname);
                    uploadmage();
                    appendLoader();
                    //startCrop();
                }
            case REQUEST_IMAGE_CAPTURE:
                if (requestCode == REQUEST_IMAGE_CAPTURE &&
                        resultCode == RESULT_OK) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    captureFlag = true;
                    saveImgFile(photo);
                    uploadmage();
                    appendLoader();
                    //startCrop();
                }
        }
    }
    void resize(Uri image){
        filePath = ImageFilePath.getPath(this,image);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inJustDecodeBounds = true;
        final int height = options.outHeight;
        final int width = options.outWidth;
        int reqWidth;
        int reqHeight;
        int reqSize;
        reqSize = 200;
        if(width>height){
            reqWidth = reqSize;
            float hw = (float)height/width;
            float newH = hw*reqSize;
            reqHeight = Math.round(newH);
        }else {
            reqHeight = reqSize;
            float wh = (float)width/height;
            float newW = wh*reqSize;
            reqWidth = Math.round(newW);
        }
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap bp =  BitmapFactory.decodeFile(filePath, options);
        saveImgFile(bp);

    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }
        return inSampleSize;
    }

    void saveImgFile(Bitmap bm ){
        File myDir = new File(root + "/Rhythmia");
        myDir.mkdirs();
        fname = "image" +createRandomString()+".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {

        }
    }
    String createRandomString(){
        Long time= System.currentTimeMillis();
        String timehex = Long.toHexString(time);
        return timehex;
    }

    public void startCrop(){
        Intent i = new Intent(getApplicationContext(), ChatCropActivity.class);
        if(captureFlag){
            i.putExtra("file",fname);
        }else {
            i.putExtra("file",selectedImage.toString());
        }
        i.putExtra("flag",!captureFlag);
        i.putExtra("user_id",user_id);
        i.putExtra("expert_id",expert_id);
        i.putExtra("report_id",report_id);
        startActivity(i);
    }
    void uploadmage(){
        FileUploadService service =
                ServiceGenerator.createService(FileUploadService.class);
        MultipartBody.Part body;
        File file;
        File myDir = new File(root + "/Rhythmia");
        file = new File(myDir, fname);
        RequestBody expert_idBody =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), expert_id);
        RequestBody user_idBody =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), user_id);
        RequestBody report_idbody =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), report_id);
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);
        body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        Call<ResponseBody> profilePicCall = service.upload(expert_idBody,user_idBody,report_idbody,body);
        profilePicCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                removeLoader();
                try {
                    System.out.println("respo"+response.body().string());
                    Toast.makeText(getApplicationContext(), "Image Sent", Toast.LENGTH_SHORT).show();

                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Image not Sent", Toast.LENGTH_SHORT).show();
                Log.e("Upload error:", t.getMessage());
                removeLoader();
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }
}
