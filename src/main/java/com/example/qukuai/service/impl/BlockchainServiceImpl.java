package com.example.qukuai.service.impl;

import com.example.qukuai.model.bean.BlockchainTransaction;
import com.example.qukuai.service.BlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.BooleanResponse;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.geth.Geth;
import org.web3j.tx.ChainId;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import static org.web3j.crypto.Hash.sha256;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * @author deray.wang
 * @date 2019/11/20 13:52
 */
@Service
public class BlockchainServiceImpl implements BlockchainService  {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockchainService.class);

    @Autowired
    private Admin admin;

    @Autowired
    private static Web3j web3j;

    @Autowired
    private Geth geth;

    /**
     * 获取账户的Nonce
     * @param web3j
     * @param addr
     * @return
     */
    @Override
    public   BigInteger getAcountNonce(Web3j web3j, String addr) {
        createAccount();
        return getNonce(web3j,addr);
    }

    @Override
    public BigDecimal getAccountBalance(Web3j web3j, String addr) {
        return getBalance(web3j,addr);
    }

    /**
     * 指定地址发送交易所需nonce获取
     * @param web3j
     * @param addr
     * @return
     */
    public static  BigInteger getNonce(Web3j web3j, String addr){
        Request<?, EthGetTransactionCount> request = web3j.ethGetTransactionCount(addr, DefaultBlockParameterName.LATEST);
        BigInteger nonce = BigInteger.ZERO;
        try {
            nonce = request.send().getTransactionCount();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nonce;
    }

    /**
     * 获取代币余额
     * @param web3j
     * @param fromAddress
     * @param contractAddress
     * @return
     */
    public static BigInteger getTokenBalance(Web3j web3j, String fromAddress, String contractAddress) {

        String methodName = "balanceOf";
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Address address = new Address(fromAddress);
        inputParameters.add(address);

        TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddress, contractAddress, data);

        EthCall ethCall;
        BigInteger balanceValue = BigInteger.ZERO;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            balanceValue = (BigInteger) results.get(0).getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return balanceValue;
    }

    /**
     * 转账ETH
     * @param web3j
     * @param fromAddr
     * @param privateKey
     * @param toAddr
     * @param amount
     * @param data
     * @return
     */
    public static String transferETH(Web3j web3j, String fromAddr, String privateKey, String toAddr, BigDecimal amount, String data){
        // 获得nonce
        BigInteger nonce = getNonce(web3j, fromAddr);
        // value 转换
        BigInteger value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

//        // 构造eth交易
//        Transaction transaction = Transaction.createEtherTransaction(fromAddr, nonce, gasPrice, null, toAddr, value);
//        // 构造合约调用交易
//        Transaction transaction = Transaction.createFunctionCallTransaction(fromAddr, nonce, gasPrice, null, contractAddr, funcABI);
        BigInteger gasPrice = BigInteger.valueOf(20);
        // 构建交易
        Transaction transaction = Transaction.createEtherTransaction(fromAddr, nonce, gasPrice, null, toAddr, value);
        // 计算gasLimit
        BigInteger gasLimit = getTransactionGasLimit(web3j, transaction);

        // 查询调用者余额，检测余额是否充足
        BigDecimal ethBalance = getBalance(web3j, fromAddr);
        BigDecimal balance = Convert.toWei(ethBalance, Convert.Unit.ETHER);
        // balance < amount + gasLimit ??
        if (balance.compareTo(amount.add(new BigDecimal(gasLimit.toString()))) < 0) {
            throw new RuntimeException("余额不足，请核实");
        }
        byte chainId = Byte.parseByte("eee");
        return signAndSend(web3j, nonce, gasPrice, gasLimit, toAddr, value, data, chainId, privateKey);
    }

    /**
     * 对交易签名，并发送交易
     * @param web3j
     * @param nonce
     * @param gasPrice
     * @param gasLimit
     * @param to
     * @param value
     * @param data
     * @param chainId
     * @param privateKey
     * @return
     */
    public static String signAndSend(Web3j web3j, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value, String data, byte chainId, String privateKey) {
        String txHash = "";
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data);
        if (privateKey.startsWith("0x")){
            privateKey = privateKey.substring(2);
        }

        ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger(privateKey, 16));
        Credentials credentials = Credentials.create(ecKeyPair);

        byte[] signMessage;
        if (chainId > ChainId.NONE){
            signMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        } else {
            signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        }

        String signData = Numeric.toHexString(signMessage);
        if (!"".equals(signData)) {
            try {
                EthSendTransaction send = web3j.ethSendRawTransaction(signData).send();
                txHash = send.getTransactionHash();
                System.out.println(send);
            } catch (IOException e) {
                throw new RuntimeException("交易异常");
            }
        }
        return txHash;
    }

    /**
     * 获取ETH余额
     * @param web3j
     * @param address
     * @return
     */
    public static BigDecimal getBalance(Web3j web3j, String address) {
        try {
            EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            //单位转换
            BigDecimal banlance = Convert.fromWei(new BigDecimal(ethGetBalance.getBalance()),Convert.Unit.ETHER);
            return banlance;
        } catch (IOException e) {
            e.printStackTrace();
            //throw new Exception("查询钱包余额失败");
            return null;
        }
    }


    public static BigInteger getTransactionGasLimit(Web3j web3j, Transaction transaction) {
        try {
            EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(transaction).send();
            if (ethEstimateGas.hasError()){
                throw new RuntimeException(ethEstimateGas.getError().getMessage());
            }
            return ethEstimateGas.getAmountUsed();
        } catch (IOException e) {
            throw new RuntimeException("net error");
        }
    }

    /**
     * generate a random group of mnemonics
     * 生成一组随机的助记词
     */
    private String generateMnemonics() {
        byte[] initialEntropy = new byte[16];
        new SecureRandom().nextBytes(initialEntropy);
        String mnemonic = MnemonicUtils.generateMnemonic(initialEntropy);
        return mnemonic;
    }

    public void createAccount() {
        //生成密钥对和地址
        String mnemonic = generateMnemonics();
        //如果使用密码可能与大部分钱包不兼容
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, "123456");
//        try {
            ECKeyPair ecKeyPair = ECKeyPair.create(sha256(seed));
            String priKeyWithPrefix = Numeric.toHexStringWithPrefix(ecKeyPair.getPrivateKey());
            String pubKeyWithPrefix = Numeric.toHexStringWithPrefix(ecKeyPair.getPublicKey());

            //根据公钥或者ECKeyPair获取钱包地址
            //String address = Keys.getAddress(ecKeyPair);
            String address = Keys.getAddress(pubKeyWithPrefix);

            System.out.println("地址：" + address);
            System.out.println("秘钥：" + priKeyWithPrefix);
//        } catch (InvalidAlgorithmParameterException |
//                CipherException | NoSuchProviderException | NoSuchAlgorithmException e) {
//            System.out.println(e.getCause().toString());
//        }


    }

    /**
     * 查询区块内容
     * @param web3j
     * @param blockNumber
     * @return
     */
    @Override
    public EthBlock getBlockEthBlock(Web3j web3j,BigInteger blockNumber){

        DefaultBlockParameter defaultBlockParameter = new DefaultBlockParameterNumber(blockNumber);
        Request<?, EthBlock> request = web3j.ethGetBlockByNumber(defaultBlockParameter, true);
        EthBlock ethBlock = null;
        try {
            ethBlock = request.send();
            //返回值 - 区块对象
            System.out.println(ethBlock.getBlock());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ethBlock;
    }

    /**
     * 输入密码创建地址
     * @param password 密码（建议同一个平台的地址使用一个相同的，且复杂度较高的密码）
     * @return 地址hash
     */
    @Override
    public String newAccount(String password) {
        Request<?, NewAccountIdentifier> request = admin.personalNewAccount(password);
        NewAccountIdentifier result = null;
        try {
            result = request.send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.getAccountId();

    }

    /**
     * 根据hash值获取交易
     * @param hash
     * @return
     * @throws IOException
     */
    public static EthTransaction getTransactionByHash(String hash) throws IOException {
         Request<?, EthTransaction> request = web3j.ethGetTransactionByHash(hash);
        return request.send();
    }

    /**
     * 账户解锁，使用完成之后需要锁定
     * @param address
     * @return
     * @throws IOException
     */
    public  Boolean lockAccount(String address) throws IOException {

        Request<?, BooleanResponse> request = geth.personalLockAccount(address);
        BooleanResponse response = request.send();
        return response.success();
    }

    /**
     * 解锁账户，发送交易前需要对账户进行解锁
     * @param address 地址
     * @param password 密码
     * @param duration 解锁有效时间，单位秒
     * @return
     * @throws IOException
     */
    public  Boolean unlockAccount(String address, String password, BigInteger duration) throws IOException{
        Request<?, PersonalUnlockAccount> request = admin.personalUnlockAccount(address, password, duration);
        PersonalUnlockAccount account = request.send();
        return account.accountUnlocked();
    }

    /**
     * 发送交易并获得交易hash值
     * @param transaction
     * @param password
     * @return
     * @throws IOException
     */
    public  String sendTransaction(Transaction transaction, String password) throws IOException {
        Request<?, EthSendTransaction> request = admin.personalSendTransaction(transaction, password);
        EthSendTransaction ethSendTransaction = request.send();
        return ethSendTransaction.getTransactionHash();
    }

    /**
     *  获取钱包里的所有用户
     * @return
     */
    @Autowired
    public List<String> getAllAccounts() {
        List<String> list = new ArrayList<String>();
        try {
            Request<?, EthAccounts> request = geth.ethAccounts();
            list = request.send().getAccounts();
            System.out.println(list.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return  list;
    }

    public void test(){

        EthFilter ethFilter = new EthFilter();
        geth.ethNewBlockFilter();
        geth.ethNewFilter(ethFilter);
    }

}
