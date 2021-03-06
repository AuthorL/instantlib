package com.baselib.instant.net.base;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

/**
 * 网络请求客户端对象
 * <p>
 * 各实现类实现各方法,只关注接口,底层由okhttp还是urlconnection实现不关注
 *
 * @author wsb
 */
public interface INetClient {
    /**
     * 执行get请求
     *
     * @param url           请求链接
     * @param params        请求参数
     * @param stateCallback 网络状态回调
     */
    void reqGet(String url, Map<String, Object> params, IHttpStateCallback stateCallback);

    /**
     * 执行post请求
     *
     * @param url           请求链接
     * @param params        请求参数
     * @param stateCallback 网络状态回调
     */
    void reqPost(String url, Map<String, Object> params, IHttpStateCallback stateCallback);

    /**
     * 销毁阶段回调
     */
    void detach();

    /**
     * 客户端设置网络请求client相关配置
     *
     * @param config 客户端对网络请求client的要求
     */
    void setConfig(NetConfig config);

    /**
     * 同步方式get请求
     *
     * @param url           请求链接
     * @param params        请求参数
     * @param stateCallback 网络状态回调
     * @return 返回响应对象
     */
    Response executeGet(String url, HashMap<String, Object> params, IHttpStateCallback stateCallback);
}
