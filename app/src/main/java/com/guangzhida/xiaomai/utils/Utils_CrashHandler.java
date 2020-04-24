package com.guangzhida.xiaomai.utils;

import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * 写入崩溃日志到本地
 */
public class Utils_CrashHandler implements Thread.UncaughtExceptionHandler {

    private Context mContext;
    private static Utils_CrashHandler INSTANCE = new Utils_CrashHandler();
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    public static Utils_CrashHandler getInstance() {
        return INSTANCE;
    }

    public void init(Context ctx) {
        mContext = ctx.getApplicationContext();
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable ex) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        //获取跟踪的栈信息，除了系统栈信息，还把手机型号、系统版本、编译版本的唯一标示
        StackTraceElement[] trace = ex.getStackTrace();
        StackTraceElement[] trace2 = new StackTraceElement[trace.length + 3];
        System.arraycopy(trace, 0, trace2, 0, trace.length);
        trace2[trace.length] = new StackTraceElement("Android", "MODEL", android.os.Build.MODEL, -1);
        trace2[trace.length + 1] = new StackTraceElement("Android", "VERSION", android.os.Build.VERSION.RELEASE, -1);
        trace2[trace.length + 2] = new StackTraceElement("Android", "FINGERPRINT", android.os.Build.FINGERPRINT, -1);
        //追加信息，因为后面会回调默认的处理方法
        ex.setStackTrace(trace2);
        ex.printStackTrace(printWriter);
        //把上面获取的堆栈信息转为字符串，打印出来
        String stacktrace = result.toString();
        printWriter.close();
        //这里把刚才异常堆栈信息写入SD卡的Log日志里面
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdcardPath = Environment.getExternalStorageDirectory().getPath() + "/xiaomai/log/";
            writeLog(stacktrace, sdcardPath);
        }
        mDefaultHandler.uncaughtException(t, ex);    //该代码不执行的话程序无法终止
    }

    /**
     * 写入Log信息的方法，写入到SD卡里面
     *
     * @param log
     * @param name
     */
    private void writeLog(String log, String name) {
        File file = new File(name);
        if (!file.exists()) {
            file.mkdirs();
        }
        CharSequence timestamp = DateFormat.format("yyyyMMdd_kkmmss", System.currentTimeMillis());
        String filename = name + "_" + timestamp + ".log";
        try {
            FileOutputStream stream = new FileOutputStream(filename);
            OutputStreamWriter output = new OutputStreamWriter(stream);
            BufferedWriter bw = new BufferedWriter(output);
            //写入相关Log到文件
            bw.write(log);
            bw.newLine();
            bw.flush();
            bw.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
