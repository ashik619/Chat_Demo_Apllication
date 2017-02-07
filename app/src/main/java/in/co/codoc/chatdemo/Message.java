package in.co.codoc.chatdemo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ashik619 on 31-01-2017.
 */
public class Message {
    String from;
    String to;
    String msg;
    int type;
    String timeStamp;
    public  Message(JSONObject object){
        try {
            this.type = object.getInt("type");
            this.from = object.getString("from_id");
            this.to = object.getString("to_id");
            this.msg = object.getString("msg");
            this.timeStamp = object.getString("created_mili");
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    public static ArrayList<Message> fromJson(JSONArray jsonObjects) {
        ArrayList<Message> transactions = new ArrayList<Message>();
        for (int i = jsonObjects.length()-1; i >= 0; i--) {
            try {
                transactions.add(new Message(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return transactions;
    }

}
