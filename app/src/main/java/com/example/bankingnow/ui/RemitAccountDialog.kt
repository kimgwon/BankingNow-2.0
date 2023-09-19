package com.example.bankingnow.ui

import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.MotionEvent
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.bankingnow.R
import com.example.bankingnow.databinding.DialogRemitAccountBinding
import com.example.bankingnow.base.BaseDialogFragment
import com.example.bankingnow.event.NumberPublicEvent
import com.example.bankingnow.util.Recorder
import com.example.bankingnow.viewmodel.RemitViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Date

class RemitAccountDialog : BaseDialogFragment<DialogRemitAccountBinding>(R.layout.dialog_remit_account) {
    private val viewModel by lazy {
        ViewModelProvider(requireParentFragment())[RemitViewModel::class.java]
    }

    private var remitResultIsFill: Boolean = false

    private val handler = Handler()

    private val filePath = Environment.getExternalStorageDirectory().absolutePath + "/Download/" + Date().time.toString() + ".aac"
    private var recorder = Recorder()

    private val stateList: Array<String> = arrayOf("FAIL", "RECORD_START", "SUCCESS")
    private val idx: MutableLiveData<Int> = MutableLiveData(0)
    private lateinit var state: String

    private val isResponse: MutableLiveData<Boolean> = MutableLiveData(false)
    private val result: MutableLiveData<String> = MutableLiveData()

    override fun initDataBinding() {
        super.initDataBinding()

        idx.observe(viewLifecycleOwner) {
            state = stateList[idx.value!!]
        }

        result.observe(viewLifecycleOwner) {
            binding.tvAccount.text = it
        }
    }

    override fun initAfterBinding() {
        super.initAfterBinding()

        setTouchScreen()

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
            }

            override fun onDone(utteranceId: String?) {
                // 말하기가 완료된 후 실행할 코드
                // tts 이벤트는 UI 쓰레드에서 호출해야 함
                Handler(Looper.getMainLooper()).post {
                    if (isResponse.value!!)
                        recorder.startOneRecord(filePath, true)
                }
            }

            override fun onError(utteranceId: String?) {
            }
        })

        viewModel.remitLiveData?.observe(viewLifecycleOwner) {
            remitResultIsFill = viewModel.getRemit()!!.isFill
        }

        result.observe(viewLifecycleOwner) {
            viewModel.setRemitAccount(it)
        }
    }

    override fun onStart() {
        super.onStart()
        // EventBus 등록
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        // EventBus 해제
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNumberEvent(event: NumberPublicEvent) {
        if (event.isSuccess){
            isResponse.postValue(true)
            customTTS.speak(event.result.predicted_number)
            result.postValue(result.value + event.result.predicted_number)
            recorder.startOneRecord(filePath, true)
        } else{
            isResponse.postValue(false)
            customTTS.speak("네트워크 연결이 안되어있습니다.")
            idx.postValue(1)
        }
    }

    private fun setTouchScreen() {
        var startX = 0f
        var startY = 0f

        binding.dialogRemitAccount.setOnTouchListener { _, event ->
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
                        RemitBankDialog().show(parentFragmentManager,"송금 계좌")
                        dismiss()
                    } else if (state=="SUCCESS" && distanceX < -100){
                        // 왼쪽으로 스와이프
                        if (remitResultIsFill)
                            setFragmentResult("Check", bundleOf("isFill" to true))
                        else
                            setFragmentResult("Check", bundleOf("isFill" to false))

                        dismiss()
                    } else if (distanceX>-10 && distanceX<10){
                        // 클릭으로 처리
                        when (state) {
                            "FAIL" -> {
                                idx.postValue(1)
                                recorder.startOneRecord(filePath, true)
                            }
                            "RECORD_START" -> {
                                idx.postValue(2)
                                recorder.stopRecording()
                                customTTS.speak("1000원. 다시 입력하시려면 터치, 다음 단계로 가시려면 오른쪽으로 스와이프 해주세요.")
                            }
                            "SUCCESS" -> {
                                idx.postValue(1)
                                recorder.startOneRecord(filePath, true)
                            }
                        }
                    }
                }
            }
            true
        }
    }
}