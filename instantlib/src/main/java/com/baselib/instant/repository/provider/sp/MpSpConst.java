package com.baselib.instant.repository.provider.sp;

/**
 * 跨进程sp文件常量
 *
 * @author wsb
 * */
public interface MpSpConst {
    String KEY = "value";
    String KEY_NAME = "name";
    String PATH_WILDCARD = "*/";
    String PATH_GET_ALL = "getAll";
    String PATH_GET_STRING = "getString";
    String PATH_GET_INT = "getInt";
    String PATH_GET_LONG = "getLong";
    String PATH_GET_FLOAT = "getFloat";
    String PATH_GET_BOOLEAN = "getBoolean";
    String PATH_CONTAINS = "contains";
    String PATH_APPLY = "apply";
    String PATH_COMMIT = "commit";

    String PATH_REGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER = "registerOnSharedPreferenceChangeListener";
    String PATH_UNREGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER = "unregisterOnSharedPreferenceChangeListener";
    String SP_CHANGE_LISTENER_KEY = null;

    int GET_ALL = 1;
    int GET_STRING = 2;
    int GET_INT = 3;
    int GET_LONG = 4;
    int GET_FLOAT = 5;
    int GET_BOOLEAN = 6;
    int CONTAINS = 7;
    int APPLY = 8;
    int COMMIT = 9;
    int REGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER = 10;
    int UNREGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER = 11;
}
