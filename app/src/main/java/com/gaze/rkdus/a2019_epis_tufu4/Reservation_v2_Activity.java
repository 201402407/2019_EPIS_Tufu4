package com.gaze.rkdus.a2019_epis_tufu4;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/*
 *  Reservation_v2_Activity
 *  Copyright 2019, 김가연. All rights reserved.
 */

public class Reservation_v2_Activity extends BaseActivity {

    TextView owner, resident, phone, resAddr, nowAddr, animal, variety, furColor, gender, neutralization, birthday, acqDate, special;

    TableRow sametime;
    ImageView self_buy;

    String id, strOwner, strAnimal;
    String TAG = "Reservation_v2_Activity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        if (height > 2000){
            setContentView(R.layout.activity_reservation_v2_);
        } else {
            setContentView(R.layout.activity_reservation_v2_small);
        }


        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        strOwner = intent.getStringExtra("owner");
        strAnimal = intent.getStringExtra("animal");

        owner = (TextView) findViewById(R.id.owner);
        resident = (TextView) findViewById(R.id.resident);
        phone = (TextView) findViewById(R.id.phone);
        resAddr = (TextView) findViewById(R.id.resAddr);
        nowAddr = (TextView) findViewById(R.id.nowAddr);
        animal = (TextView) findViewById(R.id.animal);
        variety = (TextView) findViewById(R.id.variety);
        furColor = (TextView) findViewById(R.id.furColor);
        gender = (TextView) findViewById(R.id.gender);
        neutralization = (TextView) findViewById(R.id.neutralization);
        birthday = (TextView) findViewById(R.id.birthday);
        acqDate = (TextView) findViewById(R.id.acqDate);
        special = (TextView) findViewById(R.id.special);

        sametime = (TableRow) findViewById(R.id.sametime);

        self_buy = (ImageView)findViewById(R.id.self_buy);


        new ReservationInfoData().execute(getResources().getString(R.string.url) + "/getReservationInfoData");

    }

    /* ReservationInfoData : ID, 주인 이름, 강아지 이름 값을 통해 예약 데이터 요청
     *
     *
     * Uri  --->   /getReservationInfoData
     * Parm  --->   {"user":{"id":"test","owner":"김가연","animal":"뿡이"}} 전송
     * Result  --->   {"result":{"owner":"김가연","resident":"960708-2","phone":"010-4491-0778","resAddr":"대전","nowAddr":"궁동",
     * "animal":"뿡이","variety":"시츄","furColor":"흰색+갈색","gender":"남","neutralization":"했움","birthday":"2008-04-04","acqDate":"2008-04-04","special":"겁이 많아요ㅠㅠ"}} 결과 값 */

    public class ReservationInfoData extends AsyncTask<String, String, String> {

        @Override

        protected String doInBackground(String... urls) {

            try {

                JSONObject jsonObject = new JSONObject();
                JSONObject tmp = new JSONObject();

                tmp.accumulate("id", id);
                tmp.accumulate("owner_name", strOwner);
                tmp.accumulate("pet_name", strAnimal);

                jsonObject.accumulate("user", tmp);

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {

                    URL url = new URL(urls[0]);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Cache-Control", "no-cache");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Accept", "text/html");
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.connect();

                    //서버로 보내기위해서 스트림 만듬
                    OutputStream outStream = con.getOutputStream();

                    //버퍼를 생성하고 넣음
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));

                    Log.e(TAG, jsonObject.toString());
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();//버퍼를 받아줌

                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    return buffer.toString();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            JSONObject json = null;

            try {
                json = new JSONObject(result);

                if (json.get("result") == null) {
                    new ReservationInfoData().execute(getResources().getString(R.string.url) + "/getReservationInfoData");
                } else {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject = json.getJSONObject("result");

                    owner.setText(jsonObject.getString("owner_name"));
                    resident.setText(jsonObject.getString("owner_resident"));
                    phone.setText(jsonObject.getString("owner_phone"));
                    resAddr.setText(jsonObject.getString("address1"));
                    nowAddr.setText(jsonObject.getString("address2"));
                    animal.setText(jsonObject.getString("pet_name"));
                    variety.setText(jsonObject.getString("pet_variety"));
                    furColor.setText(jsonObject.getString("pet_color"));

                    if (jsonObject.getInt("pet_gender") == 2) {
                        gender.setText("남성");
                    } else {
                        gender.setText("여성");
                    }

                    if (jsonObject.getInt("pet_neutralization") == 1) {
                        neutralization.setText("했음");
                    } else {
                        neutralization.setText("안했음");
                    }

                    if (jsonObject.getInt("sametime") == 1) {
                        sametime.setVisibility(View.VISIBLE);
                    } else {
                        sametime.setVisibility(View.GONE);
                    }


                    birthday.setText(jsonObject.getString("pet_birth"));
                    acqDate.setText(jsonObject.getString("regist_date"));
                    special.setText(jsonObject.getString("etc"));

                    if (jsonObject.getInt("self_buy") == 1) {
                        self_buy.setBackgroundResource(R.drawable.checked);
                    } else {
                        self_buy.setBackgroundResource(R.drawable.check);
                    }


                    Log.e(TAG, jsonObject.getString("owner_name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.e(TAG, result);

        }
    }
}


