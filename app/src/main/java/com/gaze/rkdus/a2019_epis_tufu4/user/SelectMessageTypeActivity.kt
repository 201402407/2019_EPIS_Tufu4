package com.gaze.rkdus.a2019_epis_tufu4.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import com.gaze.rkdus.a2019_epis_tufu4.BaseActivity
import com.gaze.rkdus.a2019_epis_tufu4.R
import com.gaze.rkdus.a2019_epis_tufu4.user.HospitalProfileActivity.START_RESERVATION
import kotlinx.android.synthetic.main.activity_select_message_type.*

class SelectMessageTypeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_message_type)
        var msgIntent: Intent? = null

        if (intent == null)
            finishPopup()

        intent.let {
            if (!intent.hasExtra("key"))
                finishPopup()
            if (!intent.hasExtra("hospitalName"))
                finishPopup()
        }

        // 반려동물 등록 예약 클릭 시
        applyTypeImg.setOnTouchListener { _, event ->
            if(event?.action == MotionEvent.ACTION_DOWN) {
                msgIntent = Intent(applicationContext, MessageActivity::class.java)
                msgIntent!!.putExtra("key", intent.getIntExtra("key", 0))
                msgIntent!!.putExtra("hospitalName", intent.getStringExtra("hospitalName"))
                startActivityForResult(msgIntent, START_RESERVATION)
            }
            false
        }

        // 예방접종 예약 클릭 시
        vaccinTypeImg.setOnTouchListener { _, event ->
            if(event?.action == MotionEvent.ACTION_DOWN) {
                msgIntent = Intent(applicationContext, VaccineMessageActivity::class.java)
                msgIntent!!.putExtra("key", intent.getIntExtra("key", 0))
                msgIntent!!.putExtra("hospitalName", intent.getStringExtra("hospitalName"))
                startActivityForResult(msgIntent, START_RESERVATION)
            }
            false
        }

        // 건강검진 클릭 시
        healthcheckupTypeImg.setOnTouchListener { _, event ->
            if(event?.action == MotionEvent.ACTION_DOWN) {
                msgIntent = Intent(applicationContext, HealthCheckupMessageActivity::class.java)
                msgIntent!!.putExtra("key", intent.getIntExtra("key", 0))
                msgIntent!!.putExtra("hospitalName", intent.getStringExtra("hospitalName"))
                startActivityForResult(msgIntent, START_RESERVATION)
            }
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {
            START_RESERVATION -> if (resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                finish()
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
            else -> {
            }
        }
    }
}
