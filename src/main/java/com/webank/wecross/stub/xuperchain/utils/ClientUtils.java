package com.webank.wecross.stub.xuperchain.utils;

import com.baidu.xuper.api.XuperClient;
import com.baidu.xuper.pb.XchainOuterClass;
import org.bouncycastle.util.encoders.Hex;

/**
 * @author SDKany
 * @ClassName ClientUtils
 * @Date 2023/8/3 22:00
 * @Version V1.0
 * @Description
 */
public class ClientUtils {

    public static String BC_NAME = "com/baidu/xuper";
//
//    // 返回address的余额
//    public static String getBalance(XuperClient client, String address){
//        return client.getBalanceDetails(address)[1].getBalance();
//    }
//
//    // 返回address的冻结余额
//    public static String getFrozenBalance(XuperClient client, String address){
//        return client.getBalanceDetails(address)[0].getBalance();
//    }

//    //返回最后一个区块
//    public static XchainOuterClass.InternalBlock getLastBlock(XuperClient client){
//        return client.queryBlock(Hex.toHexString(client.getBlockchainStatus(BC_NAME).getMeta().getTipBlockid().toByteArray()));
//    }
}
