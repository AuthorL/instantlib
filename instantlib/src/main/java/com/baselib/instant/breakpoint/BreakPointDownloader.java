package com.baselib.instant.breakpoint;

import android.content.Context;
import android.os.Looper;

import com.baselib.instant.breakpoint.bussiness.DownloadExecutor;
import com.baselib.instant.breakpoint.database.DataBaseRepository;
import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;
import com.baselib.instant.breakpoint.utils.BreakPointConst;
import com.baselib.instant.breakpoint.utils.DataCheck;
import com.baselib.instant.breakpoint.utils.DataUtils;
import com.baselib.instant.manager.BusinessHandler;
import com.baselib.instant.util.LogUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 断点下载执行类
 *
 * @author wsb
 */
class BreakPointDownloader {

    private DataBaseRepository mDatabaseRepository;

    private StreamProcessor mStreamProcessor = new OkHttpSteamProcessor();

    private final DownloadExecutor mExecutor = new DownloadExecutor();

    private final BusinessHandler mHandler = new BusinessHandler(Looper.getMainLooper(), msg -> {

    });

    /**
     * 执行指定下载任务
     * <p>
     * 下载任务将经历以下阶段
     * <p>
     * 1.任务下载链接预检查阶段,确定链接是否可用,是否出现重定向 {@link Task#preload(PreloadListener)}
     * <p>
     * 2.请求文件流得知完整文件大小{@link #getFileStream(Task, String)},创建占位文件并明确分段下载起始点
     * <p>
     * 3.解析文件流开始分段下载任务{@link Task#parseSegment(long, Task.SegmentTaskEvaluator)}
     *
     * @param context 上下文
     * @param task    任务对象
     */
    public void executeTask(Context context, Task task) {
        final PreloadListener preloadListener = new PreloadListener() {
            @Override
            public void preloadFail(String message) {
                task.onTaskPreloadFail("预加载失败," + message);
            }

            @Override
            public void preloadSuccess(String realDownloadUrl) {
                task.onTaskPreloadSuccess(realDownloadUrl);
                getFileStream(task, realDownloadUrl);
            }
        };
        asyncExecute(task.preload(preloadListener));
    }

    private void getFileStream(Task task, String downloadUrl) {
        try {
            final FileStreamListener streamListener = new FileStreamListener() {

                @Override
                public void getFileStreamFail(String msg) {
                    task.onTaskDownloadError(msg);
                }

                @Override
                public void getFileStreamSuccess(long contentLength, InputStream byteStream) {
                    task.onTaskDownloadStart(downloadUrl);

                    task.parseSegment(contentLength, getSegmentTaskEvaluator(task, downloadUrl));
                }
            };

            mStreamProcessor.getCompleteFileStream(downloadUrl, streamListener);
        } catch (Exception e) {
            e.printStackTrace();
            task.onTaskDownloadError(e.getMessage());
        }
    }

    @NotNull
    private Task.SegmentTaskEvaluator getSegmentTaskEvaluator(Task task, String downloadUrl) {
        return (threadId, cacheAccessFile, start, end) -> asyncExecute(getSegmentRunnable(threadId, cacheAccessFile,start, end, downloadUrl, task));
    }

    @NotNull
    private Runnable getSegmentRunnable(int threadId, RandomAccessFile cacheAccessFile, long start, long end, String downloadUrl, Task task) {
        return () -> {
            try {
                final RangeDownloadListener rangeDownloadListener = getRangeDownloadListener(task, threadId, cacheAccessFile,start, end);
                mStreamProcessor.downloadRangeFile(downloadUrl, task.getTmpAccessFile(), start, end, rangeDownloadListener);
            } catch (Exception e) {
                e.printStackTrace();
                task.onTaskDownloadError(e.getMessage());
            }
        };
    }

    @NotNull
    private RangeDownloadListener getRangeDownloadListener(Task task, int threadId, RandomAccessFile cacheAccessFile, long realStartIndex, long realEndIndex) {
        return new RangeDownloadListener() {
            @Override
            public void rangeDownloadFail(String msg) {
                task.onTaskDownloadError(msg);
            }

            @Override
            public void rangeDownloadFinish(long currentDownloadLength, long currentRangeFileLength) {
                LogUtils.d(String.format(Locale.SIMPLIFIED_CHINESE, "%1d分段任务下载完成,线程的任务起点为%2d-本次共下载%3d byte,写至文件%4d处,分段文件总byte大小%5d",
                        threadId,
                        realStartIndex,
                        currentDownloadLength,
                        currentRangeFileLength,
                        task.getSegmentFileSize(threadId)
                ));

                LogUtils.i("下载完成" + task.getCacheFiles()[threadId].getName() + "将被删除:" + task.getCacheFiles()[threadId].delete());
                task.getCountDownLatch().countDown();
            }

            @Override
            public void updateRangeProgress(long rangeFileDownloadIndex) {

                //将当前现在到的位置保存到文件中
                try {
                    cacheAccessFile.seek(0);
                    cacheAccessFile.write((String.valueOf(rangeFileDownloadIndex)).getBytes(Charset.defaultCharset()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // 分段任务已下载长度
                final long length = rangeFileDownloadIndex - realStartIndex;
//                if (threadId == 1){
//                    LogUtils.i(threadId+"线程本次已下载长度:"+length);
//                }
                task.onRangeFileProgressUpdate(threadId, length);

            }
        };
    }

    private void asyncExecute(Runnable runnable) {
        mExecutor.execute(runnable);
    }

    public void loadTaskRecord(Context context, LoadLocalTaskListener listener) {
        final Context applicationContext = context.getApplicationContext();
        mDatabaseRepository = new DataBaseRepository(applicationContext);
        asyncExecute(() -> {
            final List<TaskRecordEntity> taskRecords = mDatabaseRepository.loadAllTaskRecord();
            DataCheck.checkEmpty(taskRecords);

            DataCheck.checkNoEmptyWithCallback(taskRecords, data -> {
                Map<Integer, Task> taskMap = new HashMap<>(BreakPointConst.DEFAULT_CAPACITY);
                for (TaskRecordEntity recordEntity : data) {
                    final Task task = Task.Builder.transformRecord(recordEntity);
                    taskMap.put(task.getTaskId(), task);
                }
                listener.localTaskExist(taskMap);
            });
        });
    }

    public void onNewTaskAdd(Task task) {
        asyncExecute(() -> mDatabaseRepository.addTaskRecord(task.parseToRecord()));
    }

    public void deleteTaskRecord(Task task) {
        asyncExecute(() -> mDatabaseRepository.deleteTaskRecord(task.parseToRecord()));
    }

    /**
     * 加载本地记录监听
     *
     * @author wsb
     */
    interface LoadLocalTaskListener {
        /**
         * 本地存在往期任务
         *
         * @param taskMap 往期任务内容
         */
        void localTaskExist(Map<Integer, Task> taskMap);
    }


}
