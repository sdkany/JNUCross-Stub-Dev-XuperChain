package wecross.stub.demo;

import com.baidu.xuper.api.Account;
import com.baidu.xuper.api.Transaction;
import com.baidu.xuper.api.XuperClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

/**
 * @author SDKany
 * @ClassName XuperChainContractTest
 * @Date 2023/8/4 21:39
 * @Version V1.0
 * @Description
 */
public class XuperChainContractTest {
    public static XuperClient client = new XuperClient("10.154.24.12:37101");
    static String abi = "[{\"constant\":false,\"inputs\":[{\"name\":\"x\",\"type\":\"uint256\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"retVal\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";
    static String bin = "608060405234801561001057600080fd5b50600560005560bf806100246000396000f30060806040526004361060485763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166360fe47b18114604d5780636d4ce63c146064575b600080fd5b348015605857600080fd5b5060626004356088565b005b348015606f57600080fd5b506076608d565b60408051918252519081900360200190f35b600055565b600054905600a165627a7a72305820419b352168794764ac1d5d6d3460eaffedc13c00bcbb4d2ff772148d2f0670fc0029";
    static String contractName = "SimpleStorage";
    static Account account = Account.getAccountFromFile("./account2", "");
    static Map<String, String> args = new HashMap<>();
    static{
        account.setContractAccount("XC1234567890123455@xuper");
    }
    @Test
    public void EVMContractTest(){
        //deploy();
        invoke();
        //invoke2();
    }

    public static void deploy(){
        Transaction t = client.deployEVMContract(account, bin.getBytes(), abi.getBytes(), contractName, args);
        System.out.println("txID:" + t.getTxid());
        // txID:f0a067f26bbd0bcda8fa2b873ac866305eb70a4b0fe0da8b651262e7fa414496
    }

    public static void invoke(){
        // storagepay is a payable method. Amount param can be NULL if there is no need to transfer to the contract.
        args.put("x", "1234");
        System.out.println(args);
        Transaction t1 = client.invokeEVMContract(account, contractName, "set", args, null);
        System.out.println("txID:" + t1.getTxid());
        System.out.println("tx gas:" + t1.getGasUsed());
        System.out.println("*************");
        System.out.println(t1.getRawTx());
        System.out.println("*************");
        System.out.println(t1);
        System.out.println("*************");
        // txID:85671a3352aac1ffb4d3562da361736b4a785e321b9b0c4ec477df68fadf2502
    }

    public static void invoke2(){
        Transaction t2 = client.queryEVMContract(account, contractName, "get", null);
        System.out.println("tx res getMessage:" + t2.getContractResponse().getMessage());
        System.out.println("tx res getBodyStr:" + t2.getContractResponse().getBodyStr());
        System.out.println("tx res getStatus:" + t2.getContractResponse().getStatus());
    }

    @Test
    public void ABIParse(){
        String[] args = new String[]{"1234"};
        System.out.println(parseAbi(abi, "set", args));
    }

    public static Map<String, String> parseAbi(String abi, String method, String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> result = new TreeMap<>();
        try {
            JsonNode trees = objectMapper.readTree(abi);
            for (JsonNode tree : trees) {
                String type = tree.get("type").asText();
                if (!"function".equalsIgnoreCase(type)) {
                    continue;
                }
                String name = tree.get("name").asText();
                if (name.equalsIgnoreCase(method)) {
                    JsonNode inputNode = tree.get("inputs");
                    if (inputNode.size() != args.length)
                        throw new IllegalArgumentException("args size and the method input size are not equaled! method = " + method + ", args = " + Arrays.toString(args) + ", inputNode = " + inputNode);
                    System.out.println(inputNode.size());
                    for(int i = 0; i < inputNode.size(); i ++){
                        result.put(inputNode.get(i).get("name").asText(), args[i]);
                    }
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("parse abi failed");
        }
    }
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
