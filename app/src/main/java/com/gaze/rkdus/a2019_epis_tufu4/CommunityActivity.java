package com.gaze.rkdus.a2019_epis_tufu4;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.util.ArrayList;

/*
 *  CommunityActivity
 *  Copyright 2019, 김가연. All rights reserved.
 */

public class CommunityActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = "CommunityActivity";

    CommunityItem[] itemArray = new CommunityItem[100];

    // 사용자면 user 1
    //  병원이면 user 2
    int user_state = 0;

    // 제목 select 2
    // 작성자 select 1
    public int select = 1;

    String user = "USER";
    Bitmap bm;
    byte[] profile;
    ImageView imageView;

    ListView listView;
    MyAdapter myAdapter;

    ImageButton writebtn;
    ImageButton search;
    TextView one, two, three, four, five, search_input;

    TextView ask, goods, sale, find;
    ImageView ask_bar, goods_bar, sale_bar, find_bar, used_goods, puppy_sale, find_dog;
    ImageButton pre, next;
    String title, written, key;
    int count = 0;
    int num_two, num_three, num_four, num_five;

    boolean check = true;

    boolean two_check = false, three_check = false, four_check = false, five_check = false;
    public MyProgressDialog progressDialog;
    int size = 8;

    Context context;

    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onRefresh() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    class MyAdapter extends BaseAdapter {
        ArrayList<CommunityItem> items = new ArrayList<CommunityItem>();


        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        public void addItem(CommunityItem item) {
            items.add(item);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertview, ViewGroup viewGroup) {
            CommunityView view = new CommunityView(getApplicationContext());

            CommunityItem item = items.get(i);
            view.setTitle(item.getTitle());
            view.setWritten(item.getWritten());

            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        if (height > 2000) {
            size = 8;

        } else {
            size = 6;
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        Intent intent = getIntent();

        if (intent.hasExtra("user")) {
            switch (intent.getIntExtra("user", 0)) {
                case 1:
                    user_state = 1;
                    user = intent.getStringExtra("userName");
                    break;

                case 2:
                    user_state = 2;
                    user = intent.getStringExtra("hosName");

//                    profile = intent.getByteArrayExtra("profile");
//                    bm = BitmapFactory.decodeByteArray(profile, 0, profile.length);
//
//                    imageView.setImageBitmap(bm);
//                    // 프로필 사진 지정
            }
        }

        num_two = size * 2;
        num_three = size * 3;
        num_four = size * 4;
        num_five = size * 5;
        one = (TextView) findViewById(R.id.one);
        two = (TextView) findViewById(R.id.two);
        three = (TextView) findViewById(R.id.three);
        four = (TextView) findViewById(R.id.four);
        five = (TextView) findViewById(R.id.five);
        pre = (ImageButton) findViewById(R.id.pre);
        next = (ImageButton) findViewById(R.id.next);

        ask = (TextView) findViewById(R.id.ask);
        goods = (TextView) findViewById(R.id.goods);
        sale = (TextView) findViewById(R.id.sale);
        find = (TextView) findViewById(R.id.find);

        ask_bar = (ImageView) findViewById(R.id.ask_bar);
        goods_bar = (ImageView) findViewById(R.id.goods_bar);
        sale_bar = (ImageView) findViewById(R.id.sale_bar);
        find_bar = (ImageView) findViewById(R.id.find_bar);

        used_goods = (ImageView) findViewById(R.id.used_goods);
        puppy_sale = (ImageView) findViewById(R.id.puppy_sale);
        find_dog = (ImageView) findViewById(R.id.find_dog);

        writebtn = (ImageButton) findViewById(R.id.write);


        ask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ask_bar.setVisibility(View.VISIBLE);
                goods_bar.setVisibility(View.INVISIBLE);
                sale_bar.setVisibility(View.INVISIBLE);
                find_bar.setVisibility(View.INVISIBLE);

                listView.setVisibility(View.VISIBLE);
                find_dog.setVisibility(View.GONE);
                puppy_sale.setVisibility(View.GONE);
                used_goods.setVisibility(View.GONE);

                ask.setTextColor(Color.parseColor("#FF87AA"));
                goods.setTextColor(Color.parseColor("#000000"));
                sale.setTextColor(Color.parseColor("#000000"));
                find.setTextColor(Color.parseColor("#000000"));

                if (five_check){
                    two.setVisibility(View.VISIBLE);
                    three.setVisibility(View.VISIBLE);
                    four.setVisibility(View.VISIBLE);
                    five.setVisibility(View.VISIBLE);
                }else if(four_check){
                    two.setVisibility(View.VISIBLE);
                    three.setVisibility(View.VISIBLE);
                    four.setVisibility(View.VISIBLE);
                }else if (three_check){
                    two.setVisibility(View.VISIBLE);
                    three.setVisibility(View.VISIBLE);
                }else if (two_check){
                    two.setVisibility(View.VISIBLE);
                }

                one.setEnabled(true);
                next.setEnabled(true);
                pre.setEnabled(true);

                check = true;
            }
        });

        goods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ask_bar.setVisibility(View.INVISIBLE);
                goods_bar.setVisibility(View.VISIBLE);
                sale_bar.setVisibility(View.INVISIBLE);
                find_bar.setVisibility(View.INVISIBLE);

                listView.setVisibility(View.GONE);
                find_dog.setVisibility(View.GONE);
                puppy_sale.setVisibility(View.GONE);
                used_goods.setVisibility(View.VISIBLE);

                ask.setTextColor(Color.parseColor("#000000"));
                goods.setTextColor(Color.parseColor("#FF87AA"));
                sale.setTextColor(Color.parseColor("#000000"));
                find.setTextColor(Color.parseColor("#000000"));

                two.setVisibility(View.GONE);
                three.setVisibility(View.GONE);
                four.setVisibility(View.GONE);
                five.setVisibility(View.GONE);

                one.setEnabled(false);
                next.setEnabled(false);
                pre.setEnabled(false);

                check = false;
            }
        });

        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ask_bar.setVisibility(View.INVISIBLE);
                goods_bar.setVisibility(View.INVISIBLE);
                sale_bar.setVisibility(View.VISIBLE);
                find_bar.setVisibility(View.INVISIBLE);

                listView.setVisibility(View.GONE);
                find_dog.setVisibility(View.GONE);
                puppy_sale.setVisibility(View.VISIBLE);
                used_goods.setVisibility(View.GONE);

                ask.setTextColor(Color.parseColor("#000000"));
                goods.setTextColor(Color.parseColor("#000000"));
                sale.setTextColor(Color.parseColor("#FF87AA"));
                find.setTextColor(Color.parseColor("#000000"));

                two.setVisibility(View.GONE);
                three.setVisibility(View.GONE);
                four.setVisibility(View.GONE);
                five.setVisibility(View.GONE);

                one.setEnabled(false);
                next.setEnabled(false);
                pre.setEnabled(false);
                check = false;
            }
        });

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ask_bar.setVisibility(View.INVISIBLE);
                goods_bar.setVisibility(View.INVISIBLE);
                sale_bar.setVisibility(View.INVISIBLE);
                find_bar.setVisibility(View.VISIBLE);

                listView.setVisibility(View.GONE);
                find_dog.setVisibility(View.VISIBLE);
                puppy_sale.setVisibility(View.GONE);
                used_goods.setVisibility(View.GONE);

                listView.setVisibility(View.GONE);
                ask.setTextColor(Color.parseColor("#000000"));
                goods.setTextColor(Color.parseColor("#000000"));
                sale.setTextColor(Color.parseColor("#000000"));
                find.setTextColor(Color.parseColor("#FF87AA"));

                two.setVisibility(View.GONE);
                three.setVisibility(View.GONE);
                four.setVisibility(View.GONE);
                five.setVisibility(View.GONE);

                one.setEnabled(false);
                next.setEnabled(false);
                pre.setEnabled(false);
                check = false;
            }
        });
        context = this;

        writebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteCommunityActivity.class);
                intent.putExtra("id", user);
                intent.putExtra("count", count);
                intent.putExtra("state", user_state);
                startActivityForResult(intent, 1111);
            }
        });

        search = (ImageButton) findViewById(R.id.search);
        search_input = (TextView) findViewById(R.id.search_input);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check) {
                    key = search_input.getText().toString();
                    new SearchCommunity().execute(getResources().getString(R.string.urlCommunity) + "/getSearchCommunityList");
                }
            }
        });

        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAdapter = new MyAdapter();

                for (int i = 0; i < size; i++) {
                    myAdapter.addItem(itemArray[i]);
                }

                listView.setAdapter(myAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        final CommunityItem item = (CommunityItem) myAdapter.getItem(i);

                        TextView oTextWritten = (TextView) view.findViewById(R.id.written);
                        TextView oTextTitle = (TextView) view.findViewById(R.id.title);

                        title = oTextTitle.getText().toString();
                        written = oTextWritten.getText().toString();

                        Intent intent = new Intent(getApplicationContext(), ShowCommunityActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("user", user);
                        intent.putExtra("written", written);
                        intent.putExtra("index", item.getIndex());
                        intent.putExtra("state", user_state);
                        startActivityForResult(intent, 1111);
                    }
                });

                myAdapter.notifyDataSetChanged();
                myAdapter.notifyDataSetInvalidated();
            }
        });

        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAdapter = new MyAdapter();

                for (int i = size; i < size + num_two; i++) {
                    myAdapter.addItem(itemArray[i]);
                }
                listView.setAdapter(myAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        final CommunityItem item = (CommunityItem) myAdapter.getItem(i);

                        TextView oTextWritten = (TextView) view.findViewById(R.id.written);
                        TextView oTextTitle = (TextView) view.findViewById(R.id.title);

                        title = oTextTitle.getText().toString();
                        written = oTextWritten.getText().toString();

                        Intent intent = new Intent(getApplicationContext(), ShowCommunityActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("user", user);
                        intent.putExtra("written", written);
                        intent.putExtra("index", item.getIndex());
                        intent.putExtra("state", user_state);
                        startActivityForResult(intent, 1111);
                    }
                });

                myAdapter.notifyDataSetChanged();
                myAdapter.notifyDataSetInvalidated();
            }
        });

        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAdapter = new MyAdapter();

                for (int i = num_two; i < num_two + num_three; i++) {
                    myAdapter.addItem(itemArray[i]);
                }

                listView.setAdapter(myAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        final CommunityItem item = (CommunityItem) myAdapter.getItem(i);

                        TextView oTextWritten = (TextView) view.findViewById(R.id.written);
                        TextView oTextTitle = (TextView) view.findViewById(R.id.title);

                        title = oTextTitle.getText().toString();
                        written = oTextWritten.getText().toString();

                        Intent intent = new Intent(getApplicationContext(), ShowCommunityActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("user", user);
                        intent.putExtra("written", written);
                        intent.putExtra("index", item.getIndex());
                        intent.putExtra("state", user_state);
                        startActivityForResult(intent, 1111);
                    }
                });

                myAdapter.notifyDataSetChanged();
                myAdapter.notifyDataSetInvalidated();
            }
        });

        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAdapter = new MyAdapter();

                for (int i = num_three; i < num_three + num_four; i++) {
                    myAdapter.addItem(itemArray[i]);
                }

                listView.setAdapter(myAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        final CommunityItem item = (CommunityItem) myAdapter.getItem(i);

                        TextView oTextWritten = (TextView) view.findViewById(R.id.written);
                        TextView oTextTitle = (TextView) view.findViewById(R.id.title);

                        title = oTextTitle.getText().toString();
                        written = oTextWritten.getText().toString();

                        Intent intent = new Intent(getApplicationContext(), ShowCommunityActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("user", user);
                        intent.putExtra("written", written);
                        intent.putExtra("index", item.getIndex());
                        intent.putExtra("state", user_state);
                        startActivityForResult(intent, 1111);
                    }
                });

                myAdapter.notifyDataSetChanged();
                myAdapter.notifyDataSetInvalidated();
            }
        });

        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAdapter = new MyAdapter();

                for (int i = num_four; i < num_four + num_five; i++) {
                    myAdapter.addItem(itemArray[i]);
                }

                listView.setAdapter(myAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        final CommunityItem item = (CommunityItem) myAdapter.getItem(i);

                        TextView oTextWritten = (TextView) view.findViewById(R.id.written);
                        TextView oTextTitle = (TextView) view.findViewById(R.id.title);

                        title = oTextTitle.getText().toString();
                        written = oTextWritten.getText().toString();

                        Intent intent = new Intent(getApplicationContext(), ShowCommunityActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("user", user);
                        intent.putExtra("written", written);
                        intent.putExtra("index", item.getIndex());
                        intent.putExtra("state", user_state);
                        startActivityForResult(intent, 1111);
                    }
                });

                myAdapter.notifyDataSetChanged();
                myAdapter.notifyDataSetInvalidated();
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        if (height > 2000) {
            ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.select, R.layout.spinner_item);
            spinner.setAdapter(adapter);
        } else {
            ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.select, R.layout.spinner_item_small);
            spinner.setAdapter(adapter);
        }


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    select = 1;
                } else {
                    select = 2;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        progressDialog = MyProgressDialog.show(context, "", "", true, false, null);
        new CommunityListData().execute(getResources().getString(R.string.urlCommunity) + "/getCommunityList");

        listView = (ListView) findViewById(R.id.communityList);
//
//        myAdapter = new MyAdapter();
//
//        ArrayList<String> titles = new ArrayList<>();
//
//        myAdapter.addItem(new CommunityItem("내장형 시렁", "김가연", 1));
//        myAdapter.addItem(new CommunityItem("나의 맥북 최고 >0< ㅎㅎ", "정지원", 2));
//        myAdapter.addItem(new CommunityItem("마이크로칩 무서웡", "이해원", 3));
//        myAdapter.addItem(new CommunityItem("k 해커톤 멀다", "박지민", 4));
//
//       listView.setAdapter(myAdapter);
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                final CommunityItem item = (CommunityItem) myAdapter.getItem(i);
//                final String name = item.getWritten();
//
//                Toast.makeText(getApplicationContext(), item.getIndex()+"", Toast.LENGTH_SHORT).show();
//
//                // 글 보여지는 intent 만들기
//            }
//        });

    }

    /* CommunityListData : 전체 커뮤니티 글 리스트 출력
     *
     * select 1 : 작성자에서 검색
     * select 2 : 제목에서 검색
     *
     * Uri  --->   /getCommunityListData
     * Parm  --->   {"user":1} 전송
     * Result  --->   {"result":{"community":[{"TITLE":"뿡이는 뭐할까","author":"김가연","board_index":1},{"TITLE":"지원아 일어나","author":"정지원","board_index":2}]}} 결과 값
     *
     * ps. 결과값 : result Object 안에 JSONArray : community 넣어서!!  */

    public class CommunityListData extends AsyncTask<String, String, String> {

        @Override

        protected String doInBackground(String... urls) {

            try {

                JSONObject jsonObject = new JSONObject();
                JSONObject tmp = new JSONObject();

                jsonObject.accumulate("user", 1);

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
            JSONArray state = null;

            myAdapter = new MyAdapter();

            progressDialog.dismiss();
            try {
                json = new JSONObject(result);

                if (json.get("result") == null) {
                    new CommunityListData().execute(getResources().getString(R.string.urlCommunity) + "/getCommunityList");
                } else {
                    JSONObject jsonObject = json.getJSONObject("result");

                    state = jsonObject.getJSONArray("community");

                    count = 0;
                    for (int i = 0; i < state.length(); i++) {
                        JSONObject jsonTemp = state.getJSONObject(i);
                        itemArray[count] = new CommunityItem(jsonTemp.getString("title"), jsonTemp.getString("author"), jsonTemp.getInt("board_index"));
                        count++;
                    }

                    viewCount(count);
                    one.setEnabled(true);

                    if (count < size) {
                        for (int i = 0; i < count; i++) {
                            myAdapter.addItem(itemArray[i]);
                            one.setEnabled(false);
                        }
                    } else {
                        for (int i = 0; i < size; i++) {
                            one.setEnabled(true);
                            myAdapter.addItem(itemArray[i]);
                        }
                    }

                    listView.setAdapter(myAdapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            final CommunityItem item = (CommunityItem) myAdapter.getItem(i);

                            TextView oTextWritten = (TextView) view.findViewById(R.id.written);
                            TextView oTextTitle = (TextView) view.findViewById(R.id.title);

                            title = oTextTitle.getText().toString();
                            written = oTextWritten.getText().toString();

                            Intent intent = new Intent(getApplicationContext(), ShowCommunityActivity.class);
                            intent.putExtra("title", title);
                            intent.putExtra("user", user);
                            intent.putExtra("written", written);
                            intent.putExtra("index", item.getIndex());
                            intent.putExtra("state", user_state);
                            startActivityForResult(intent, 1111);
                        }
                    });

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            Log.e(TAG, result);

        }
    }

    private void viewCount(int count) {
        two.setVisibility(View.GONE);
        three.setVisibility(View.GONE);
        four.setVisibility(View.GONE);
        five.setVisibility(View.GONE);
        if (size < count && count <= size * 2) {
            two.setVisibility(View.VISIBLE);
            two_check = true;
            num_two = count - size;
        } else if (size * 2 < count && count <= size * 3) {
            two.setVisibility(View.VISIBLE);
            three.setVisibility(View.VISIBLE);
            two_check = true;
            three_check = true;
            num_three = count - size * 2;
        } else if (size * 3 < count && count <= size * 4) {
            two.setVisibility(View.VISIBLE);
            three.setVisibility(View.VISIBLE);
            four.setVisibility(View.VISIBLE);
            two_check = true;
            three_check = true;
            four_check = true;
            num_four = count - size * 3;
        } else if (size * 4 < count && count <= size * 5) {
            two.setVisibility(View.VISIBLE);
            three.setVisibility(View.VISIBLE);
            four.setVisibility(View.VISIBLE);
            five.setVisibility(View.VISIBLE);
            two_check = true;
            three_check = true;
            four_check = true;
            five_check = true;
            num_five = count - size * 4;
        }
    }

    /* SearchCommunity : ID값과 key 값을 통해 값이 포함된 리스트 출력
     *
     * select 1 : 작성자에서 검색
     * select 2 : 제목에서 검색
     *
     * Uri  --->   /getSearchCommunity
     * Parm  --->   {"user":{"select":1,"key":"뿡이"}} 전송
     * Result  --->   {"result":{"community":[{"TITLE":"뿡이는 뭐할까","author":"김가연","board_index":1},{"author":"지원아 일어나","author":"정지원","board_index":2}]}} 결과 값
     *
     * ps. 결과값 : result Object 안에 JSONArray : community 넣어서!!  */

    public class SearchCommunity extends AsyncTask<String, String, String> {

        @Override

        protected String doInBackground(String... urls) {

            try {

                JSONObject jsonObject = new JSONObject();
                JSONObject tmp = new JSONObject();

                tmp.accumulate("select", select);
                tmp.accumulate("key", key);

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
            JSONArray state = null;

            myAdapter = new MyAdapter();

            try {
                json = new JSONObject(result);

                if (json.get("result") == null) {
                    new SearchCommunity().execute(getResources().getString(R.string.urlCommunity) + "/getSearchCommunityList");
                } else {
                    JSONObject jsonObject = json.getJSONObject("result");

                    state = jsonObject.getJSONArray("community");
                    count = 0;
                    for (int i = 0; i < state.length(); i++) {
                        JSONObject jsonTemp = state.getJSONObject(i);
                        itemArray[count] = new CommunityItem(jsonTemp.getString("title"), jsonTemp.getString("author"), jsonTemp.getInt("board_index"));
                        count++;
                    }

                    viewCount(count);
                    one.setEnabled(true);
                    if (count < size) {
                        for (int i = 0; i < count; i++) {
                            myAdapter.addItem(itemArray[i]);
                            one.setEnabled(false);
                        }
                    } else {
                        for (int i = 0; i < size; i++) {
                            myAdapter.addItem(itemArray[i]);
                        }
                    }

                    listView.setAdapter(myAdapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            final CommunityItem item = (CommunityItem) myAdapter.getItem(i);

                            TextView oTextWritten = (TextView) view.findViewById(R.id.written);
                            TextView oTextTitle = (TextView) view.findViewById(R.id.title);

                            title = oTextTitle.getText().toString();
                            written = oTextWritten.getText().toString();

                            Intent intent = new Intent(getApplicationContext(), ShowCommunityActivity.class);
                            intent.putExtra("title", title);
                            intent.putExtra("user", user);
                            intent.putExtra("written", written);
                            intent.putExtra("index", item.getIndex());
                            intent.putExtra("state", user_state);
                            startActivityForResult(intent, 1111);
                        }
                    });

                    myAdapter.notifyDataSetChanged();
                    myAdapter.notifyDataSetInvalidated();


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            Log.e(TAG, result);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

//    public void startAni(){
//        final LottieAnimationView animationView = (LottieAnimationView) findViewById(R.id.animation_view);
//
//        animationView.setAnimation("loading.json");
//        animationView.loop(true);
//        //anim.setVisibility(View.VISIBLE);
//        animationView.playAnimation();
//        animationView.addAnimatorListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                animationView.setRepeatCount(3);
//                animationView.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                //anim.setVisibility(View.GONE);
//                animationView.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//            }
//        });
//    }

}
