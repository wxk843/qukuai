package com.example.qukuai.controller;

import com.example.qukuai.model.ServiceResponse;
import com.example.qukuai.model.bean.BlockchainTransaction;
import com.example.qukuai.service.BlockchainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author deray.wang
 * @date 2019/11/19 17:21
 */
@Slf4j
@RestController
@RequestMapping("/eth")
@Api(description = "test")
public class WalletController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WalletController.class);

    @Autowired
    private Web3j web3j;

    @Autowired
    private BlockchainService blockchainService;

    /**
     * 获取当前以太坊节点高度
     * @return
     */
    @RequestMapping(value = "/height", method = RequestMethod.GET)
//    @ApiOperation(httpMethod = "GET", value = "获取授权分组定义", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "GET", value = "获取当前以太坊节点高度", produces = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse getHeight() {
        try {
            //当前以太坊节点高度
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            //发送异步请求
//            web3j.web3ClientVersion().sendAsync().get();
            long blockHeight = blockNumber.getBlockNumber().longValue();

            System.out.println(blockchainService.getAcountNonce(web3j,"0xefd443f855a7a21008a40bbd5f4521449438e320"));
            System.out.println(web3j.ethAccounts().send().getAccounts());
            return ServiceResponse.createSuccessResponse("",blockHeight);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ServiceResponse.createFailResponse("",0,"");
    }

    /**
     * 获得ethblock
     * @param blockNumber 区块编号
     * @return
     */
    @RequestMapping(value = "/getBlock", method = RequestMethod.GET)
    @ApiOperation(httpMethod = "GET", value = "查询区块内容", produces = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse getBlock(@ApiParam(name = "blockNumber") @RequestParam(name = "blockNumber") BigInteger blockNumber) {
        //查询区块内容
        EthBlock ethBlock = blockchainService.getBlockEthBlock(web3j,blockNumber);
        System.out.println(ethBlock);
        return ServiceResponse.createSuccessResponse("",ethBlock);
    }

    /**
     * 获取ETH余额
     * @param addr
     * @return
     */
    @RequestMapping(value = "/getBalance", method = RequestMethod.GET)
    @ApiOperation(httpMethod = "GET", value = "获取ETH余额", produces = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse getAccountBalance(@ApiParam(name = "addr") @RequestParam(name = "addr") String addr) {
        try {

            BigDecimal balance = blockchainService.getAccountBalance(web3j,addr);

            return ServiceResponse.createSuccessResponse("",balance);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ServiceResponse.createFailResponse("",0,"");
    }

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    @ApiOperation(httpMethod = "POST", value = "Test")
    public ServiceResponse test(){
        try {
            //测试交易
            BlockchainTransaction trx = new BlockchainTransaction();
            trx.setFromId(0);
            trx.setToId(1);
            trx.setValue(10);
            EthAccounts accounts = web3j.ethAccounts().send();
            System.out.println(accounts.getAccounts());
            System.out.println("fromAddr:"+accounts.getAccounts().get(trx.getFromId()));
            EthGetTransactionCount transactionCount = web3j.ethGetTransactionCount(accounts.getAccounts().get(trx.getFromId()), DefaultBlockParameterName.LATEST).send();
            Transaction transaction = Transaction.createEtherTransaction(accounts.getAccounts().get(trx.getFromId()), transactionCount.getTransactionCount(), BigInteger.valueOf(trx.getValue()), BigInteger.valueOf(21_000), accounts.getAccounts().get(trx.getToId()),BigInteger.valueOf(trx.getValue()));
            EthSendTransaction response = web3j.ethSendTransaction(transaction).send();
            if (response.getError() != null) {
                trx.setAccepted(false);
                System.out.println(trx);
            }
            trx.setAccepted(true);
            String txHash = response.getTransactionHash();
            LOGGER.info("Tx hash: {}", txHash);
            trx.setId(txHash);
            EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash).send();
            if (receipt.getTransactionReceipt().isPresent()) {
                LOGGER.info("Tx receipt: {}", receipt.getTransactionReceipt().get().getCumulativeGasUsed().intValue());
            }
            System.out.println(trx);
            return ServiceResponse.createSuccessResponse("",trx);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ServiceResponse.createFailResponse("",0,"");
    }

    @RequestMapping(value = "/test1", method = RequestMethod.POST)
    @ApiOperation(httpMethod = "POST", value = "自定义数据写入到区块链中")
    public ServiceResponse test1(){
        try {
            //封装业务参数
            Map<String,String> map = new HashMap<String,String>();
            map.put("time", String.valueOf(new Date()));
            map.put("type","info");
            map.put("msg","Web3 Test!!!");

            JSONObject jsonObj=new JSONObject(map);
            System.out.println(jsonObj.toString());

            //将data转化为hex
            String data = HexUtils.toHexString(jsonObj.toString().getBytes("UTF-8"));

            //测试交易
            BlockchainTransaction trx = new BlockchainTransaction();
            trx.setFromId(0);
            trx.setToId(1);
            EthAccounts accounts = web3j.ethAccounts().send();
            System.out.println(accounts.getAccounts());
            System.out.println("fromAddr:"+accounts.getAccounts().get(trx.getFromId()));

            Transaction transaction = Transaction.createEthCallTransaction(accounts.getAccounts().get(trx.getFromId()), accounts.getAccounts().get(trx.getToId()),data);
            EthSendTransaction response = web3j.ethSendTransaction(transaction).send();
            if (response.getError() != null) {
                trx.setAccepted(false);
                System.out.println(response);
            }
            trx.setAccepted(true);
            String txHash = response.getTransactionHash();
            LOGGER.info("Tx hash: {}", txHash);
            trx.setId(txHash);
            EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash).send();
            if (receipt.getTransactionReceipt().isPresent()) {
                LOGGER.info("Tx receipt: {}", receipt.getTransactionReceipt().get().getCumulativeGasUsed().intValue());
            }
            System.out.println(trx);
            return ServiceResponse.createSuccessResponse("",trx);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ServiceResponse.createFailResponse("",0,"");
    }


    @RequestMapping(value = "/test2", method = RequestMethod.POST)
    @ApiOperation(httpMethod = "POST", value = "查看这笔交易")
    public ServiceResponse test2(){
        try {
            //将data转化为hex
//            String data = HexUtils.toHexString(jsonObj.toString().getBytes("UTF-8"));

            String hash ="0x565c7530f3ec52be536fe3814a6d33206bb74e02e7722be168e5bd6897c426e0";
            EthTransaction ethTransaction= web3j.ethGetTransactionByHash(hash).send();

            String inputData = ethTransaction.getResult().getInput();
            System.out.println(inputData);
            inputData = inputData.replace("0x","");
            System.out.println(inputData);
            byte[] myBytes = HexUtils.fromHexString(inputData);
            System.out.println(myBytes);

            String myStr = new String(myBytes);
            System.out.println(myStr);
//            //从交易地址获取数据
//            web3j.ethGetTransaction(address).then(console.log);
//
//            web3j.ethGetTransaction(address,function(error, result){
//                //console.log(result);
//                inputData = result.input;
//                res_str = HexUtils.fromHexString(inputData).toString();
//                res_json = JSON.parse(res_str);
//                console.log(res_json);
//            });
            return ServiceResponse.createSuccessResponse("",ethTransaction.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ServiceResponse.createFailResponse("",0,"");
    }

}
