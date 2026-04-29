package com.dlz.kit.exception;

/**
 * HttpException for SDK
 */
public class HttpException extends BaseException {
    private static final long serialVersionUID = 4454410583070023L;

    private static int DEFUALT_ERROR_CODE = 7003;

    private int status;
    static {
        ExceptionErrors.addErrors(7003, "Http状态非正常");
    }
    public HttpException(String message, int status) {
        super(DEFUALT_ERROR_CODE, message + " http状态码：" + status, null);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

}
