package com.baselib.mvvmuse.view.fragment

import android.arch.lifecycle.ViewModelProviders
import com.baselib.instant.mvvm.view.AbsMvvmFragment
import com.baselib.mvpuse.R
import com.baselib.mvpuse.databinding.LayoutFragmentTwoBinding
import com.baselib.mvvmuse.viewmodel.OneFragmentViewModel
import com.baselib.mvvmuse.viewmodel.TwoFragmentViewModel
import java.util.*
import java.util.concurrent.TimeUnit


class TwoFragment : AbsMvvmFragment<LayoutFragmentTwoBinding, TwoFragmentViewModel>() {
    override fun initViewModel() = ViewModelProviders.of(this).get(TwoFragmentViewModel::class.java)
    override fun getContentLayout(): Int = R.layout.layout_fragment_two

    companion object {
        fun getInstance() = TwoFragment()
    }

    override fun lazyLoadData() {
        super.lazyLoadData()
        Timer().schedule(object : TimerTask() {
            override fun run() {
                showError()
            }
        }, TimeUnit.SECONDS.toMillis(2))
    }

}