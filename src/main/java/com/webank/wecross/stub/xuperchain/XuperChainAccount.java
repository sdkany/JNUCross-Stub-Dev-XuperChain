package com.webank.wecross.stub.xuperchain;

import com.baidu.xuper.crypto.xchain.sign.ECKeyPair;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.xuperchain.utils.Numeric;
import org.bouncycastle.math.ec.ECPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;

public class XuperChainAccount implements Account {
    private static final Logger logger = LoggerFactory.getLogger(XuperChainAccount.class);

    private String name;
    private String type;
    private String identity;
    private int keyID;
    private ECKeyPair ecKeyPair;

    public XuperChainAccount(){
        super();
    }

    public XuperChainAccount(String name, String type, ECKeyPair ecKeyPair) {
        super();
        this.name = name;
        this.type = type;
        this.ecKeyPair = ecKeyPair;
        this.identity = com.baidu.xuper.api.Account.create(ecKeyPair).getAddress();
    }

    public XuperChainAccount(ECKeyPair ecKeyPair) {
        super();
        this.name = com.baidu.xuper.api.Account.create(ecKeyPair).getAddress();
        this.ecKeyPair = ecKeyPair;
        this.identity = this.name;
    }

    public XuperChainAccount(Map<String, Object> properties) {
        String priKeyStr = (String) properties.get("privateKey");

        if (priKeyStr == null || priKeyStr.length() == 0) {
            logger.error("privateKey has not given");
            return;
        }

        try {
            BigInteger privateKey = Numeric.toBigInt(priKeyStr);
            ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);//new ECKeyPair(privateKey, publicKey);
            this.name = com.baidu.xuper.api.Account.create(ecKeyPair).getAddress();
            this.ecKeyPair = ecKeyPair;
            this.identity = this.name;

            logger.info("New account: {} address(identity):{}", name, identity);

        } catch (Exception e) {
            logger.error("XuperChainAccount exception: " + e.getMessage());
            return;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getIdentity() {
        return identity;
    }

    @Override
    public int getKeyID() {
        return keyID;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    public ECKeyPair getEcKeyPair() {
        return ecKeyPair;
    }

}
