package com.gaze.rkdus.a2019_epis_tufu4.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.gaze.rkdus.a2019_epis_tufu4.BaseActivity;
import com.gaze.rkdus.a2019_epis_tufu4.GlobalApplication;
import com.gaze.rkdus.a2019_epis_tufu4.R;
import com.gaze.rkdus.a2019_epis_tufu4.item.SearchResultData;
import com.gaze.rkdus.a2019_epis_tufu4.popup.NicknamePopupActivity;
import com.gaze.rkdus.a2019_epis_tufu4.utils.AddEntrpsInfoService;
import com.gaze.rkdus.a2019_epis_tufu4.utils.Prop;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.gaze.rkdus.a2019_epis_tufu4.user.SearchActivity.StringToJSON;


public class UserLoginActivity extends BaseActivity {
    private SessionCallback callback;
    public final static String TAG = "LogGoGo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        if(KakaoSDK.getAdapter() == null)
            KakaoSDK.init(new GlobalApplication.KakaoSDKAdapter());

        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
        Session.getCurrentSession().checkAndImplicitOpen();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }
    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            requestMe();
        }
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if (exception != null) {
                Logger.e(exception);
            }
            Log.d(TAG, "세션 실패");
            setContentView(R.layout.activity_user_login);   // 세션 실패 시 로그인화면 다시 불러옴
        }
    }

    /** 사용자에 대한 정보를 가져온다 **/
    private void requestMe() {

        List<String> keys = new ArrayList<>();
        keys.add("properties.nickname");
        keys.add("properties.profile_image");
        keys.add("kakao_account.email");

        UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                super.onFailure(errorResult);
                Log.e(TAG, "requestMe onFailure message : " + errorResult.getErrorMessage());
            }

            @Override
            public void onFailureForUiThread(ErrorResult errorResult) {
                super.onFailureForUiThread(errorResult);
                Log.e(TAG, "requestMe onFailureForUiThread message : " + errorResult.getErrorMessage());
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.e(TAG, "requestMe onSessionClosed message : " + errorResult.getErrorMessage());
            }

            @Override
            public void onSuccess(MeV2Response result) {
                KAKAO_ID = String.valueOf(result.getId());
                Log.e(TAG, "requestMe onSuccess message : " + result.getKakaoAccount().getEmail() + " " + result.getId() + " " + result.getNickname() + " " + result.getKakaoAccount().getPhoneNumber());
                redirectSignupActivity();
            }

        });
    }

    protected void redirectSignupActivity() {
        Toast.makeText(getApplicationContext(), "카카오톡 아이디로 자동 로그인 되었습니다.", Toast.LENGTH_SHORT).show();
        checkExistNickname();
//        String temp = loadJSONFile();
//        getArrayList(temp);
    }


    /**
     * json 데이터를 DB에 저장하기 위함. 밑 함수 3개.
     * @param str
     */
    private void getArrayList(String str) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(str);
            Log.d(TAG, "putJSONInArrayList start");
            JSONArray jsonArray = jsonObject.getJSONArray("data");
//        // Gson사용. JSONArray to ArrayList
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Prop.EntrpsJsonData>>(){}.getType();

            ArrayList<Prop.EntrpsJsonData> list = new ArrayList<Prop.EntrpsJsonData>();
            list = gson.fromJson(jsonArray.toString(), listType);
            Log.d(TAG, list.size() + " a ");
            int cnt1 = 0, cnt2 = 0, cnt3 = 0, cnt4 = 0, cnt5 = 0;
            for(Prop.EntrpsJsonData data : list) {
                if(data.getADDR() == null) {
                    cnt1++;
                }
                if(data.getENTRPS_NM() == null) {
                    cnt2++;
                }
                if(data.getRPRSNTV_NM() == null) {
                    cnt3++;
                }
                if(data.getENTRPS_TELNO() == null) {
                    cnt4++;
                }
                if(data.getDETAIL_ADDR() == null) {
                    cnt5++;
                }
//                Log.d(TAG, data.getADDR() + ", " + data.getENTRPS_NM() + ", " + data.getRPRSNTV_NM() + ", " + data.getENTRPS_TELNO() + ", " + data.getDETAIL_ADDR());
            }
            Log.d(TAG, cnt1 + " is 1 " + cnt2 + " is 2 " + cnt3 + " is 3 " + cnt4 + " is 4 " + cnt5 + " is 5 ");
            setEntrpsJson(list);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static int index = 3800;
    static boolean check = false;
    private void setEntrpsJson(ArrayList<Prop.EntrpsJsonData> list) {
        ArrayList<Prop.EntrpsJsonData> temp = new ArrayList<Prop.EntrpsJsonData>();
        int max = 100;
        if(index + max >= list.size()) {
            max = list.size() - index;
            check = true;
        }
        for(int i = index; i < index + max; i++) {
            temp.add(list.get(i));
        }
        index += max;
//        if(!check) {
            sendEntrpsJson(list, temp);
//        }
    }
    @SuppressLint("CheckResult")
    private void sendEntrpsJson(ArrayList<Prop.EntrpsJsonData> list, ArrayList<Prop.EntrpsJsonData> temp) {
        AddEntrpsInfoService addEntrpsInfoService = Prop.INSTANCE.getRetrofit().create(AddEntrpsInfoService.class);
        Prop.AllEntrpsJsonData datas = new Prop.AllEntrpsJsonData(temp);
        addEntrpsInfoService.resultRepos(datas)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(item -> {
                    if(item.getResultCode().equals("success")) {
                        Log.d(TAG, "success : " + index);
                        if(!check) {
                            new Handler().postDelayed(new Runnable() {
                                @SuppressLint("CheckResult")
                                @Override
                                public void run() {
                                    setEntrpsJson(list);
//                                Log.d(TAG, "ASD");
                                }
                            }, 2000);
                        }
                    }
                }, e -> {
                    Log.d(TAG, "checkStatusOfRide " + e);
                });
    }

    /*
    json 파일 불러와서 String으로 리턴하기
     */
    private String loadJSONFile() {
        String json = "";

        try {
            InputStream is = getAssets().open("vowow_entrps_list.json");
            int fileSize = is.available();

            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");
            Log.d(TAG, fileSize + " , " + json.length());
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        return json;

//        Log.d(TAG, "loadJSONFIle start")
//        var result: String? = null
//
//        var fileinputStream: FileInputStream? = null
//        try {
//            fileinputStream = openFileInput(filename)
//            val size = fileinputStream!!.available()
//            val buffer = ByteArray(size)
//            fileinputStream.read(buffer)
//            fileinputStream.close()
//            result = String(buffer, charset("UTF-8"))
//        } catch (e: FileNotFoundException) {
//            Log.d(TAG, "사전에 등록증을 저장한 내역이 없습니다.")
//            return null
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return result
    }

    /*
    해당 kakaoID에 해당하는 닉네임이 존재하는지 체크
     */
    private void checkExistNickname() {
        NicknameAsyncTask nicknameAsyncTask = new NicknameAsyncTask();
        nicknameAsyncTask.execute("/user/getNickname");
    }

    private class NicknameAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String search_url = Prop.INSTANCE.serverUrl + strings[0];    // URL
            // 서버에 메세지 정보 전송
            try {
                // String type, ownerName, address, hp, petName, race, petColor, petBirth, neutralization, petGender;
                JSONObject jsonObject = new JSONObject();
                // Message에 담은 모든 정보 JSONObject에 담기
                jsonObject.accumulate("userId", KAKAO_ID); // key JSONObject에 담기

                // POST 전송방식을 위한 설정
                HttpURLConnection con = null;
                BufferedReader reader = null;

                Log.d(TAG, "url : " + search_url);
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
                if(responseCode == HttpURLConnection.HTTP_OK) { // 200 정상 연결
                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line;    // 한 줄씩 읽어오기 위한 임시 String 변수
                    while((line = reader.readLine()) != null){
                        buffer.append(line); // buffer에 데이터 저장
                    }
                    return buffer.toString(); //서버로 부터 받은 값을 리턴해줌
                }
                else {  // 연결이 잘 안됨
                    printConnectionError(con);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (TextUtils.isEmpty(result)) {
                Toast.makeText(getApplicationContext(), "서버 오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
            }
            else {
                JSONObject jsonObject = StringToJSON(result);
                try {
                    int getResult = jsonObject.getInt("resultCode");
                    Log.d(TAG, "result : " + getResult);
                    if (getResult == 0) {
                        Toast.makeText(getApplicationContext(), "닉네임이 없습니다. 닉네임을 생성해주세요.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), NicknamePopupActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        NICKNAME = jsonObject.getString("nickname");
                        Toast.makeText(getApplicationContext(), NICKNAME + " 님,\n반갑습니다!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
