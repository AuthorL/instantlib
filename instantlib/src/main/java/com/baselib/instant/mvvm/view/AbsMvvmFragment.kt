package com.baselib.instant.mvvm.view

import android.arch.lifecycle.LifecycleObserver
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.RelativeLayout
import com.baselib.instant.R
import com.baselib.instant.databinding.LayoutBaseFragmentBinding
import com.baselib.instant.mvvm.viewmodel.IViewModel
import com.baselib.instant.util.LogUtils

/**
 * mvvm架构v层fragment基类
 *
 * V层只关注DB对象和VM对象，业务交由VM对象处理，界面交给DB对象进行操作
 *
 * @author wsb
 * */
abstract class AbsMvvmFragment<DB : ViewDataBinding, VM : IViewModel> : Fragment(), IViewOperate, IViewBuilder {
    /**
     * 加载中
     * */
    private var loadingView: View? = null

    /**
     * 加载失败
     * */
    private var errorView: View? = null

    /**
     * 空布局
     * */
    private var emptyView: View? = null

    /**
     * 加载动画
     * */
    private var animationDrawable: AnimationDrawable? = null

    /**
     * 准备添加的布局对象
     */
    protected lateinit var dataBinding: DB

    /**
     * viewModel
     */
    protected var viewModel: VM? = null

    /**
     * 根布局对象
     * */
    private lateinit var rootDataBinding: LayoutBaseFragmentBinding

    /**
     * 失败后点击刷新
     */
    override fun onRefresh() {
        LogUtils.d("点击进行页面重新刷新")
    }

    protected var prepared: Boolean = false
    protected var loadedOnce: Boolean = false
    protected var visible: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return DataBindingUtil.inflate<LayoutBaseFragmentBinding>(inflater, getBaseLayout(), container, false)?.let { rootBinding ->
            rootDataBinding = rootBinding

            dataBinding = DataBindingUtil.inflate<DB>(inflater, getContentLayout(), null, false).apply {
                root.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }

            rootBinding.rltContainer.addView(dataBinding.root)
            rootBinding.root
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loadingView = initLoadingView()

        animationDrawable = initLoadAnimate()

        emptyView = initEmptyView()

        errorView = initErrorView()?.apply {
            isClickable = true
            setOnClickListener {
                showLoading()
                onRefresh()
            }
        }

        viewModel = this.initViewModel().also {
            lifecycle.addObserver(it as LifecycleObserver)
        }
        prepared = true


    }

    override fun initEmptyView(): View? = getView<ViewStub>(R.id.vs_empty).takeIf { it != null }?.let { vs ->
        getEmptyLayout().takeIf { it > 0 }?.run {
            vs.layoutResource = this
        }
        vs.inflate()
    }

    /**
     * 获取指定的加载空结果布局
     *
     * 子类可以重写定义该界面的加载空结果展示
     * */
    override fun getEmptyLayout(): Int = R.layout.layout_loading_empty

    /**
     * 获取加载阶段的动画
     * */
    override fun initLoadAnimate(): AnimationDrawable? = loadingView?.let {
        it.findViewById<ImageView>(R.id.img_progress).drawable as AnimationDrawable
    }

    abstract fun initViewModel(): VM


    override fun initLoadingView(): View? = getView<ViewStub>(R.id.vs_loading).takeIf { it != null }?.let { vs ->
        getLoadingLayout().takeIf { it > 0 }?.run {
            vs.layoutResource = this
        }

        vs.inflate()
    }

    override fun initErrorView(): View? = getView<ViewStub>(R.id.vs_error_refresh).takeIf { it != null }?.let { vs ->
        getErrorLayout().takeIf { it > 0 }?.run {
            vs.layoutResource = this
        }
        vs.inflate()
    }

    override fun getBaseLayout(): Int = R.layout.layout_base_fragment

    /**
     * 获取指定的加载错误布局
     *
     * 子类可以重写定义该界面的加载错误展示
     * */
    override fun getErrorLayout(): Int = R.layout.layout_loading_error

    /**
     * 获取指定的加载阶段布局
     *
     * 子类可以重写定义该界面的加载阶段展示
     * */
    open fun getLoadingLayout(): Int = R.layout.layout_loading_view


    protected fun <T : View> getView(id: Int): T? {
        return view.takeIf { it != null }?.findViewById(id)
    }

    override fun showLoading() = activity?.runOnUiThread {
        LogUtils.i("${this::class.java.simpleName} showLoading")
        errorView?.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        emptyView?.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        dataBinding.root.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        loadingView?.apply {
            if (visibility != View.VISIBLE) {
                visibility = View.VISIBLE
            }
        }

        animationDrawable?.apply {
            if (!isRunning) {
                start()
            }
        }
    }

    override fun showContentView() = activity?.runOnUiThread {
        LogUtils.i("${this::class.java.simpleName} showContentView")
        errorView?.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        emptyView?.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        loadingView?.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        dataBinding.root.apply {
            if (visibility != View.VISIBLE) {
                visibility = View.VISIBLE
            }
        }

        animationDrawable?.apply {
            if (isRunning) {
                stop()
            }
        }
    }

    override fun showError() = activity?.runOnUiThread {
        LogUtils.i("${this::class.java.simpleName} showError")
        loadingView?.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        emptyView?.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        dataBinding.root.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        errorView?.apply {
            if (visibility != View.VISIBLE) {
                visibility = View.VISIBLE
            }
        }

        animationDrawable?.apply {
            if (isRunning) {
                stop()
            }
        }
    }

    override fun showEmpty() = activity?.runOnUiThread {
        LogUtils.i("${this::class.java.simpleName} showEmpty")
        loadingView?.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        errorView?.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        dataBinding.root.apply {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }

        emptyView?.apply {
            if (visibility != View.VISIBLE) {
                visibility = View.VISIBLE
            }
        }

        animationDrawable?.apply {
            if (isRunning) {
                stop()
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (userVisibleHint) {
            visible = true
            onVisible()
        } else {
            visible = false
            onInvisible()
        }
    }

    open fun onInvisible() {}

    /**
     * 显示时加载数据,需要这样的使用
     * 注意声明 isPrepared，先初始化
     * 生命周期会先执行 setUserVisibleHint 再执行onActivityCreated
     * 在 onActivityCreated 之后第一次显示加载数据，只加载一次
     */
    open fun lazyLoadData() {
        LogUtils.d(this::class.java.simpleName + " 首次被显示,加载数据")
    }

    open fun onVisible() {
//        viewModel?.takeIf {
//            visible && !it.getLoadedOnceState() && it.getPreparedState()
//        }?.apply {
        if (prepared&&visible && !loadedOnce) {
            loadedOnce=true
//            dataBinding?.root.postDelayed({
                showLoading()
                lazyLoadData()
//            }, 100)
//        }
        }
    }

    override fun onDestroy() {
        animationDrawable?.run {
            if (isRunning) {
                stop()
            }
        }

        viewModel.run {
            lifecycle.removeObserver(this as LifecycleObserver)
        }

        super.onDestroy()
    }

}