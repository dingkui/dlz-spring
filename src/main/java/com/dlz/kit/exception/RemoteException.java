package com.dlz.kit.exception;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * BaseException for SDK
 */
public class RemoteException extends BaseException {
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -5345825923487658213L;
    private static int DEFUALT_ERROR_CODE = 7001;
    static {
        ExceptionErrors.addErrors(7000, "远程服务器连接失败");
        ExceptionErrors.addErrors(7001, "远程调用异常");
        ExceptionErrors.addErrors(7002, "远程调用数据读取异常");
    }
    protected RemoteException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    private RemoteException(String message, Throwable cause) {
        super(DEFUALT_ERROR_CODE, message, cause);
    }

    private RemoteException(String message) {
        super(DEFUALT_ERROR_CODE, message);
    }

    public static RemoteException build(String message, Throwable cause) {
        RemoteException e = null;
        if (cause != null && cause instanceof UnknownHostException || cause instanceof IOException || cause instanceof SocketException) {
            e = new RemoteException(7000, message, cause);
        } else if (cause != null && cause instanceof IOException) {
            e = new RemoteException(7002, message, cause);
        } else {
            e = new RemoteException(message, cause);
        }
        return e;
    }
}
