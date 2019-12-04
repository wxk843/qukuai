package com.example.qukuai.model.bean;

import lombok.Data;

/**
 * @author deray.wang
 * @date 2019/11/20 13:44
 */
@Data
public class BlockchainTransaction {
    private String id;
    //发送发件人ID
    private Integer fromId;
    //交易金额
    private long value;
    //收件人ID
    private Integer toId;

    private Boolean accepted;
}
