package com.baselib.mvvmuse.view.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.text.TextUtils
import com.baselib.instant.mvvm.view.AbsMvvmFragment
import com.baselib.use.R
import com.baselib.mvvmuse.viewmodel.HostFragmentViewModel
import com.baselib.use.databinding.LayoutFragmentHostBinding

class HostFragment : AbsMvvmFragment<LayoutFragmentHostBinding, HostFragmentViewModel>() {
    private lateinit var pageList: MutableList<Fragment>
    private lateinit var titleList: MutableList<String>

    companion object {
        @JvmOverloads
        fun getInstance(type: String? = null): HostFragment = HostFragment().apply {
            if (!TextUtils.isEmpty(type)) {
                arguments = Bundle().apply {
                    putString("type", type)
                }
            }
        }
    }

    override fun initViewModel(): HostFragmentViewModel = ViewModelProviders.of(this).get(HostFragmentViewModel::class.java)

    override fun getContentLayout(): Int = R.layout.layout_fragment_host

    override fun lazyLoadData() {
        super.lazyLoadData()

        initPageList()
        initTitleList()

        dataBinding.vpPage.offscreenPageLimit = 2
        dataBinding.vpPage.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(p0: Int): Fragment = pageList[p0]

            override fun getCount(): Int = titleList.size

            override fun getPageTitle(position: Int): CharSequence? = titleList[position]

        }.apply {
            notifyDataSetChanged()
        }

        dataBinding.tabHost.setupWithViewPager(dataBinding.vpPage, false)
        showContentView()
    }

    private fun initTitleList() {
        titleList = mutableListOf<String>().apply {
            add("首页")
            add("资讯")
            add("个人")
        }
    }

    private fun initPageList() {
        pageList = mutableListOf<Fragment>().apply {
            add(OneFragment.getInstance())
            add(TwoFragment.getInstance())
            add(ThreeFragment.getInstance())
        }
    }

}