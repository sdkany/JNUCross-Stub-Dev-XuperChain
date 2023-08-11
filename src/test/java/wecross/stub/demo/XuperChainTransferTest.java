package wecross.stub.demo;

import com.baidu.xuper.api.Account;
import com.baidu.xuper.api.XuperClient;
import org.junit.Test;
import com.webank.wecross.stub.xuperchain.utils.ClientUtils;

import java.math.BigInteger;

/**
 * @author SDKany
 * @ClassName XuperChainTransferTest
 * @Date 2023/8/4 19:21
 * @Version V1.0
 * @Description
 */
public class XuperChainTransferTest {
    public static XuperClient client = new XuperClient("10.154.24.12:37101");

    @Test
    public void TransferTest(){
        //client.setChainName("xuper");
        Account account1 = Account.getAccountFromPlainFile("./masterKeys");
        Account account2 = Account.getAccountFromFile("./account2", "");
        System.out.println("account1 address = " + account1.getAddress());
        System.out.println("account2 address = " + account2.getAddress());

        System.out.println("account1 balance = " + client.getBalance(account1.getAddress(),false));
        System.out.println("account2 balance = " + client.getBalance(account2.getAddress(),false));

        client.transfer(account1, account2.getAddress(), BigInteger.valueOf(10000000L), "1");

        System.out.println("account1 balance = " + client.getBalance(account1.getAddress(),false));
        System.out.println("account2 balance = " + client.getBalance(account2.getAddress(),false));
    }

    @Test
    public void NewAccount(){
        //Account account = Account.createAndSaveToFile("./account2", "", 1, 1);

        Account account = Account.getAccountFromFile("./account2", "");
        System.out.println(account.getAddress());
    }

    @Test
    public void NewContractAccount(){
        Account account = Account.getAccountFromFile("./account2", "");
        //client.createContractAccount(account, "1234567890123455");
        client.transfer(account, "XC1234567890123455@xuper", BigInteger.valueOf(1000000), "1");
        BigInteger result = client.getBalance("XC1234567890123455@xuper", false);
        System.out.println("contract account address : " + "XC1234567890123455@xuper");
        System.out.println("balance = " + client.getBalance("XC1234567890123455@xuper", false));
        XuperClient.BalDetails[] result2 = client.getBalanceDetails("XC1234567890123455@xuper");
        for (XuperClient.BalDetails a: result2) {
            System.out.println("***************");
            System.out.println(a.getBalance());
            System.out.println(a.getFrozen());
        }
    }
}
