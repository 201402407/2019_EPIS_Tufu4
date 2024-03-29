package com.gaze.rkdus.a2019_epis_tufu4.popup;

import android.content.Context;
import android.os.Vibrator;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.gaze.rkdus.a2019_epis_tufu4.R;

public class ManagementPopupActivity extends AppCompatActivity {
    TextView tvManagementMsg, tvManagementTitle;
    Button okBtn;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management_popup);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                // 키잠금 해제하기
//                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                // 화면 켜기
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // 진동
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {50, 500, 100, 500};  // 대기, 진동, 대기, 진동 순서
        vibrator.vibrate(pattern, -1);

        tvManagementMsg = (TextView) findViewById(R.id.managementPopupText);
        okBtn = (Button) findViewById(R.id.okBtn);
        tvManagementTitle = (TextView) findViewById(R.id.tvManagementTitle);

        tvManagementTitle.setText("등록이 완료되었습니다.");
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
