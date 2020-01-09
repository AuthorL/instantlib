package com.baselib.mvpuse.presenter;

import android.content.Context;

import com.baselib.instant.breakpoint.BreakPointHelper;
import com.baselib.instant.breakpoint.Task;
import com.baselib.instant.breakpoint.TaskPostListener;
import com.baselib.instant.mvp.BasePresenter;
import com.baselib.instant.util.LogUtils;
import com.baselib.mvpuse.model.BreakPointModel;
import com.baselib.mvpuse.view.BreakPointView;

public class BreakPointPresenter extends BasePresenter<BreakPointView, BreakPointModel> implements TaskPostListener {
    @Override
    public BreakPointModel initModel() {
        return new BreakPointModel();
    }

    public void pauseTask() {
        if (BreakPointHelper.getInstance().pauseTask(getModel().getTaskId())) {
            getView().onTaskPause();
        } else {
            getView().setTextContent("不存在该任务");
        }
    }

    public void startTask(Context context) {
        final Task task = new Task.Builder()
                .setTaskUrl("https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk")
//                            .setTaskFileName("downtask.apk")
                .setTaskFileDir(context.getFilesDir().getAbsolutePath())
                .build();


        task.addTaskListener(this);
        BreakPointHelper.getInstance().postTask(context, task);
    }

    @Override
    public void postNewTaskSuccess(int taskId) {
        getModel().saveId(taskId);
        LogUtils.i("任务添加成功,等待下载,id为" + taskId);
    }

    @Override
    public void onTaskDownloadError(String message) {
        getView().onTaskDownloadError(message);
    }

    @Override
    public void onTaskProgressUpdate(long taskTotalSize, long length) {
        if (getModel().shouldUpdateView(taskTotalSize, length)) {
            getView().onTaskProgressUpdate(taskTotalSize, length, getModel().getPercentage(taskTotalSize, length));
        }
    }

    @Override
    public void onTaskDownloadFinish() {
        LogUtils.i("任务下载完成");
        getView().onTaskDownloadFinish(BreakPointHelper.getInstance().getTaskPath(getModel().getTaskId()));


    }

    @Override
    public void onTaskCancel() {
        LogUtils.i("任务取消");
        getView().onTaskCancel();
    }

    @Override
    public void onTaskDownloadStart(String downloadUrl, long contentLength) {
        LogUtils.i("下载开始,文件来源:" + downloadUrl);
        getView().onTaskDownloadStart(downloadUrl, contentLength);


    }

    @Override
    public void onTaskPause() {
        getView().onTaskPause();
    }

    public void cancelTask() {
        BreakPointHelper.getInstance().removeTask(getModel().getTaskId());
    }

    public void saveLength(long contentLength) {
        getModel().saveLength(contentLength);
    }

    public long getLength() {
        return getModel().getContentLength();
    }



}