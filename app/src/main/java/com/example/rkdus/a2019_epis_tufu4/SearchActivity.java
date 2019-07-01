package com.example.rkdus.a2019_epis_tufu4;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/*
 * 사용자
 * 반려동물 대행 등록업체를 리스트로 출력하고, 검색 기능 추가
 * 현재 위치를 받아서 현재 위치와 가까운 병원 순으로 나열하는 액티비티
 * 앱 등록 필터 사용
 * 출력된 리스트에서 앱 등록이 된 병원업체 클릭하면 예약 화면으로 진행
 * - 이해원
 */
public class SearchActivity extends AppCompatActivity {
    public static final String SERVER_URL = "http://172.30.1.26:3000";
    public static final String TAG = "LogGoGo";

    /*
    디비 테이블 저장 정보
    - HospitalInfo_TB

        HOSPITAL_KEY: int   -> PK
        CEO_NAME: char(30) NOT NULL
        HOSPITAL_NAME: char(50) NOT NULL
        PHONE_NUMBER: char(15) DEFAULT NULL
        ADDRESS1: char(80) DEFAULT NULL
        ADDRESS2: char(50) DEFAULT NULL
        SIGNUP_APP: tinyint(1) NOT NULL DEFAULT 0
     */

    static final int GET_FILTER = 100;
    boolean isSearchCurrentLocation, isSignUpApp, isBestReservationCount;
    final String switchOnColor = "#0067A3";
    final String switchOffColor = "#000000";
    final String fileName = "searchResult";
    int indexStartNum;
    LocationManager locationManager;
    String searchWord;
    int filter = 0; // 현재 입힌 필터 종류.     0 : 전체(필터 X)    1 : 최다예약순

    ArrayList<SearchResultData> searchResultList = new ArrayList<>();
    ArrayList<SearchItemData> searchList = new ArrayList<>();
    ArrayList<SearchResultData> signUpAppList = new ArrayList<>();
    SearchAsyncTask searchAsyncTask;
    // SearchListAdapter listAdapter;
    SearchListAdapter adapter;

    RecyclerView searchRecyclerView;
    TabLayout listTabLayout;
    ImageView ivSearchBtn, ivFilterBtn;
    EditText eSearch;
    TextView searchCurrentLocationSwitch;
    ListView lvSearchList;
    CheckedTextView ctvIsSignUpApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 변수 값 초기화
        isSearchCurrentLocation = false;
        isSignUpApp = false;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);   // 현재 위치의 위도 경도를 가져오기 위함
        indexStartNum = 1; // 현재 1페이지부터 5페이지까지.

        // 레이아웃 뷰 정의
        ivSearchBtn = (ImageView) findViewById(R.id.searchBtn);
        ivFilterBtn = (ImageView) findViewById(R.id.filterBtn);
        eSearch = (EditText) findViewById(R.id.searchEditText);
        searchCurrentLocationSwitch = (TextView) findViewById(R.id.isSearchCurrentLocation);
        ctvIsSignUpApp = (CheckedTextView) findViewById(R.id.isSignUpApp);
        // lvSearchList = (ListView) findViewById(R.id.searchListView);
        searchRecyclerView = (RecyclerView) findViewById(R.id.searchListView);
        listTabLayout = (TabLayout) findViewById(R.id.listTablayout);

        // 권한 확인 및 요청
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        searchAsyncTask = new SearchAsyncTask();
        searchAsyncTask.execute("/searchHospitalData", "all"); // 모든 데이터 가져오기

        //setTabIndex();
        // EditText 자판 리스너 이벤트
        eSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH: // 자판에서 검색 모양 아이콘을 누르면
                        Toast.makeText(getApplicationContext(), "검색을 시작합니다.", Toast.LENGTH_LONG).show();
                        if(setSearchWord())
                            SearchHospitalData();
                        break;
                    default:
                        return false;
                }
                return false;
            }
        });

        // 검색 이미지 클릭 이벤트
        ivSearchBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:   // 클릭 시
                        Toast.makeText(getApplicationContext(), "검색 이미지 click", Toast.LENGTH_LONG).show();
                        if(setSearchWord())
                            SearchHospitalData();
                        break;
                    case MotionEvent.ACTION_CANCEL: // 클릭하지 않은 상태 시
                        break;
                }
                return true;
            }
        });

        // 필터 이미지 클릭 이벤트
        ivFilterBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:   // 클릭 시
                        Toast.makeText(getApplicationContext(), "필터 이미지 click", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), FilterPopupActivity.class);
                        startActivityForResult(intent, GET_FILTER);
                        break;
                    case MotionEvent.ACTION_CANCEL: // 클릭하지 않은 상태 시
                        break;
                }
                return true;
            }
        });

        // 어플등록 체크박스 체크 이벤트
        ctvIsSignUpApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckedTextView view = (CheckedTextView) v;
                view.toggle();
                if(view.isChecked()) {
                    isSignUpApp = true;
                    Toast.makeText(getApplicationContext(), "어플등록업체 보기 체크", Toast.LENGTH_LONG).show();
                }
                else {
                    isSignUpApp = false;
                    Toast.makeText(getApplicationContext(), "어플등록업체 보기 체크 X", Toast.LENGTH_LONG).show();
                }
                showListView();
            }
        });

        // 현재위치로 찾기 클릭 시
        searchCurrentLocationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSearchCurrentLocation) {   // 만약 현재 위치로 찾기로 리스트를 보여주고 있는 경우
                    searchCurrentLocationSwitch.setTextColor(Color.parseColor(switchOffColor));  // 색상변경
                    Toast.makeText(getApplicationContext(), "현재 위치로 찾기 종료합니다.", Toast.LENGTH_LONG).show();
                    isSearchCurrentLocation = false;
                }
                else {  // 현재 위치로 찾고 싶은 경우
                    searchCurrentLocationSwitch.setTextColor(Color.parseColor(switchOnColor));  // 색상변경
                    Toast.makeText(getApplicationContext(), "현재 위치로 찾기 시작합니다.", Toast.LENGTH_LONG).show();
                    isSearchCurrentLocation = true;
                }
                showListView();  // 현재 상태에 따라 on / off 변경
            }
        });

        // 탭 레이아웃 클릭 시
        listTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                changeTabView(pos);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    /*
    사용자가 탭을 눌렀을 때
     */
    private void changeTabView(int index) {
        switch (index) {
            case 0: // ◀ 버튼 클릭 시
                showNextPage
            case 1:
                int pageNum = Integer.parseInt(listTabLayout.getTabAt(index).getText().toString()); // 해당 페이지 번호 가져오기

            case 6: // ▶ 버튼 클릭 시
        }
    }

    /*
    해당 arraylist의 크기를 보고 전체 페이지 크기를 반환.
     */
    private int getMaxTabSize(ArrayList<?> arrayList) {
        int tabSize = arrayList.size();
        int maxTabSize = 0;
        if(tabSize % 8 == 0)    // ex. 40개면 5페이지.
            maxTabSize = tabSize / 8;
        else    // 39개여도 5페이지.
            maxTabSize = (tabSize / 8) + 1;
        return maxTabSize;
    }

    /*
    이전 또는 다음 페이지를 선택하여 새로운 페이지를 출력해야 하는 경우
     */
    private void showNextPage(ArrayList<?> arrayList, boolean isUpPage) {
        int maxSize = getMaxTabSize(arrayList);
        if(isUpPage) {  // 다음 5개의 페이지를 넘기라고 한다면
            if(indexStartNum + 5 > maxSize) { // 전체가 12페이지인데 11페이지에서 다음 페이지를 선택한 경우.
            Log.d(TAG, "Tab 페이지 설정하는데 다음 페이지가 없는 경우. ex) 최대 14페이지인데 11페이지에서 다음 페이지를 클릭하는 경우.");
            Toast.makeText(getApplicationContext(), "해당 탭 내 최대 페이지입니다.", Toast.LENGTH_LONG).show();
            }
            else {
                indexStartNum += 5;
                setTabIndex(arrayList); // Tab Page 변경
            }
        }
        else {  // 이전 5개의 페이지를 넘기라고 한다면
            if(indexStartNum - 5 > 0) { // 전체가 12페이지인데 1페이지에서 이전 페이지를 선택한 경우.
                Log.d(TAG, "이전 페이지가 존재하지 않습니다. 최소 페이지입니다.");
                Toast.makeText(getApplicationContext(), "해당 탭 내 최소 페이지입니다.", Toast.LENGTH_LONG).show();
            }
            else {
                indexStartNum -= 5;
                setTabIndex(arrayList); // Tab Page 변경
            }
        }
    }

    /*
    Tab Layout에 Tab 생성하는 함수
     */
    private void setTabIndex(ArrayList<?> arrayList) {
        int tabSize = arrayList.size();
        int maxTabSize = getMaxTabSize(arrayList);

        listTabLayout.removeAllTabs(); // tab item 제거

        listTabLayout.addTab(listTabLayout.newTab().setText("◀"));
        if(indexStartNum + 5 > maxTabSize) { // 전체가 12페이지인데 11페이지부터 시작하는 경우.
            for(int i = indexStartNum; i <= maxTabSize; i++) {
                listTabLayout.addTab(listTabLayout.newTab().setText(String.valueOf(i)));
                Log.d(TAG, String.valueOf(i) + " 페이지 생성!");
            }
        }
        else {
            int i = 0;
            while(i < 5) {
                listTabLayout.addTab(listTabLayout.newTab().setText(String.valueOf(indexStartNum + i)));
                Log.d(TAG, String.valueOf(indexStartNum + i) + " 페이지 생성!");
                i++;
            }
        }
        listTabLayout.addTab(listTabLayout.newTab().setText("▶"));
    }

    /*
    현재 필터와 조건에 맞는 arraylist 반환
     */
    private ArrayList<?> getArrayListFromFilter(int filter) {
        this.filter = filter;
        switch (filter) {
            case 0:
                return searchResultList;
                break;
            case 1:
                return
        }
    }

    @Override
    protected void onDestroy() {
        // 배열과 AsyncTask 초기화
        searchList.clear();
        signUpAppList.clear();
        searchAsyncTask = new SearchAsyncTask();
        try {
            if(searchAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                searchAsyncTask.cancel(true);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /*
    현재 위치의 위도 경도 값 가져오는 함수
    @return : Location(위도와 경도를 담음)
     */
    @SuppressLint("MissingPermission")
    private Location getLatLon() {
        Log.d(TAG, "getLatLon start");
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        return location;
    }

    /*
    주소로 위도 경도 값 구하기(ReverseGeoCoding)
    @param address(String) - 주소
    @return latlon(double[2]) - 위도, 경도
     */
    private Location addressToLatLon(Context context, String address) {
        List<Address> addressList = null;
        Log.d(TAG, "addressToLatLon start");
        Geocoder coder = new Geocoder(context);
        Log.d(TAG, "addressToLatLon start2");
        try {
            addressList = coder.getFromLocationName(address, 1);  // 1개의 주소만 가져옴
            Log.d(TAG, "addressList22 : " + addressList.size());
            if(addressList.isEmpty())
                return null;
            else {
                Location myLocation = new Location("");
                double lat = addressList.get(0).getLatitude();
                double lon = addressList.get(0).getLongitude();
                myLocation.setLatitude(lat);
                myLocation.setLongitude(lon);
                return myLocation;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "주소 변환 실패");
        }
        return null;
    }

    /*
    거리 계산하는 함수
    @param : 내위치 lon, lat과 목적지의 lon, lat
    @return : float(두 지점 사이의 거리)
     */
    private float calculateDistance(double myLon, double myLat, double targetLon, double targetLat) {
        Log.d(TAG, "calculateDistance start");

        Location myLocation = new Location("current my place");
        myLocation.setLongitude(myLon);
        myLocation.setLatitude(myLat);

        Location targetLocation = new Location("my target place");
        targetLocation.setLongitude(targetLon);
        targetLocation.setLatitude(targetLat);

//        float distance[] = new float[1];
//        myLocation.distanceBetween(myLat, myLon, targetLat, targetLon, distance);
//        float dis = distance[0];
//        Log.d(TAG, "distanceBetween : " + String.valueOf(dis));
        return myLocation.distanceTo(targetLocation);
    }

    /*
    현재위치에서부터 SearchList에 있는 data들까지의 거리를 구하기.
    @param : searchList or signUpAppList 중 하나를 받음. 해당 ArrayList 위치에 따른 필터 적용
     */
    private void setLocationList(ArrayList<SearchItemData> arrayList) {
        Log.d(TAG, "setLocationList start");

        // 현재 사용자의 위도 경도 획득
        Location myLocation = getLatLon();
        Log.d(TAG, "my lat : " + myLocation.getLatitude() + " , my lon : " + myLocation.getLongitude());
//        Log.d(TAG, String.valueOf(calculateDistance(
//                36.363386, 127.351442,
//                36.360738, 127.351450
//        )));

        // 인자로 받은 ArrayList에 거리 계산해서 넣기
        Iterator<SearchItemData> itemDataIterator = arrayList.iterator();
        while(itemDataIterator.hasNext()) {
            SearchItemData searchItemData = itemDataIterator.next();

            searchItemData.setDistance( // 거리(float) 집어넣기
                    calculateDistance(
                            myLocation.getLatitude(), myLocation.getLongitude(),   // 현재 내 위치의 위도, 경도
                            searchItemData.getLatitude(), searchItemData.getLongitude() // 리스트의 데이터에 저장된 위도와 경도
                    )
            );
        }
        // 각 data들의 거리 값을 비교하여 정렬하기
        Collections.sort(arrayList, new Comparator<SearchItemData>() {
            @Override
            public int compare(SearchItemData s1, SearchItemData s2) {
                return Double.compare(s1.getDistance(), s2.getDistance());
            }
        });
    }

    /*
    현재 위치로 찾기 클릭에 따른 찾기 온오프 기능 구현 함수
     */
    private void showListView() {

        // final ArrayList<SearchItemData> arrayList = getNeedToShowListView();
        final ArrayList<SearchResultData> arrayList = getNeedToShowListView();
        Log.d(TAG, "showListView ArrayList size : " + arrayList.size());
        if(arrayList.isEmpty()) // null check
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setSearchListView(arrayList);
                    }
                });
            }
        });
    }

    /*
    조건에 따라 리스트 보여주는 함수
     */
    private void SearchHospitalData() {
        Log.d(TAG, "SearchHospitalData start");
        searchAsyncTask = new SearchAsyncTask();
        try {   // 현재 searchAsyncTask가 작동중인지 확인. 두 번 실행은 오류남
            if(searchAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                searchAsyncTask.cancel(true);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // 초기화
        searchList.clear();
        signUpAppList.clear();
        // 서버 접속 실행
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
            searchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "/searchHospitalData", "searchword");
        else
            searchAsyncTask.execute("/searchHospitalData", "searchword");
        // showListView();    // 리스트뷰 표시
    }

    /*
    searchList ArrayList에서 어플등록 off인 item 제거 후 signUpAppList에 넣기
     */
    private void setSignUpAppList() {
        Log.d(TAG, "setSignUpAppList start");

        // 검색 결과를 담은 ArrayList의 값이 없는 경우 함수 종료
        if(searchResultList.isEmpty()) {
            Toast.makeText(getApplicationContext(), "검색한 결과가 없기에 필터가 불가능합니다.", Toast.LENGTH_LONG).show();
            return;
        }

        // 이미 배열 내 값이 존재한 경우 함수 종료
        if(!signUpAppList.isEmpty())
            return;

        // System.arraycopy(searchList, 0, signUpAppList, 0, searchList.size()); // deep copy
        // signUpAppList = searchList; // copy
        Iterator<SearchResultData> itemDataIterator = searchResultList.iterator();
        while(itemDataIterator.hasNext()) {
            SearchResultData searchResultData = itemDataIterator.next();

            // 등록 여부 off인 값 삭제
            if(searchResultData.getBoolSIGNUP_APP())
                Log.d(TAG, "showListView ArrayList signUpApp : " + searchResultData.getSIGNUP_APP());
                signUpAppList.add(searchResultData);
        }
    }

    /*
    온오프에 따른 어플등록 업체만 보기 유무 설정해서 출력하는 함수
     */
    private ArrayList<SearchResultData> getNeedToShowListView() {
        Log.d(TAG, "getNeedToShowListView start");
        if(isSignUpApp) {
            if(isSearchCurrentLocation) {
                Log.d(TAG, "어플등록 ㅇ, 현재위치 ㅇ");
                //setLocationList(signUpAppList); // 현재 위치 킨 경우
            }
            else if(filter == 1) {  // 최다 예약 순
                Log.d(TAG, "어플등록 ㅇ, 최다 예약 순 ㅇ");
                Collections.sort(signUpAppList, new Comparator<SearchResultData>() {    // 각 data들의 최다 예약 횟수를 비교하여 내림차순 정렬하기
                    @Override
                    public int compare(SearchResultData searchResultData, SearchResultData t1) {
                        return String.valueOf(t1.getCOUNT()).compareTo(String.valueOf(searchResultData.getCOUNT()));
                    }
                });
            }
            else { // 필터 X.
                Log.d(TAG, "어플등록 ㅇ, 필터 X");
                setSignUpAppList();
                Collections.sort(signUpAppList, new Comparator<SearchResultData>() {     // 각 data들의 병원 명 값을 비교하여 가나다순 정렬하기
                    @Override
                    public int compare(SearchResultData searchResultData, SearchResultData t1) {
                        return searchResultData.getHOSPITAL_NAME().compareTo(t1.getHOSPITAL_NAME());
                    }
                });
            }
            Log.d(TAG, "showListView ArrayList size : " + signUpAppList.size());
            return signUpAppList;
        }
        else {
            if(isSearchCurrentLocation) {
                Log.d(TAG, "어플등록 X, 현재위치 ㅇ");
                // setLocationList(searchResultList);
            }
            else if(filter == 1) {
                Log.d(TAG, "어플등록 X, 최다 예약 순 ㅇ");
                Collections.sort(searchResultList, new Comparator<SearchResultData>() { // 각 data들의 최다 예약 횟수를 비교하여 내림차순 정렬하기
                    @Override
                    public int compare(SearchResultData searchResultData, SearchResultData t1) {
                        return String.valueOf(t1.getCOUNT()).compareTo(String.valueOf(searchResultData.getCOUNT()));
                    }
                });
            }
            else {
                Log.d(TAG, "어플등록 X, 필터 X.");
                Collections.sort(searchResultList, new Comparator<SearchResultData>() { // 각 data들의 병원 명 값을 비교하여 가나다순 정렬하기
                    @Override
                    public int compare(SearchResultData searchResultData, SearchResultData t1) {
                        return searchResultData.getHOSPITAL_NAME().compareTo(t1.getHOSPITAL_NAME());
                    }
                });
            }
            return searchResultList;
        }
    }

    /*
    서버에 접근하는 백그라운드 작업 + UI Thread 활용하는 AsyncTask
     */
    private class SearchAsyncTask extends AsyncTask<String, String, String> {
        // String... [0] : url  [1] : type  [2 ...] : type마다 필요한 값들
        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "SearchAsyncTask doInBackground");
            String search_url = SERVER_URL + strings[0];    // URL
            String type = strings[1];
            // POST 전송방식을 위한 설정
            HttpURLConnection con = null;
            BufferedReader reader = null;

            // 서버에 특정 키워드 디비에서 검색 요청
            try {
                JSONObject jsonObject = setJSONForSendPost(strings); // type(목적)에 맞는 JSONObject 생성하기
                Log.d(TAG, "url : " + search_url);
                Log.d(TAG, "type : " + type);
                URL url = new URL(search_url);  // URL 객체 생성
                Log.d(TAG, "jsonObject String : " + jsonObject.toString());
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(10 * 1000);   // 서버 접속 시 연결 시간
                con.setReadTimeout(10 * 1000);   // Read시 연결 시간
                con.setRequestMethod("POST"); // POST방식 설정
                con.setRequestProperty("Content-Type", "application/json"); // application JSON 형식으로 전송
                con.setRequestProperty("Accept-Charset", "UTF-8"); // Accept-Charset 설정.
                con.setRequestProperty("Accept", "text/json"); // 서버에 response 데이터를 html로 받음 -> JSON 또는 xml
                con.setDoOutput(true); // Outstream으로 post 데이터를 넘겨주겠다는 의미
                con.setDoInput(true); // Inputstream으로 서버로부터 응답을 받겠다는 의미

                // 서버로 보내기위해서 스트림 만듬
                OutputStream outStream = con.getOutputStream();
                // 버퍼를 생성하고 넣음
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                writer.write(jsonObject.toString());    // searchword : 검색키워드 식으로 전송
                writer.flush();
                writer.close(); // 버퍼를 받아줌

                con.connect();

                // 응답 코드 구분
                int responseCode = con.getResponseCode();   // 응답 코드 설정
                if(responseCode == HttpURLConnection.HTTP_OK)  // 200 정상 연결
                    //서버로 부터 데이터를 받음
                {
                    InputStream stream = con.getInputStream();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] bytes = new byte[1024];
                    int count;
                    while ((count = stream.read(bytes)) > 0) {
                        baos.write(bytes, 0, count);
                    }

                    byte[] fileArray = baos.toByteArray();  // byte화
                    String allString = new String(baos.toByteArray());    // byte to string
                    // ByteArrayInputStream bais = new ByteArrayInputStream(fileArray);
                    return allString;
                }
                else {  // 정상 연결 아닐 시
                    printConnectionError(con);
                    return null;
                }
                /*
                String tag;
                // 각각 값을 제대로 받았는지 체크하기 위한 boolean
                boolean isGetHospitalName = false;
                boolean isGetCeoName = false;
                boolean isGetPhoneNum = false;
                int eventType = parser.getEventType();

                // 파싱 시작
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT: // xml의 시작순간
                            break;
                        case XmlPullParser.END_DOCUMENT: // xml의 끝순간
                            break;
                        case XmlPullParser.START_TAG:
                    }
                }
            */
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d(TAG, String.valueOf(e));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.d(TAG, String.valueOf(e));
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, String.valueOf(e));
            } catch (JSONException e) { // JSON 객체 오류
                e.printStackTrace();
                Log.d(TAG, String.valueOf(e));
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
                if(s.equals(null))
                return;
            setSearchListArray(s);
            ArrayList<SearchResultData> arrayList = getNeedToShowListView();
            if(arrayList.isEmpty()) { // null check
                Log.d(TAG, "getNeedToShowListView result is empty");
                return;
            }
            setSearchListView(arrayList);
        }
    }

    /*
    JSON 형식으로 저장된 String 값을 JSON Object로 변환해서 리턴
     */
    private JSONObject StringToJSON(String JSONstr) {
        Log.d(TAG, "StringToJSON start");

//        JSONParser parser = new JSONParser();
//        Object obj = parser.parse(JSONstr);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(JSONstr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /*
    FileOutputStream을 사용해서 서버에서 받은 데이터 전부 파일로 저장하기
     */
    private boolean saveJSONFile(ByteArrayOutputStream byteArrayOutputStream, String filename) {
        Log.d(TAG, "saveJSONFile start");

        filename += ".json";
        // 파일 생성(덮어쓰기)
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = openFileOutput(filename, MODE_PRIVATE); // MODE_PRIVATE : 다른 앱에서 해당 파일 접근 못함
            byteArrayOutputStream.writeTo(fileOutputStream);    // 파일에 작성
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
    json 파일 불러와서 String으로 리턴하기
     */
    private String loadJSONFile(String filename) {
        Log.d(TAG, "loadJSONFIle start");

        String result = null;
        filename += ".json";
        // 파일 생성(덮어쓰기)
        FileInputStream fileinputStream = null;
        try {
            fileinputStream = openFileInput(filename);
            int size = fileinputStream.available();
            byte[] buffer = new byte[size];
            fileinputStream.read(buffer);
            fileinputStream.close();
            result = new String(buffer, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
    받아온 String 값 Json 파일로 임시 저장
     */
    private boolean JSONObjTofile(JSONObject jsonObject, String filename) {
        Log.d(TAG, "JSONObjTofile start");

        filename += ".json";
        // 파일 생성(덮어쓰기)
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = openFileOutput(filename, MODE_PRIVATE); // MODE_PRIVATE : 다른 앱에서 해당 파일 접근 못함
            fileOutputStream.write(jsonObject.toString().getBytes());   // Json 쓰기
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
    String 파라미터를 받아 ArrayList에 저장
     */
    private void setSearchListArray(String bufstr) {
        Log.d(TAG, "setSearchListArray start");
        JSONObject jsonObject = StringToJSON(bufstr);   // JSON 객체 생성
        // JSONObjTofile(jsonObject, fileName);  // json파일로 저장
        putJSONInArrayList(jsonObject); // ArrayList에 넣기
    }

    /*
    ListView에 ArrayList 넣고 출력
    */
    private void setSearchListView(final ArrayList<SearchResultData> arrayList) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        searchRecyclerView.setLayoutManager(linearLayoutManager);

        adapter = new SearchListAdapter(arrayList);
        adapter.resetAll(arrayList);
        searchRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.d(TAG, "setSearchListView start");
            Toast.makeText(getApplicationContext(), "출력을 완료했습니다.", Toast.LENGTH_LONG).show();
            // 클릭 이벤트
//            lvSearchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    SearchResultData item = arrayList.get(position);
//                    Toast.makeText(getApplicationContext(), item.getHOSPITAL_NAME() + ", " + item.getCEO_NAME(), Toast.LENGTH_LONG).show();
//                    if(item.getBoolSIGNUP_APP()) {   // 앱 등록 되어있을 시
//                        Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
//                        intent.putExtra("key", item.getHOSPITAL_KEY());  // Hospital_Key를 인텐트에 담아서
//                        startActivity(intent);  // MessageTypeActivity 실행
//                    }
//                    else
//                        Toast.makeText(getApplicationContext(), "어플 등록이 되어있지 않습니다.", Toast.LENGTH_LONG).show();
//                }
//                        });
    }

    /*
    어플등록 필터 여부에 따른 Array값 정리
     */

    /*
    Json 형식의 String 변수를 ArrayList 안에 전부 넣기
    -> Gson 라이브러리를 사용
     */
    private void putJSONInArrayList(JSONObject jsonObject) {
        try {
            Log.d(TAG, "putJSONInArrayList start");
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            Log.d(TAG, "JsonArray size : " + jsonArray.length());
            Log.d(TAG, "JsonArray string size : " + jsonArray.toString().length());

            // Gson사용. JSONArray to ArrayList
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<SearchResultData>>(){}.getType();
            searchResultList = gson.fromJson(jsonArray.toString(), listType);

            Log.d(TAG, "arrayList에 담기 성공!");
//            for(int i = jsonArray.length() - 1; i >= 0; i--) { // jsonArray에 담긴 jsonObject를 하나씩 꺼낸다.
//                jsonObject = jsonArray.getJSONObject(i);
//
//                Location location = addressToLatLon( // 주소 -> 위도, 경도
//                        getApplicationContext(),
//                        jsonObject.getString("ADDRESS1")
//                );
//                // 위도, 경도 저장
//                if(location != null) {  // null이 아닌 경우
//                    jsonObject.accumulate("lat", location.getLatitude());
//                    jsonObject.accumulate("lon", location.getLongitude());
//                }
//                searchArray.add(jsonObject); // 한 셋트 ArrayList에 넣기
//                Log.d(TAG, "searchList 개수 : " + searchArray.size());
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    private void putJSONInArrayList(JSONObject jsonObject) {
//        try {
//            Log.d(TAG, "putJSONInArrayList start");
//            JSONArray jsonArray = jsonObject.getJSONArray("result");
//            Log.d(TAG, "JsonArray size : " + jsonArray.length());
//            for(int i = jsonArray.length() - 1; i >= 0; i--) { // jsonArray에 담긴 jsonObject를 하나씩 꺼낸다.
//                jsonObject = jsonArray.getJSONObject(i);
//                SearchItemData searchItemData = new SearchItemData(
//                        jsonObject.getInt("HOSPITAL_KEY"),
//                        jsonObject.getString("CEO_NAME"),
//                        jsonObject.getString("HOSPITAL_NAME"),
//                        jsonObject.getString("PHONE_NUMBER"),
//                        jsonObject.getString("ADDRESS1"),
//                        jsonObject.getString("ADDRESS2"),
//                        getBoolSignUpApp(jsonObject.getInt("SIGNUP_APP"))
//                );
//                Log.d(TAG, "putJSONInArrayList searchItemData : " + searchItemData.getHospitalName());
//                Log.d(TAG, "searchItemData.getAddress1() : " + searchItemData.getAddress1());
//                Log.d(TAG, "searchItemData.getAddress2() : " + searchItemData.getAddress2());
//                if(searchItemData.getAddress2().equals(null))
//                    Log.d(TAG, "searchItemData.getAddress2() is null !");
//
//                Location location = addressToLatLon( // 주소 -> 위도, 경도
//                        getApplicationContext(),
//                        searchItemData.getAddress1()
//                );
//                // 위도, 경도 저장
//                if(location != null) {  // null이 아닌 경우
//                    searchItemData.setLatitude(location.getLatitude());
//                    searchItemData.setLongitude(location.getLongitude());
//                }
//                searchList.add(searchItemData); // 한 셋트 ArrayList에 넣기
//                Log.d(TAG, "searchList 개수 : " + searchList.size());
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    /*
    HttpURLConnection 연결 잘 안되는 경우 원인 내용 Log 출력
     */
    public static void printConnectionError(HttpURLConnection con) throws IOException {
        Log.d(TAG, "printConnectionError");
        InputStream is = con.getErrorStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] byteBuffer = new byte[1024];
        byte[] byteData = null;
        int nLength = 0;
        while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
            baos.write(byteBuffer, 0, nLength);
        }
        byteData = baos.toByteArray();
        String response = new String(byteData);
        Log.d(TAG, "응답 코드 발생! 오류 내용 = " + response);
    }

    /*
    EditText 값 유효성 체크한 뒤 리턴
     */
    private boolean setSearchWord() {
        Log.d(TAG, "setSearchWord");
        String eSearchTempText = eSearch.getText().toString();    // 검색어 임시 변수에 저장.
        if(TextUtils.isEmpty(eSearchTempText.trim())) { // 공백처리
            Toast.makeText(getApplicationContext(), "값을 입력해주세요.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(!eSearchTempText.equals(eSearchTempText.trim())) { // 앞뒤 공백이 존재하는 단어 입력
            Toast.makeText(getApplicationContext(), "앞 뒤 공백이 있습니다. 삭제하고 다시 시도하세요.", Toast.LENGTH_LONG).show();
            return false;
        }
        searchWord = eSearchTempText;   // 전역 변수에 저장
        return true;
    }

    /*
    type에 따라 필요한 값을 JSONObject에 담는 함수
    @param : String...
    [0] : URL
    [1] : type
    [2] : 1) searchword : X  2) id : hospital_key
    @return : JSONObject(POST 방식으로 요청하기 위해 보내는 값을 담음) - SearchItemData
     */
    private JSONObject setJSONForSendPost(String... strings) throws JSONException, UnsupportedEncodingException {
        JSONObject jsonObject = new JSONObject();
        String type = strings[1];
        Log.d(TAG, "setJSONForSendPost type : " + type);
        if(type.equals("searchword")) {
            jsonObject.accumulate("searchword", searchWord); // 검색어 인코딩까지 수행
        }
        if(type.equals("all")) {
            jsonObject.accumulate("searchword", "allHospitalData"); // 검색어 인코딩까지 수행
        }
//        else if(type.equals("id")) {
//            jsonObject.accumulate("key", strings[2]);
//        }
        Log.d(TAG, "결과 : " + jsonObject.toString());
        return jsonObject;
    }

    /*
    SIGNUP_APP int to boolean
    0 : 등록 X
    1 : 등록 O
     */
    private boolean getBoolSignUpApp(int signUpApp) {
        if(signUpApp == 1)
            return true;
        return false;
    }
    /*
    Location 관련 권한 체크 상태 확인 함수
    @return : boolean(true : 체크한 경우, false : 체크 안한 경우)
     */
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkLocationPermission() {
        Log.d(TAG, "checkLocationPermission start ");
        if ( checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            // Should we show an explanation?
            // 권한 팝업에서 한번이라도 거부한 경우 true 리턴.
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
            {
                // ...
            }
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult start ");
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                for (int i=0; i<grantResults.length; ++i) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        // 하나라도 거부한다면.
                        new AlertDialog.Builder(this).setTitle("알림").setMessage("위치 권한을 허용해주셔야 해당 서비스를 이용하실 수 있습니다.")
                                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).setNegativeButton("권한 설정", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                getApplicationContext().startActivity(intent);
                            }
                        }).setCancelable(false).show();
                        return;
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
            switch (requestCode) {
                case GET_FILTER:
                    if(resultCode == RESULT_OK) {
                        Log.d(TAG, "팝업창에서 확인 누름!");
                    }
                    else {
                        Log.d(TAG, "팝업창에서 취소 누름!");
                    }
                    break;
                default:
                    break;
            }
    }
}