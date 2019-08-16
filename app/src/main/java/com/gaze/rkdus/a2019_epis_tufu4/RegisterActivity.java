package com.gaze.rkdus.a2019_epis_tufu4;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;

import org.json.JSONException;
import org.json.JSONObject;

import pl.polidea.view.ZoomView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    TextView owner, resident, phone, resAddr, nowAddr, animal, variety, furColor, gender, neutralization, birthday, acqDate, special, owner_2;
    TextView year, month, date;

    String id, str_owner, str_animal;

    String pdf_name;

    int type = 0;
    // 1: 내장형 / 2 : 외장형 / 3 : 등록인식표

    final int MY_PERMISSION_REQUEST_STORAGE = 1234;
    private ImageButton pdf;
    FrameLayout frameLayout;
    String dirpath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        View v = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.zoom_item, null, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        long now = System.currentTimeMillis();
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat nameFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        String str_year = yearFormat.format(currentTime);
        String str_month = monthFormat.format(currentTime);
        String str_date = dayFormat.format(currentTime);
        pdf_name = nameFormat.format(currentTime);

        ZoomView zoomView = new ZoomView(this);
        zoomView.addView(v);
        zoomView.setLayoutParams(layoutParams);
        zoomView.setMiniMapEnabled(false); // 좌측 상단 검은색 미니맵 설정
        zoomView.setMaxZoom(2f); // 줌 Max 배율 설정  1f 로 설정하면 줌 안됩니다.

        FrameLayout container = (FrameLayout) findViewById(R.id.container);
        container.addView(zoomView);

        Intent intent = getIntent();
        type = intent.getIntExtra("type", 0);

        id = intent.getStringExtra("id");
        str_owner = intent.getStringExtra("owner");
        str_animal = intent.getStringExtra("animal");

        pdf_name = pdf_name + "_" + str_animal;

        owner = (TextView) findViewById(R.id.owner);
        owner_2 = (TextView) findViewById(R.id.owner_2);
        resident = (TextView) findViewById(R.id.resident);
        phone = (TextView) findViewById(R.id.phone);
        resAddr = (TextView) findViewById(R.id.resAddr);
        nowAddr = (TextView) findViewById(R.id.nowAddr);
        animal = (TextView) findViewById(R.id.animal);
        variety = (TextView) findViewById(R.id.variety);
        furColor = (TextView) findViewById(R.id.furColor);
        gender = (TextView) findViewById(R.id.gender);
        neutralization = (TextView) findViewById(R.id.neutralization_o);
        birthday = (TextView) findViewById(R.id.birthday);
        acqDate = (TextView) findViewById(R.id.acqDate);
        special = (TextView) findViewById(R.id.special);
        year = (TextView) findViewById(R.id.year);
        month = (TextView) findViewById(R.id.month);
        date = (TextView) findViewById(R.id.day);

        owner.setText(str_owner);
        owner_2.setText(str_owner);
        animal.setText(str_animal);

        year.setText(str_year);
        month.setText(str_month);
        date.setText(str_date);

        frameLayout = (FrameLayout) findViewById(R.id.framelayout);
        pdf = (ImageButton) findViewById(R.id.pdf);

        pdf.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //pdf.setVisibility(View.INVISIBLE);
                try {
                    checkPermission();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        new ReservationInfoData().execute(getResources().getString(R.string.url) + "/getReservationInfoData");

//        check = (ImageButton) findViewById(R.id.check);
//
//        check.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new ReservationCheck().execute(getResources().getString(R.string.url) + "/putChangeState");
//                finish();
//            }
//        });

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
                tmp.accumulate("owner_name", str_owner);
                tmp.accumulate("pet_name", str_animal);

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

                    owner.setText(jsonObject.getString("OWNER_NAME"));
                    resident.setText(jsonObject.getString("OWNER_RESIDENT"));
                    phone.setText(jsonObject.getString("OWNER_PHONE_NUMBER"));
                    resAddr.setText(jsonObject.getString("OWNER_ADDRESS1"));
                    nowAddr.setText(jsonObject.getString("OWNER_ADDRESS2"));
                    animal.setText(jsonObject.getString("PET_NAME"));
                    variety.setText(jsonObject.getString("PET_VARIETY"));
                    furColor.setText(jsonObject.getString("PET_COLOR"));

                    if (jsonObject.getString("PET_GENDER") == "1") {
                        gender.setText("남성");
                    } else {
                        gender.setText("여성");
                    }

                    if (jsonObject.getString("PET_NEUTRALIZATION") == "1") {
                        neutralization.setText("O");
                    } else {
                        neutralization.setText("X");
                    }


                    birthday.setText(jsonObject.getString("PET_BIRTH"));
                    acqDate.setText(jsonObject.getString("REGIST_DATE"));
                    special.setText(jsonObject.getString("ETC"));

                    Log.e(TAG, jsonObject.getString("OWNER_NAME"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.e(TAG, result);

        }
    }

    /* ReservationCheck : ID, 주인 이름, type 값을 통해 해당 데이터 상태를 등록 대기 상태로 변경
     *
     * type -> 1 : 내장형 / 2 : 외장형 / 3 : 등록인식표
     *
     * 변경 성공하면 -> int 1
     * 변경 실패하면 -> int 0
     *
     *  id/name/type이 일치하는 데이터의 state를 1에서 2로 변경!
     *
     * Uri  --->   /putChangeState
     * Parm  --->   {"user":{"id":"test","name":"김가연","type":1}} 전송
     * Result  --->   {"result":1} 결과 값 */

    public class ReservationCheck extends AsyncTask<String, String, String> {

        @Override

        protected String doInBackground(String... urls) {

            try {

                JSONObject jsonObject = new JSONObject();
                JSONObject tmp = new JSONObject();

                tmp.accumulate("id", id);
                tmp.accumulate("owner_name", str_owner);
                tmp.accumulate("type", type);

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
            int success = 0;

            try {
                json = new JSONObject(result);

                if (json.get("result") == null) {
                    new ReservationCheck().execute(getResources().getString(R.string.url) + "/putChangeState");
                } else {
                    success = (int) json.get("result");

                    if (success == 1) {
                        Toast.makeText(getApplicationContext(), "예약확인!!", Toast.LENGTH_LONG).show();

                        String tel = "tel:" + phone.getText().toString();
                        startActivity(new Intent("android.intent.action.DIAL", Uri.parse(tel)));

                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.e(TAG, result);

        }
    }

    public void shareFIle(File shareFile) {

        final Intent intent = new Intent();

        intent.setAction(Intent.ACTION_SEND);           // 단일파일 보내기

//        intent.setPackage("com.google.android.gm");   // 지메일로 보내기

        // 파일형태에 맞는 type설정

        MimeTypeMap type = MimeTypeMap.getSingleton();

        intent.setType(type.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(shareFile.getPath())));

//        intent.setType("plain/text"); // text 형태로 전달

//        intent.setType("*/*");        // 모든 공유 형태 전달

        intent.putExtra(Intent.EXTRA_SUBJECT, "공유 제목");  // 제목

        intent.putExtra(Intent.EXTRA_TEXT, "공유 내용");     // 내용

        Log.i(TAG, Environment.getExternalStorageDirectory() + File.separator + pdf_name + ".jpg");

        if (shareFile != null) {

            Uri contentUri = FileProvider.getUriForFile(this,

                    getApplicationContext().getPackageName() + ".fileprovider", shareFile); // manifest의  ${applicationId}.fileprovider



            intent.putExtra(Intent.EXTRA_STREAM, contentUri); // 단일 파일 전송

        }

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);     // 공유 앱에 권한 주기

        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);    // 공유 앱에 권한 주기

        startActivity(Intent.createChooser(intent, "공유 타이틀"));

    }

    public void layoutToImage() throws IOException {


        Bitmap bm = Bitmap.createBitmap(frameLayout.getWidth(), frameLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        Drawable bgDrawable = frameLayout.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        frameLayout.draw(canvas);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File f = new File(Environment.getExternalStorageDirectory() + File.separator + pdf_name + ".jpg");
        try {

            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());

            Document document = new Document();
            dirpath = Environment.getExternalStorageDirectory().toString();

            PdfWriter.getInstance(document, new FileOutputStream(dirpath + "/" + pdf_name + ".pdf"));
            document.open();

            Image image = Image.getInstance(f.toString());
            float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                    - document.rightMargin() - 0) / image.getWidth()) * 100;
            image.scalePercent(scaler);
            image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);
            document.add(image);
            document.close();

            f.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();

        dirpath = Environment.getExternalStorageDirectory().toString();
        String text = "file://"+dirpath + "/" + pdf_name + ".pdf";

//
//        File file = new File(text);
//
//        intent.setAction(Intent.ACTION_SEND);
//        intent.setType("application/*");
//        //intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(new File(text).toString()));
//        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(new File(text).toString()));
//        Intent chooser = Intent.createChooser(intent, "공유하기");
//        startActivity(chooser);
//        //shareFIle(f);

//        Toast.makeText(RegisterActivity.this, Uri.parse(new File(text).toString()).toString(), Toast.LENGTH_LONG).show();
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setType("application/pdf");
//        shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "abc@gmail.com" });
//        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "test " +    "test!!!");
//        shareIntent.putExtra(Intent.EXTRA_TEXT, "test");
//        shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse(new File(text).toString()).toString());
//        startActivity(shareIntent);

        createCachedFile(RegisterActivity.this, pdf_name+".pdf", "This is a test");

        startActivity(getSendEmailIntent( RegisterActivity.this, "gayeon@naver.com", "Test", "See attached", pdf_name+".pdf"));

//        createCachedFile(RegisterActivity.this, pdf_name+".pdf", "Test!!!");
//        Toast.makeText(RegisterActivity.this, Uri.parse(new File(text).toString()).toString(), Toast.LENGTH_LONG).show();
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setType("application/pdf");
//        shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "abc@gmail.com" });
//        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "test " +    "test!!!");
//        shareIntent.putExtra(Intent.EXTRA_TEXT, "test");
////        shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse(new File(text).toString()).toString());
//        shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse("content://" + CachedFileProvider.AUTHORITY + "/"+ pdf_name+ ".pdf"));
//        startActivity(shareIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() throws IOException {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to write the permission.
                Toast.makeText(this, "Read/Write external storage", Toast.LENGTH_SHORT).show();
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSION_REQUEST_STORAGE);

            // MY_PERMISSION_REQUEST_STORAGE is an
            // app-defined int constant

        } else {
            // 다음 부분은 항상 허용일 경우에 해당이 됩니다.
            layoutToImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    try {
                        layoutToImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // permission was granted, yay! do the
                    // calendar task you need to do.

                } else {

                    Log.d(TAG, "Permission always deny");

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
        }
    }

    public static void createCachedFile(Context context, String fileName, String content) throws IOException {

        File cacheFile = new File(context.getCacheDir() + File.separator + fileName);
        cacheFile.createNewFile();

        FileOutputStream fos = new FileOutputStream(cacheFile);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
        PrintWriter pw = new PrintWriter(osw);

        pw.println(content);

        pw.flush();
        pw.close();
    }

    public static Intent getSendEmailIntent(Context context, String email, String subject, String body, String fileName) {

        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        //Explicitly only use Gmail to send
        emailIntent.setClassName("com.google.android.gm","com.google.android.gm.ComposeActivityGmail");

        emailIntent.setType("plain/text");

        //Add the recipients
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { email });

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

        //Add the attachment by specifying a reference to our custom ContentProvider
        //and the specific file of interest
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://" + CachedFileProvider.AUTHORITY + "/" + fileName));

        return emailIntent;
    }
}
