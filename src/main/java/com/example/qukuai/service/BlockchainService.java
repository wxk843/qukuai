package com.example.qukuai.service;

import com.example.qukuai.model.bean.BlockchainTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;


/**
 * @author deray.wang
 * @date 2019/11/20 13:36
 */
public interface BlockchainService {

    /**
     * 获取账户的Nonce
     * @param web3j
     * @param addr
     * @return
     */
    BigInteger getAcountNonce(Web3j web3j, String addr);

    BigDecimal getAccountBalance(Web3j web3j, String addr);

    /**
     * 查询区块内容
     * @param web3j
     * @param blockNumber
     * @return
     */
    EthBlock getBlockEthBlock(Web3j web3j, BigInteger blockNumber);

    String newAccount(String password);

    List<String> getAllAccounts();

}
