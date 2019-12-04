package com.example.qukuai.enmus;

/**
 * @author deray.wang
 * @date 2019/11/21 14:36
 */
public enum ResultMsg {
    /**
     * 系统级别的错误码
     */
    ERROR_MSG(99, "服务器处理失败.错误码:%s"),
    FAULT(0, "失败"),
    SUCCESS(1, "成功"),
    LOGIN_REQUIRED(3, "需要登录"),
    PROMPT_ERROR(4, "提示错误"),
    NO_PERMISSION(5, "权限不足"),
    ACCOUNT_NOT_ENABLED(6, "账户无效"),
    ACCOUNT_TOKEN_FAULT(7, "token过期"),
    FREQUENT_ACCESS(8, "访问频繁"),
    DATA_IS_NULL(9, "数据不存在"),

    SYSTEM_ERROR(500, "系统异常");

    /**
     * code
     */
    private int index;
    /**
     * 描述
     */
    private String msg;

    ResultMsg(int index, String msg) {
        this.index = index;
        this.msg = msg;
    }


    public String getMsg() {
        return msg;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
