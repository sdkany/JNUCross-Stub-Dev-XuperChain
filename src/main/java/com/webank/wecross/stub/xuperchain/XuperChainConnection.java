package com.webank.wecross.stub.xuperchain;

import com.baidu.xuper.api.Account;
import com.baidu.xuper.api.Transaction;
import com.baidu.xuper.api.XuperClient;
import com.baidu.xuper.pb.XchainOuterClass;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.moandjiezana.toml.Toml;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.xuperchain.utils.ClientUtils;
import com.webank.wecross.stub.xuperchain.utils.TransactionConstant;
import com.webank.wecross.stub.xuperchain.utils.Utils;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XuperChainConnection implements Connection {
    private static final Logger logger = LoggerFactory.getLogger(XuperChainConnection.class);
    public static String xuperChainURL = "10.154.24.12:37101";
    private XuperClient client; //= new XuperClient(xuperChainURL);

    public XuperChainConnection() {
        client = new XuperClient(xuperChainURL);
    }

    // 给定一个url，构建xuperchain链接
    public XuperChainConnection(String chainUrl) {
        client = new XuperClient(chainUrl);
    }

    // 读取path路径下的stub.toml文件中的connectionsStr，来构建xuperchain链接
    public static XuperChainConnection build(String path){
        Toml toml = new Toml();
        toml = toml.read(new File(path + File.separator + "stub.toml"));
        Map<String, Object> stubConfig = toml.toMap();
        Map<String, Object> channelServiceConfigValue =
                (Map<String, Object>) stubConfig.get("channelService");
        String url = ((ArrayList<String>)channelServiceConfigValue.get("connectionsStr")).get(0);
        return  new XuperChainConnection(url);
    }

    public XuperChainConnection(Map<String, Object> properties) throws Exception {
        Map<String, Object> common = (Map<String, Object>) properties.get("common");
        if (common == null) {
            throw new Exception("[common] item not found");
        }

        String chainUrl = (String) common.get("chainUrl");
        if (chainUrl == null) {
            throw new Exception("\"chainUrl\" item not found");
        }

        client = new XuperClient(chainUrl);
    }

    @Override
    public void asyncSend(Request request, Callback callback) {
        int type = request.getType();
        byte[] data = request.getData();
        switch (type) {
            case TransactionConstant.Type.SEND_TRANSACTION:
            {
                try {
                    TransactionContext context = (TransactionContext) request.getResourceInfo().getProperties().get("context");
                    TransactionRequest transactionRequest = (TransactionRequest) request.getResourceInfo().getProperties().get("request");
                    XuperChainAccount account = (XuperChainAccount) context.getAccount();
                    String method = transactionRequest.getMethod();
                    String[] args = transactionRequest.getArgs();
                    String contractName = request.getPath();
                    String abi = (String) context.getResourceInfo().getProperties().get("abi");
                    Map<String, String> argsMap = Utils.parseAbi(abi, method, args);

                    Transaction t1 = client.invokeEVMContract(Account.create(account.getEcKeyPair()), contractName, method, argsMap, null);

                    if (t1 == null) {
                        String message = "SEND_TRANSACTION failed!";
                        Response response = new Response();
                        response.setErrorCode(TransactionConstant.Result.ERROR);
                        response.setErrorMessage(message);
                        response.setData(null);
                        logger.error(message);
                        callback.onResponse(response);
                    } else {
                        Response response = new Response();
                        response.setErrorCode(TransactionConstant.Result.SUCCESS);
                        response.setErrorMessage("Success");
                        response.setData(Utils.objectMapper.writeValueAsBytes(t1));
                        callback.onResponse(response);
                    }
                } catch (Exception e) {
                    Response response = new Response();
                    response.setErrorCode(TransactionConstant.Result.ERROR);
                    response.setErrorMessage(e.getMessage());
                    response.setData(null);
                    logger.error("SEND_TRANSACTION failed!", e);
                    callback.onResponse(response);
                }
                break;
            }

            case TransactionConstant.Type.CALL_TRANSACTION:
            {
                try {
                    TransactionContext context = (TransactionContext) request.getResourceInfo().getProperties().get("context");
                    TransactionRequest transactionRequest = (TransactionRequest) request.getResourceInfo().getProperties().get("request");
                    XuperChainAccount account = (XuperChainAccount) context.getAccount();
                    String method = transactionRequest.getMethod();
                    String[] args = transactionRequest.getArgs();
                    String contractName = request.getPath();
                    String abi = (String) context.getResourceInfo().getProperties().get("abi");
                    Map<String, String> argsMap = Utils.parseAbi(abi, method, args);
                    Transaction t1 = client.queryEVMContract(Account.create(account.getEcKeyPair()), contractName, method, argsMap);

                    Response response = new Response();
                    response.setErrorCode(TransactionConstant.Result.SUCCESS);
                    response.setErrorMessage("Success");
                    response.setData(Utils.objectMapper.writeValueAsBytes(t1));
                    callback.onResponse(response);
                } catch (Exception e) {
                    Response response = new Response();
                    response.setErrorCode(TransactionConstant.Result.ERROR);
                    response.setErrorMessage("call failed,error" + e.getMessage());
                    response.setData(null);
                    callback.onResponse(response);
                }
                break;
            }

            case TransactionConstant.Type.GET_TRANSACTION_RECEIPT:
            {
                try {
                    String txHash = new String(data);
                    XchainOuterClass.Transaction transaction = client.queryTx(txHash);
                    Response response = new Response();
                    response.setErrorCode(TransactionConstant.Result.SUCCESS);
                    response.setErrorMessage("Success");
                    response.setData(Utils.toByteArray(transaction));
                    callback.onResponse(response);
                } catch (Exception e) {
                    Response response = new Response();
                    response.setErrorCode(TransactionConstant.Result.ERROR);
                    response.setErrorMessage(e.getMessage());
                    response.setData(null);
                    callback.onResponse(response);
                }
                break;
            }
//            case TransactionConstant.Type.GET_ABI:
//            {
//                try {
//                    String path = request.getPath();
//                    if (!path.substring(0,2).equals("0x")) path = "0x" + path;
//                    String abi = citAj.appGetAbi(path, DefaultBlockParameterName.PENDING)
//                            .send()
//                            .getAbi();
//                    Response response = new Response();
//                    response.setErrorCode(TransactionConstant.Result.SUCCESS);
//                    response.setErrorMessage("Success");
//                    response.setData(abi.getBytes(StandardCharsets.UTF_8));
//                    callback.onResponse(response);
//                } catch (IOException e) {
//                    Response response = new Response();
//                    response.setErrorCode(TransactionConstant.Result.ERROR);
//                    response.setErrorMessage(e.getMessage());
//                    response.setData(null);
//                    callback.onResponse(response);
//                }
//                break;
//            }
            case TransactionConstant.Type.GET_BLOCK_NUMBER:
            {
                try {
                    long blockNumber = client.getHeight();
                    Response response = new Response();
                    response.setErrorCode(TransactionConstant.Result.SUCCESS);
                    response.setErrorMessage("Success");
                    response.setData(Utils.longToBytes(blockNumber));
                    callback.onResponse(response);
                } catch (Exception e) {
                    Response response = new Response();
                    response.setErrorCode(TransactionConstant.Result.ERROR);
                    response.setErrorMessage(e.getMessage());
                    response.setData(null);
                    callback.onResponse(response);
                }
                break;
            }
            case TransactionConstant.Type.GET_BLOCK_BY_NUMBER:
            {
                try {
                    XchainOuterClass.InternalBlock block = client.queryBlock(Hex.toHexString(data));
                    if (block == null) {
                        Response response = new Response();
                        response.setErrorCode(TransactionConstant.STATUS.INTERNAL_ERROR);
                        response.setErrorMessage("Block is empty");
                        response.setData(null);
                        callback.onResponse(response);
                        return;
                    }
                    Response response = new Response();
                    response.setErrorCode(TransactionConstant.STATUS.OK);
                    response.setErrorMessage("Success");
                    response.setData(Utils.toByteArray(block));
                    callback.onResponse(response);
                } catch (Exception e) {
                    Response response = new Response();
                    response.setErrorCode(TransactionConstant.STATUS.CONNECTION_EXCEPTION);
                    response.setErrorMessage(e.getMessage());
                    response.setData(null);
                    callback.onResponse(response);
                }
                break;
            }
            case TransactionConstant.Type.GET_BLOCK_BY_HASH:
            {
                try {
                    XchainOuterClass.InternalBlock block = client.queryBlock(Utils.toHexString(data));
                    Response response = new Response();
                    response.setErrorCode(TransactionConstant.STATUS.OK);
                    response.setErrorMessage("Success");
                    response.setData(Utils.toByteArray(block));
                    callback.onResponse(response);
                } catch (Exception e) {
                    Response response = new Response();
                    response.setErrorCode(TransactionConstant.STATUS.CONNECTION_EXCEPTION);
                    response.setErrorMessage(e.getMessage());
                    response.setData(null);
                    callback.onResponse(response);
                }
                break;
            }
            default:
            {
                Response response = new Response();
                response.setErrorCode(TransactionConstant.Result.ERROR);
                response.setErrorMessage("Unrecognized type of " + type);
                response.setData(null);
                callback.onResponse(response);
                break;
            }
        }
    }

    @Override
    public void setConnectionEventHandler(ConnectionEventHandler eventHandler) {

    }

    @Override
    public Map<String, String> getProperties() {
        return null;
    }

    public XuperClient getClient() {
        return client;
    }

//    public static Map<String, String> parseAbi(String abi, String[] agrs) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            JsonNode trees = objectMapper.readTree(abi);
//            for (JsonNode tree : trees) {
//                String type = tree.get("type").asText();
//                if (!"function".equalsIgnoreCase(type)) {
//                    continue;
//                }
//                Abi inner = new Abi();
//                inner.setName(tree.get("name").asText());
//                inner.setInputTypes(makeType(tree.get("inputs")));
//                inner.setOutputTypes(makeType(tree.get("outputs")));
//                abis.add(inner);
//            }
//            return abis;
//        } catch (IOException e) {
//            throw new RuntimeException("parse abi failed");
//        }
//    }
//
//    public static List<AbiFunctionType> makeType(JsonNode node) {
//        final List<AbiFunctionType> result = Lists.newArrayListWithCapacity(node.size());
//        node.forEach(
//                input -> {
//                    AbiFunctionType type = new AbiFunctionType();
//                    type.setType(input.get("type").asText());
//                    type.setName(input.get("name").asText());
//                    result.add(type);
//                });
//        return result;
//    }
}
