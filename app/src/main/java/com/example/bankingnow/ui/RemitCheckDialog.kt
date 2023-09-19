package com.example.bankingnow.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import com.example.bankingnow.R
import com.example.bankingnow.databinding.DialogRemitCheckBinding
import com.example.bankingnow.databinding.DialogRemitPasswordBinding
import com.example.bankingnow.base.BaseDialogFragment
import com.example.bankingnow.model.RemitModel
import com.example.bankingnow.viewmodel.RemitViewModel
import java.util.Locale

class RemitCheckDialog(remitInfo: RemitModel) : BaseDialogFragment<DialogRemitCheckBinding>(R.layout.dialog_remit_check) {
    private val handler = Handler()
    private val remitInfo: RemitModel = remitInfo

    override fun initDataBinding() {
        super.initDataBinding()

        binding.tvRemitName.text = remitInfo.name
        binding.tvRemitBank.text = remitInfo.user.bank
        binding.tvRemitMoney.text = remitInfo.money
    }

    override fun initAfterBinding() {
        super.initAfterBinding()

        setTouchScreen()
        customTTS.speak("${remitInfo.name}님에게 보내겠습니까")
    }


    private fun setTouchScreen() {
        var startX = 0f
        var startY = 0f

        binding.dialogRemitCheck.setOnTouchListener { _, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                }

                MotionEvent.ACTION_UP -> {
                    val endX = event.x
                    val distanceX = endX - startX

                    // 스와이프를 감지하기 위한 조건 설정
                    if (distanceX > 100) {
                        // 오른쪽으로 스와이프
                        RemitAccountDialog().show(parentFragmentManager, "비밀 번호")
                        dismiss()
                    } else if (distanceX < -100) {
                        // 왼쪽으로 스와이프
                        RemitPasswordDialog().show(parentFragmentManager, "송금")
                        dismiss()
                    }
                }
            }
            true
        }
    }
}