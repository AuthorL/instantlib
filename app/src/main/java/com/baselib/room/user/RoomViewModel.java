package com.baselib.room.user;

import android.app.Application;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;

import com.baselib.instant.mvvm.viewmodel.BaseViewModel;
import com.baselib.instant.util.LogUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


public class RoomViewModel extends BaseViewModel<RoomModel> {

    private ObservableField<String> mLocalData;
    private ObservableField<String> mSaveState;
    private MutableLiveData<String> mUsernameError = new MutableLiveData<>();
    private MutableLiveData<String> mPasswordError = new MutableLiveData<>();

    private MediatorLiveData<List<UserEntity>> mShowUserLiveData = new MediatorLiveData<>();
    private LiveData<List<UserEntity>> allUserSourse;

    public LiveData<String> getUserNameError() {
        return mUsernameError;
    }

    public LiveData<String> getPasswordError() {
        return mPasswordError;
    }

    public ObservableField<String> getSaveState() {
        return mSaveState;
    }

    public ObservableField<String> getLocalData() {
        return mLocalData;
    }

    public LiveData<List<UserEntity>> getShowUserLiveData() {
        return mShowUserLiveData;
    }

    public RoomViewModel(@NotNull Application application, @Nullable RoomModel model) {
        super(application, model);
    }

    @Override
    public void onViewModelStart() {
        super.onViewModelStart();
        mLocalData = new ObservableField<>("暂无");
        mSaveState = new ObservableField<>("save");
    }

    public RoomViewModel(@NotNull Application application) {
        this(application, null);
    }

    public static ViewModelProvider.Factory getFactory(@NotNull Application application) {
        return new ViewModelProvider.Factory() {

            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                T instance;
                try {
                    instance = modelClass.getConstructor(Application.class, RoomModel.class).newInstance(application, new RoomModel(application));
                } catch (Exception e) {
                    e.printStackTrace();
                    instance = null;
                }
                return instance;
            }
        };
    }

    public void addUser(Editable userId, Editable userName, Editable password) {
        mSaveState.set("正在保存用户资料");
        if (TextUtils.isEmpty(userName)) {
            mUsernameError.postValue("用户名不能为空");
            mSaveState.set("条件不满足");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordError.postValue("密码不能为空");
            mSaveState.set("条件不满足");
            return;
        }
        if (TextUtils.isEmpty(userId)) {
            mPasswordError.postValue("id不能为空");
            mSaveState.set("条件不满足");
            return;
        }

        mSaveState.set("保存用户资料");

        getModel().insertUser(userId.toString(),userName.toString(),password.toString());


    }

    public void loadLocalUser() {
        if (allUserSourse!=null){
            mShowUserLiveData.removeSource(allUserSourse);
        }
        allUserSourse = getModel().getAllUser();
        mShowUserLiveData.addSource(allUserSourse, userEntities -> mShowUserLiveData.postValue(userEntities));
    }

    @Override
    public void onDestroy(@NotNull LifecycleOwner owner) {
        mShowUserLiveData.removeSource(allUserSourse);
        super.onDestroy(owner);
    }

    public void loadOneUser() {
        Disposable subscribe = getModel().getOneUser().subscribe(userEntities -> {
            LogUtils.i("收到加载单一用户的数据"+userEntities);
            ArrayList<UserEntity> list = new ArrayList<>();
            list.add(userEntities);
            mShowUserLiveData.postValue(list);
        });
        addDisposable(subscribe);
    }
}
