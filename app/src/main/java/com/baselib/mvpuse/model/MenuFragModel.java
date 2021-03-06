package com.baselib.mvpuse.model;

import android.content.Context;

import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.mvp.BaseModel;
import com.baselib.instant.net.base.IHttpStateCallback;
import com.baselib.instant.executor.ThreadExecutorProxy;
import com.baselib.instant.util.LogUtils;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Response;

public class MenuFragModel extends BaseModel {
    public MenuFragModel() {
        ThreadExecutorProxy threadExecutorProxy = (ThreadExecutorProxy) GlobalManager.Companion.getManager(GlobalManager.EXECUTOR_POOL_SERVICE);
        threadExecutorProxy.runOnAsyncThread(() -> LogUtils.d("这是异步运行在的线程" + Thread.currentThread().getId()));
    }

    public Response getJokeByOkhttp() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("page",1);
        map.put("count",2);
        map.put("type","video");
        IHttpStateCallback stateCallback = new IHttpStateCallback() {
            @Override
            public void reqBefore(String url) {
                LogUtils.d("即将请求的url为" + url);
            }

            @Override
            public void reqStart() {
                LogUtils.d("开始请求网络");
            }

            @Override
            public void reqSuccess(Response response) {
                LogUtils.d("网络请求成功");

            }

            @Override
            public void reqFail(String failInfo) {
                LogUtils.d("请求失败");
            }

            @Override
            public void reqError(IOException e) {
                LogUtils.d("请求错误"+e.getMessage());
            }

            @Override
            public void reqFinish() {
                LogUtils.d("请求结束");
            }
        };
        return GlobalManager.Companion.getNetworkManager().executeGet("https://api.apiopen.top/getJoke", map, stateCallback);
    }

    @Override
    public void onModelDetach(Context context) {
        super.onModelDetach(context);
        GlobalManager.Companion.onDestroy();
    }
}
