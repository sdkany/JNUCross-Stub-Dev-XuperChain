package com.webank.wecross.stub.xuperchain;

import com.baidu.xuper.crypto.xchain.hash.Hash;
import com.baidu.xuper.crypto.xchain.sign.ECKeyPair;
import com.baidu.xuper.crypto.xchain.sign.Ecc;
import com.baidu.xuper.pb.XchainOuterClass;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.xuperchain.utils.Numeric;
import com.webank.wecross.stub.xuperchain.utils.TransactionConstant;
import com.webank.wecross.stub.xuperchain.utils.Utils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class XuperChainDriver implements Driver {
    private static final Logger logger = LoggerFactory.getLogger(XuperChainDriver.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public ImmutablePair<Boolean, TransactionRequest> decodeTransactionRequest(Request request) {
        return null;
    }

    @Override
    public List<ResourceInfo> getResources(Connection connection) {
        return null;
    }

    @Override
    public void asyncCall(TransactionContext context, TransactionRequest request, boolean byProxy, Connection connection, Callback callback) {
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.getProperties().put("context", context);
        resourceInfo.getProperties().put("request", request);
        //业务层需要将待调用的合约名字写到这里context 的path中的resource中
        String contractName = context.getPath().getResource();
        connection.asyncSend(
                newRequest(contractName,
                        TransactionConstant.Type.CALL_TRANSACTION,
                        null, resourceInfo),
                response -> {
                    TransactionResponse transactionResponse = new TransactionResponse();
                    com.baidu.xuper.api.Transaction t1 = null;
                    try {
                        t1 = Utils.objectMapper.readValue(response.getData(), com.baidu.xuper.api.Transaction.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String[] result = new String[]{t1.getTxid(), t1.getContractResponse().getMessage(), t1.getContractResponse().getBodyStr()};
                    transactionResponse.setResult(result);
                    transactionResponse.setErrorCode(0); // original receipt status
                    transactionResponse.setMessage("Success");
                    //transactionResponse.setHash();
                    //transactionResponse.setBlockNumber();
                    callback.onTransactionResponse(null, transactionResponse);
                });
    }

    // xuperchain似乎不会将abi存储在链上，没有找到相关的方法，因此需要将abi离线保存到数据库中
    // abi可以存储在context的ResourceInfo中的properties（map）中
    @Override
    public void asyncSendTransaction(TransactionContext context, TransactionRequest request, boolean byProxy, Connection connection, Callback callback) {
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.getProperties().put("context", context);
        resourceInfo.getProperties().put("request", request);
        //业务层需要将待调用的合约名字写到这里context 的path中的resource中
        String contractName = context.getPath().getResource();
        connection.asyncSend(newRequest(
                        contractName,
                        TransactionConstant.Type.SEND_TRANSACTION,
                        null, resourceInfo),
                response -> {
                    // todo verify transaction on-chain proof
                    if(response.getErrorCode() != TransactionConstant.STATUS.OK) {
                        callback.onTransactionResponse(new TransactionException(response.getErrorCode(), response.getErrorMessage()), null);
                    } else {
                        com.baidu.xuper.api.Transaction t1 = null;
                        try {
                            t1 = Utils.objectMapper.readValue(response.getData(), com.baidu.xuper.api.Transaction.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        TransactionResponse receipt = new TransactionResponse();
                        receipt.setErrorCode(0); // SUCCESS
                        receipt.setMessage("Success");
                        receipt.setHash(t1.getTxid());
                        callback.onTransactionResponse(null, receipt);
                    }
                });
    }

    @Override
    public void asyncGetBlockNumber(Connection connection, GetBlockNumberCallback callback) {
        connection.asyncSend(newRequest(null, TransactionConstant.Type.GET_BLOCK_NUMBER, null, null), response -> {
            if (response.getErrorCode() != TransactionConstant.STATUS.OK) {
                callback.onResponse(new Exception(response.getErrorMessage()), -1);
            } else {
                callback.onResponse(null, Utils.bytesToLong(response.getData()));
            }
        });
    }

    @Override
    public void asyncGetBlock(long blockNumber, boolean onlyHeader, Connection connection, GetBlockCallback callback) {
        connection.asyncSend(newRequest(null, TransactionConstant.Type.GET_BLOCK_BY_NUMBER, Utils.longToBytes(blockNumber), null), response -> {
            if(response.getErrorCode() != TransactionConstant.STATUS.OK) {
                callback.onResponse(new Exception(response.getErrorMessage()), null);
            } else {
                XchainOuterClass.InternalBlock block = (XchainOuterClass.InternalBlock)Utils.toObject(response.getData());
                Block weCrossBlock = new Block();
                BlockHeader blockHeader = new BlockHeader();
                blockHeader.setHash(Numeric.toHexString(block.getBlockid().toByteArray()));
                blockHeader.setNumber(block.getHeight());
                blockHeader.setPrevHash(Numeric.toHexString(block.getPreHash().toByteArray()));
                blockHeader.setTransactionRoot(Numeric.toHexString(block.getMerkleRoot().toByteArray()));
                weCrossBlock.setBlockHeader(blockHeader);
                weCrossBlock.setRawBytes(response.getData());

                List<String> transactionsHashes = new ArrayList<>();
                java.util.List<com.baidu.xuper.pb.XchainOuterClass.Transaction> transactionList = block.getTransactionsList();
                if (transactionList != null){
                    for (com.baidu.xuper.pb.XchainOuterClass.Transaction transaction: transactionList) {
                        transactionsHashes.add(Numeric.toHexString(transaction.getTxid().toByteArray()));
                    }
                }
                weCrossBlock.setTransactionsHashes(transactionsHashes);
                callback.onResponse(null, weCrossBlock);
            }
        });
    }

    @Override
    public void asyncGetTransaction(String transactionHash, long blockNumber, BlockManager blockManager, boolean isVerified, Connection connection, GetTransactionCallback callback) {
        connection.asyncSend(newRequest(
                        null,
                        TransactionConstant.Type.GET_TRANSACTION_RECEIPT,
                        transactionHash.getBytes(),
                        null),
                response -> {
                    try {
                        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                        XchainOuterClass.Transaction transactionReceipt =
                                objectMapper.readValue(response.getData(), XchainOuterClass.Transaction.class);
                        Transaction transaction = new Transaction();
                        transaction.setTxBytes(response.getData());
                        transaction.setAccountIdentity(transactionReceipt.getInitiator());
                        // xuperchain的transaction中只有block的id，获取它
                        String blockID = Numeric.toHexString(transactionReceipt.getBlockid().toByteArray());
                        // 使用blockID请求到块高，并写入wecross transaction
                        transaction.getTransactionResponse().setBlockNumber(((XuperChainConnection)connection).getClient().queryBlock(blockID).getHeight());
                        transaction.getTransactionResponse().setHash(Numeric.toHexString(transactionReceipt.getTxid().toByteArray()));
                        transaction.setReceiptBytes(response.getData()); // 写入xuperchain的transaction原始数据（byte数组）
                        transaction.getTransactionResponse().setErrorCode(0);
                        callback.onResponse(null, transaction);
                    } catch (IOException e) {
                        callback.onResponse(new Exception(
                                        "deserialize failed,error:" + e.getMessage()),
                                null);
                    }
                });
    }

    @Override
    public void asyncCustomCommand(String command, Path path, Object[] args, Account account, BlockManager blockManager, Connection connection, CustomCommandCallback callback) {
        // todo:
    }

    @Override
    public byte[] accountSign(Account account, byte[] message) {
        ECKeyPair ecKeyPair = ((XuperChainAccount)account).getEcKeyPair();

        byte[] hash = Hash.doubleSha256(message);
        byte[] result = new byte[0];
        try {
            result = Ecc.sign(hash, ecKeyPair.privateKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean accountVerify(String identity, byte[] signBytes, byte[] message) {
        ECDSASigner signer = new ECDSASigner();


        //signer.init(false, new ECPublicKeyParameters(publicKey, Ecc.domain));
        byte[] hash = Hash.doubleSha256(message);

        //signer.verifySignature(message);
        byte[] r = Arrays.copyOfRange(signBytes, 0, 32);
        byte[] s = Arrays.copyOfRange(signBytes, 32, 64);
        return false;
    }

    public Request newRequest(String path, int type, byte[] data, ResourceInfo resourceInfo){
        Request request = new Request();
        request.setType(type);
        request.setData(data);
        request.setPath(path);
        request.setResourceInfo(resourceInfo);
        return request;
    }
}
