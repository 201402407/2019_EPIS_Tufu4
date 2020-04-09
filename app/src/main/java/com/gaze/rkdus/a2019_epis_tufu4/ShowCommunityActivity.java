package com.gaze.rkdus.a2019_epis_tufu4;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
 *  ShowCommunityActivity
 *  Copyright 2019, 김가연. All rights reserved.
 */

public class ShowCommunityActivity extends BaseActivity {
    public static final String TAG = "ShowCommunityActivity";

    ListView listView;
    MyAdapter myAdapter;

    TextView txt_title, txt_id, txt_date, txt_count, txt_content;
    EditText comment_txt;
    ImageButton comment_btn;

    ImageView pic1, pic2, pic3;
    LinearLayout comment_layout;

    String comment, user, strpic1, strpic2, strpic3, title, id, author;
    Bitmap bm1, bm2, bm3;
    byte[] picbyte1, picbyte2, picbyte3;

    int index, count, state;
    int viewWidth;

    public MyProgressDialog progressDialog;

    Context context;

    class MyAdapter extends BaseAdapter {
        ArrayList<CommentItem> items = new ArrayList<CommentItem>();


        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        public void addItem(CommentItem item) {
            items.add(item);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertview, ViewGroup viewGroup) {
            CommentView view = new CommentView(getApplicationContext());

            CommentItem item = items.get(i);
            view.setId(item.getid());
            view.setDate(item.getdate());
            view.setComment(item.getcomment());

            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        if (height > 2000){
            setContentView(R.layout.activity_show_community);

        } else {
            setContentView(R.layout.activity_show_community_small);
        }

        viewWidth = dm.widthPixels;

        Intent intent = getIntent();
        state = intent.getIntExtra("state", 0);
        author = intent.getStringExtra("written");
        user = intent.getStringExtra("user");
        title = intent.getStringExtra("title");
        index = intent.getIntExtra("index", 0);

        //strpic1 = intent.getStringExtra("pic1");

        //picbyte1 = Base64.decode(strpic1, Base64.DEFAULT);
        //bm = BitmapFactory.decodeByteArray(picbyte1, 0, picbyte1.length);

        txt_title = (TextView) findViewById(R.id.title);
        txt_id = (TextView) findViewById(R.id.id);
        txt_date = (TextView) findViewById(R.id.date);
        txt_count = (TextView) findViewById(R.id.count);
        txt_content = (TextView) findViewById(R.id.content);
        listView = (ListView) findViewById(R.id.commentList);

        pic1 = (ImageView) findViewById(R.id.pic1);
        pic2 = (ImageView) findViewById(R.id.pic2);
        pic3 = (ImageView) findViewById(R.id.pic3);

        comment_txt = (EditText) findViewById(R.id.comment_txt);
        comment_btn = (ImageButton) findViewById(R.id.comment_btn);
        comment_layout = (LinearLayout) findViewById(R.id.comment);

//        if (state == 1) {
//            comment_layout.setVisibility(View.GONE);
//            comment_btn.setVisibility(View.GONE);
//        }

        context = this;

        comment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment = comment_txt.getText().toString();
                progressDialog = MyProgressDialog.show(context, "", "", true, false, null);

                new CommentDB().execute(getResources().getString(R.string.urlCommunity) + "/putComment");
            }
        });
        txt_title.setText(title);
        txt_id.setText(author);

        new CommentListData().execute(getResources().getString(R.string.urlCommunity) + "/getCommentList");

//        myAdapter = new MyAdapter();
//
//        ArrayList<String> titles = new ArrayList<>();
//        myAdapter.addItem(new CommentItem("김가연", nowTime, "이런 경우에는 제가 봤을땐 주인분이 문제네요. 왜 천사같은 개를 탓하세요? 나가 뒈지ㅣ세요ㅗ"));
//        myAdapter.addItem(new CommentItem("김가연", nowTime, "롸..? ㄴㅇㅂㅇㄱ"));
//        myAdapter.addItem(new CommentItem("김가연", nowTime, "커뮤니티 빠르게 끝내자ㅎㅎ"));
//        myAdapter.addItem(new CommentItem("김가연", nowTime, "이게 글이냐 지워라~"));
//
//        listView.setAdapter(myAdapter);
//        setListViewHeightBasedOnChildren(listView);
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                final CommentItem item = (CommentItem) myAdapter.getItem(i);
//                final String name = item.getid();
//
//                Toast.makeText(getApplicationContext(), item.getid() + "", Toast.LENGTH_SHORT).show();
//
//                // 글 보여지는 intent 만들기
//            }
//        });

    }

    /* CommentListData : 인덱스 값을 통해 글의 내용과 거기에 달린 댓글 리스트 요청
     *
     *
     * Uri  --->   /getCommentListData
     * Parm  --->   {"user":{"article_index":2}} 전송
     * Result  --->   {"result":{"content":[{"ARTICLE_DATE":"2019-06-24","ARTICLE_CONTENT":"랄랄라라ㅏ","IMG_URL_1":"ASFSES","IMG_URL_2":"ASFSES","IMG_URL_3":NULL}]
     *                       ,"comment":[{"COMMENT_AUTHOR":"김가연","COMMENT_DATE":"2019-06-24","COMMENT_CONTENT":"그게 글이냐!!"},{"COMMENT_AUTHOR":"정지원","COMMENT_DATE":"2019-07-24","COMMENT_CONTENT":"그래 글이다!!"}]}} 결과 값
     *
     * ps. 결과값 : result Object 안에 JSONArray : content, comment  두 개 넣어서!!  */

    public class CommentListData extends AsyncTask<String, String, String> {

        @Override

        protected String doInBackground(String... urls) {

            try {

                JSONObject jsonObject = new JSONObject();
                JSONObject tmp = new JSONObject();

                tmp.accumulate("board_index", index);

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

            myAdapter = new MyAdapter();

            JSONArray content = null, comment = null;

            try {
                json = new JSONObject(result);

                if (json.get("result") == null) {
                    new CommentListData().execute(getResources().getString(R.string.urlCommunity) + "/getCommentList");
                } else {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject = json.getJSONObject("result");

                    content = jsonObject.getJSONArray("content");
                    comment = jsonObject.getJSONArray("comment");

                    for (int i = 0; i < content.length(); i++) {
                        JSONObject jsonTemp = content.getJSONObject(i);
                        txt_content.setText(jsonTemp.getString("content"));
                        txt_date.setText(jsonTemp.getString("date"));

                        if (!jsonTemp.getString("img_url_1").equals("")) {
                            strpic1 = jsonTemp.getString("img_url_1");
                            Log.e(TAG, "HI"+jsonTemp.getString("img_url_1"));
                            picbyte1 = Base64.decode(strpic1, Base64.DEFAULT);

                            bm1 = BitmapFactory.decodeByteArray(picbyte1, 0, picbyte1.length);

                            float width = bm1.getWidth();
                            float height = bm1.getHeight();

                            if(width > viewWidth)
                            {
                                float percente = (float)(width / 100);
                                float scale = (float)(viewWidth / percente);
                                width *= (scale / 100) * 2.1;
                                height *= (scale / 100) * 2.1;
                            }

                            Bitmap temp = Bitmap.createScaledBitmap(bm1, (int) width, (int) height, true);

                            Drawable drawable = new BitmapDrawable(temp);

                            pic1.setBackground(drawable);
                            pic1.setVisibility(View.VISIBLE);
                        }
                        if (!jsonTemp.getString("img_url_2").equals("")) {
                            strpic2 = jsonTemp.getString("img_url_2");
                            Log.e(TAG, "HI"+jsonTemp.getString("img_url_2"));
                            picbyte2 = Base64.decode(strpic2, Base64.DEFAULT);
                            bm2 = BitmapFactory.decodeByteArray(picbyte2, 0, picbyte2.length);

                            float width = bm2.getWidth();
                            float height = bm2.getHeight();

                            if(width > viewWidth)
                            {
                                float percente = (float)(width / 100);
                                float scale = (float)(viewWidth / percente);
                                width *= (scale / 100) * 2.1;
                                height *= (scale / 100) * 2.1;
                            }

                            Bitmap temp = Bitmap.createScaledBitmap(bm2, (int) width, (int) height, true);

                            Drawable drawable = new BitmapDrawable(temp);

                            pic2.setBackground(drawable);
                            pic2.setVisibility(View.VISIBLE);
                        }
                        if (!jsonTemp.getString("img_url_3").equals("")) {
                            strpic3 = jsonTemp.getString("img_url_3");
                            Log.e(TAG, "HI"+jsonTemp.getString("img_url_3"));
                            picbyte3 = Base64.decode(strpic3, Base64.DEFAULT);
                            bm3 = BitmapFactory.decodeByteArray(picbyte3, 0, picbyte3.length);

                            float width = bm3.getWidth();
                            float height = bm3.getHeight();

                            if(width > viewWidth)
                            {
                                float percente = (float)(width / 100);
                                float scale = (float)(viewWidth / percente);
                                width *= (scale / 100) * 2.1;
                                height *= (scale / 100) * 2.1;
                            }

                            Bitmap temp = Bitmap.createScaledBitmap(bm3, (int) width, (int) height, true);

                            Drawable drawable = new BitmapDrawable(temp);

                            pic3.setBackground(drawable);
                            pic3.setVisibility(View.VISIBLE);
                        }
                    }
                    for (int i = 0; i < comment.length(); i++) {
                        JSONObject jsonTemp = comment.getJSONObject(i);
                        count++;
                        myAdapter.addItem(new CommentItem(jsonTemp.getString("author"), jsonTemp.getString("date"), jsonTemp.getString("content")));
                    }

                    listView.setAdapter(myAdapter);
                    setListViewHeightBasedOnChildren(listView);

                    txt_count.setText(count + "");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            Log.e(TAG, result);

        }
    }

    /* CommentDB : 댓글 쓰기
     * 댓글 달기 성공 -> int 1
     * 댓글 달기 실패 -> int 0
     *
     * Uri  --->   /putCommentDB
     * Parm  --->   {"user":{"article_index":2,"comment_author":"미리내병원","comment_date":"2019.07.03","comment_content":"커뮤니티가 끝나간당"}} 전송
     * Result  --->   {"result":1} 결과 값 */

    public class CommentDB extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {

            try {
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                String nowTime = sdf.format(date);

                JSONObject jsonObject = new JSONObject();
                JSONObject tmp = new JSONObject();

                tmp.accumulate("board_index", index);
                tmp.accumulate("author", user);
                tmp.accumulate("date", nowTime);
                tmp.accumulate("content", comment);

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

                    Log.e("ShowComment", jsonObject.toString());
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

                    Log.e("ShowComment!!!! : ", buffer.toString());
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
            int succes = 0;

            try {
                json = new JSONObject(result);

                if (json.get("result") == null) {
                    new CommentDB().execute(getResources().getString(R.string.urlCommunity) + "/putComment");
                } else {
                    succes = (int) json.get("result");

                    progressDialog.dismiss();

                    if (succes == 1) {
                        Toast.makeText(getApplicationContext(), "댓글 달기 성공!!", Toast.LENGTH_LONG).show();

                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);

                    } else {
                        Toast.makeText(getApplicationContext(), "댓글 달기 실패!!", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.e("ShowComment", result);

        }
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        for (int size = 0; size < listView.getCount(); size++) {
            View listItem = myAdapter.getView(size, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = (totalHeight + (listView.getDividerHeight() * (listView.getCount() - 1)));


        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}


