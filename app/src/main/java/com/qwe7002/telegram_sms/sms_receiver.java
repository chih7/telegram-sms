package com.qwe7002.telegram_sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class sms_receiver extends BroadcastReceiver {
    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public sms_receiver() {

    }
    public class request_json {
        public String chat_id;
        public String text;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", MODE_PRIVATE);

        String bot_token = sharedPreferences.getString("bot_token","");
        String chat_id = sharedPreferences.getString("chat_id","");
        try {
            if (SMS_RECEIVED.equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    assert pdus != null;
                    final SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    if (messages.length > 0) {
                        String msgBody = messages[0].getMessageBody();
                        String msgAddress = messages[0].getOriginatingAddress();
                        Date date = new Date(messages[0].getTimestampMillis());
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String msgDate = format.format(date);
                        String request_uri = "https://api.telegram.org/bot"+bot_token+"/sendMessage";
                        request_json request_body = new request_json();
                        request_body.chat_id = chat_id;
                        request_body.text = "From: " + msgAddress + "\nBody: " + msgBody
                                + "\nDate: " + msgDate;
                        Gson gson = new Gson();
                        String request_body_raw = gson.toJson(request_body);
                        Log.d("body", request_body_raw);

                        RequestBody body = RequestBody.create(JSON,request_body_raw);
                        OkHttpClient okHttpClient = new OkHttpClient();
                        Request request = new Request.Builder().url(request_uri).method("POST", body).build();
                        Call call = okHttpClient.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d("Failure", "onFailure: ");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Log.d("response", response.body().string());
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
