package com.gaze.rkdus.a2019_epis_tufu4.popup;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gaze.rkdus.a2019_epis_tufu4.BaseActivity;
import com.gaze.rkdus.a2019_epis_tufu4.R;
import com.gaze.rkdus.a2019_epis_tufu4.item.MyReservationData;

/*
예약하기 버튼 클릭 시 나타나는 팝업창
 */
public class MessagePopupActivity extends BaseActivity {
    public static final String TAG = "LogGoGo";
    Button okBtn, cancelBtn;
    TextView tvMessagePopup;
    String messageText;
    Intent intent, resultIntent;
    MyReservationData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_popup);

        okBtn = (Button) findViewById(R.id.okBtn);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        tvMessagePopup = (TextView) findViewById(R.id.messagePopupText);

        resultIntent = new Intent();    // 결과로 전송할 인텐트
        intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra("messageType")) {
                messageText = intent.getStringExtra("messageType");
            }
            else
                finishPopup();
        }
        else
            finishPopup();

        switch (messageText) {
            case "reservation":
                tvMessagePopup.setText(R.string.reservationPopupMsg);
                break;
            case "payment":
                tvMessagePopup.setText("결제 하시겠습니까?");
                break;
        }

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //데이터 전달하기
                setResult(RESULT_OK, resultIntent);
                //액티비티(팝업) 닫기
                finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //데이터 전달하기
                setResult(RESULT_CANCELED, resultIntent);
                //액티비티(팝업) 닫기
                finish();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }
    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}
