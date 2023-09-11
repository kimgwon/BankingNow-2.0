package com.example.bankingnow.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.fragment.findNavController
import com.example.bankingnow.R
import com.example.bankingnow.databinding.FragmentMainBinding
import com.example.bankingnow.databinding.FragmentRemitBinding
import com.example.writenow.base.BaseFragment

class MainFragment : BaseFragment<FragmentMainBinding>(R.layout.fragment_main) {
    override fun initStartView() {
        super.initStartView()

        // 송금 금액 다이얼로그
        val dialog = RemitPasswordDialog()
        dialog.show(parentFragmentManager,"")
    }

    override fun initDataBinding() {
        super.initDataBinding()


    }


    override fun initAfterBinding() {
        super.initAfterBinding()

        binding.btnBalance.setOnClickListener{
            vibratePhone()
            navController.navigate(R.id.action_mainFragment_to_balanceFragment)
        }
        binding.btnRemit.setOnClickListener{
            vibratePhone()
            navController.navigate(R.id.action_mainFragment_to_remitFragment)
        }
    }

}