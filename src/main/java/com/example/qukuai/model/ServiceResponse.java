package com.example.qukuai.model;

/**
 * @author deray.wang
 * @date 2019/11/21 14:33
 */

import com.example.qukuai.enmus.ResultMsg;
import com.example.qukuai.utils.TimeUtil;
import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 统一的结果集
 */
@SuppressWarnings("unchecked")
@ApiModel(value = "接口通用响应", description = "S-R")
public class ServiceResponse<T> implements Serializable {
    private static final long serialVersionUID = -7105469190103583078L;

    /**
     * 成功的code
     */
    public static final int SUCCESS_KEY = 1;

    /**
     * 失败的code
     */
    public static final int FAIL_KEY = 0;

    /**
     * client调用失败的code
     */
    public static final int CLIENT_FAIL_KEY = -999;

    /**
     * 通用的失败响应
     */
    public static final ServiceResponse FAIL_RESPONSE = createFailResponse(null, FAIL_KEY, null);

    /**
     * 通用的成功响应
     */
    public static final ServiceResponse SUCCESS_RESPONSE = createSuccessResponse(null, null);

    /**
     * client调用的失败响应
     */
    public static final ServiceResponse CLIENT_FAIL_RESPONSE = createFailResponse(null, CLIENT_FAIL_KEY, null);

    /**
     * 本次服务调用的唯一标识
     */
    @Getter
    @ApiModelProperty(value = "本次服务调用的唯一标识")
    private String traceId;

    /**
     * 响应时间
     * 格式如2017-04-27 13:42:54
     */
    @Getter
    @ApiModelProperty(value = "响应时间(格式如YYYY-MM-DD hh:mm:ss)")
    private String respTime;

    /**
     * 调用结果成功还是失败
     */
    @Getter
    @ApiModelProperty(value = "调用结果成功还是失败")
    private boolean success;

    /**
     * 响应状态代码
     */
    @Getter
    @ApiModelProperty(value = "响应状态代码")
    private int stateCode;

    /**
     * 数据
     */
    @Getter
    @ApiModelProperty(value = "数据")
    private T data;

    /**
     * 响应状态详情信息
     */
    @Getter
    @ApiModelProperty(value = "响应状态详情信息")
    private String stateDesc;

    /**
     * 响应状态详细信息，
     * 必要时可包含错误堆栈
     */
    @Getter
    @Setter
    @ApiModelProperty(value = "响应状态详细信息(必要时可包含错误堆栈)")
    private String stateDetail;

    private ServiceResponse() {
    }


    /**
     * @param traceId     请求标识
     * @param success     是否成功
     * @param stateCode   响应状态
     * @param data        结果数据
     * @param stateDesc   结果描述
     * @param stateDetail 错误堆栈
     */
    public ServiceResponse(String traceId, boolean success, int stateCode, T data, String stateDesc, String stateDetail) {
        this.traceId = traceId;
        this.respTime = TimeUtil.formatDate(new Date(), TimeUtil.YYYY_MM_DD_HH_MM_SS);
        this.success = success;
        this.stateCode = stateCode;
        this.data = data;
        this.stateDesc = stateDesc;
        this.stateDetail = stateDetail;
    }

    /**
     * @param traceId   请求标识
     * @param success   是否成功
     * @param stateCode 响应状态
     * @param data      结果数据
     * @param stateDesc 结果描述
     */
    public ServiceResponse(String traceId, boolean success, int stateCode, T data, String stateDesc) {
        this.traceId = traceId;
        this.respTime = TimeUtil.formatDate(new Date(), TimeUtil.YYYY_MM_DD_HH_MM_SS);
        this.success = success;
        this.stateCode = stateCode;
        this.data = data;
        this.stateDesc = stateDesc;
    }

    /**
     * 构建成功的响应
     *
     * @param data
     * @return
     */
    public static <T> ServiceResponse<T> createSuccessResponse(String traceId, T data) {
        return new ServiceResponse(traceId, true, SUCCESS_KEY, data, null);
    }

    /**
     * 构建成功的响应
     *
     * @param data
     * @param msg
     * @return
     */
    public static <T> ServiceResponse<T> createSuccessResponse(String traceId, T data, String msg) {
        return new ServiceResponse(traceId, true, SUCCESS_KEY, data, msg);
    }

    /**
     * 构建错误的响应
     *
     * @param code 错误代码
     * @param msg  错误描述
     * @return
     */
    public static <T> ServiceResponse<T> createFailResponse(String traceId, int code, String msg) {
        return new ServiceResponse(traceId, false, code, null, msg);
    }

    /**
     * 构建错误的响应
     *
     * @param code
     * @param data
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> ServiceResponse<T> createFailResponse(String traceId, int code, T data, String msg) {
        return new ServiceResponse(traceId, false, code, data, msg);
    }

    /**
     * 返回默认的失败响应
     *
     * @param <T>
     * @return
     */
    public static <T> ServiceResponse<T> defaultFailResponse(String traceId) {
        return createFailResponse(traceId, FAIL_KEY, "系统异常，请联系管理员！");
    }

    /**
     * 构建错误的响应
     *
     * @param traceId   唯一标识
     * @param resultMsg 错误描述
     * @return
     */
    public static <T> ServiceResponse<T> createFailResponse(String traceId, ResultMsg resultMsg) {
        return new ServiceResponse(traceId, false, resultMsg.getIndex(), null, resultMsg.getMsg());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("traceId", this.traceId)
                .add("respTime", this.respTime)
                .add("success", this.success)
                .add("stateCode", this.stateCode)
                .add("data", this.data)
                .add("stateDesc", this.stateDesc).toString();
    }
}
