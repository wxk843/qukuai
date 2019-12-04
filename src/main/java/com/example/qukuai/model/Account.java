package com.example.qukuai.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.geth.Geth;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author deray.wang
 * @date 2019/11/22 18:26
 */
public class Account {

    @Autowired
    private Web3j web3j;
    @Autowired
    private Geth geth;

    public List<String> getAccountlist(){

        try{System.out.println(web3j.ethAccounts().send().getAccounts());;
            return  web3j.ethAccounts().send().getAccounts();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String createAccount(String accountName,String password,AccountInfo accountInfo) {

        Request<?, NewAccountIdentifier> request = geth.personalNewAccount(password);
        NewAccountIdentifier result = null;
        try {
            result = request.send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.getAccountId();

    }

}
