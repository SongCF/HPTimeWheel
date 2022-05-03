package org.songcf.utils;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author songcf
 * @version : LogUtil.java
 */
public class LogUtil {

    public interface Logger {
        default boolean isInfoEnabled() {return true;}
        void info(String logStr);
        void warn(String logStr);
        void error(String logStr);
    }

    private static class DefaultLogger implements Logger {

        public static PrintStream cout = System.out;
        public static PrintStream cerr = System.err;

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public void info(String logStr) {
            cout.println(logStr);
        }

        @Override
        public void warn(String logStr) {
            cout.println("[WARN]" + logStr);
        }

        @Override
        public void error(String logStr) {
            cerr.println(logStr);
        }
    }

    /**
     * default
     */
    private static Logger logger = new DefaultLogger();

    /**
     * 可处理任意多个输入参数，并避免在日志级别不够时字符串拼接带来的资源浪费
     *
     * @param obj
     */
    public static void info(Object... obj) {
        Logger logger = getLogger();
        if (logger.isInfoEnabled()) {
            Object[] objs = (Object[]) obj;
            String message = "";
            for (int i = 0; i < objs.length; i++) {
                message = message + objs[i] + ",";
            }
            message = message.substring(0, message.lastIndexOf(","));
            logger.info(getLogKey() + message);
        }
    }

    /**
     * 可处理任意多个输入参数，并避免在日志级别不够时字符串拼接带来的资源浪费
     *
     * @param obj
     */
    public static void warn(Object... obj) {
        Object[] objs = (Object[]) obj;
        String message = "";
        for (int i = 0; i < objs.length; i++) {
            message = message + objs[i] + ",";
        }
        message = message.substring(0, message.lastIndexOf(","));
        getLogger().warn(getLogKey() + message);
    }

    /**
     * 可处理任意多个输入参数，并避免在日志级别不够时字符串拼接带来的资源浪费
     *
     * @param obj
     */
    public static void error(Object... obj) {
        Object[] objs = (Object[]) obj;
        String message = "";
        for (int i = 0; i < objs.length; i++) {
            message = message + objs[i] + ",";
        }
        message = message.substring(0, message.lastIndexOf(","));
        getLogger().error(getLogKey() + message);
    }

    /**
     * get trace
     *
     * @param e
     * @return
     */
    public static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static void setLogger(Logger l) {
        logger = l;
    }

    private static Logger getLogger() {
        return logger;
    }

    private static String getLogKey() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSSZ");
        String ts = simpleDateFormat.format(new Date());
        return "[LogUtil][" + ts + "]";
    }

}