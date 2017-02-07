package in.co.codoc.chatdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ChatCropActivity extends AppCompatActivity {
    CropImageView cropImageView;
    String filename;
    File myDir;
    String fname = null;
    Bitmap bp;
    Bitmap cropped;
    Bitmap resized;
    Button crop;
    Button next;
    ImageView imageView;
    Boolean cropFlag = false;
    Boolean updateFlag;
    String user_id = null;
    String expert_id = null;
    String report_id = null;
    boolean flag;
    String root = Environment.getExternalStorageDirectory().toString();
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_chat_crop);
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        imageView = (ImageView) findViewById(R.id.imageView);
        cropImageView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        crop = (Button) findViewById(R.id.crop);
        next = (Button) findViewById(R.id.next);
        getintent();
        imageView.setImageBitmap(bp);//
        //imageView.setImageURI(imageUri);
        crop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                crop();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                next();

            }
        });
    }
    void getintent(){
        Intent i = getIntent();
        filename = i.getStringExtra("file");
        user_id = i.getStringExtra("user_id");
        report_id = i.getStringExtra("report_id");
        expert_id = i.getStringExtra("expert_id");
        flag = i.getBooleanExtra("flag",false);
        try {
            bp = decodeSampledBitmapFromUri(this,filename,flag);
        }catch (Exception e){
            e.printStackTrace();
            finish();
        }
    }
    public  Bitmap decodeSampledBitmapFromUri(Context context, String filename, boolean flag) throws FileNotFoundException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        String filePath;
        if(!flag) {
            File image = new File(root + "/Rhythmia", filename);
            filePath =  image.getAbsolutePath();
        }else {
            Uri imageUri = Uri.parse(filename);
            filePath = ImageFilePath.getPath(context,imageUri);
            System.out.println("filepath: "+filePath);
        }
        //File image = new File(ImageFilePath.getPath(context,imageUri));
        System.out.println(filePath);
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
        return BitmapFactory.decodeFile(filePath, options);
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
    void crop(){
        if(cropFlag){
            cropImageView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            crop.setText("Crop");
            cropFlag = false;
        }else {
            imageView.setVisibility(View.GONE);
            cropImageView.setVisibility(View.VISIBLE);
            cropImageView.setImageBitmap(bp);
            crop.setText("Cancel");
            cropFlag = true;
        }
    }
    void next(){
        if (cropFlag) {
            cropped = cropImageView.getCroppedImage();
            saveImgFile(cropped);
        } else {
            saveImgFile(bp);
        }
        if (fname != null) {
            startUpdateProfileActivity();
        }

    }
    void saveImgFile(Bitmap bm){
        myDir = new File(root + "/Rhythmia");
        myDir.mkdirs();
        fname = "ECG_image"+createRandomString()+".png";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            if(bm!=null)
            {
                bm.recycle();
            }
            System.out.println(file.toString());
            //startReportActivity();
        } catch (Exception e) {

        }
    }
    String createRandomString(){
        Long time= System.currentTimeMillis();
        String timehex = Long.toHexString(time);
        return timehex;
    }
    void startUpdateProfileActivity(){
        Intent i = new Intent(getApplicationContext(), ChatActivity.class);
        i.putExtra("cropped",true);
        i.putExtra("user_id",user_id);
        i.putExtra("expert_id",expert_id);
        i.putExtra("report_id",report_id);
        i.putExtra("file",fname);
        startActivity(i);

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(getApplicationContext(), ChatActivity.class);
        i.putExtra("cropped",false);
        i.putExtra("user_id",user_id);
        i.putExtra("expert_id",expert_id);
        i.putExtra("report_id",report_id);
        startActivity(i);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }
}
