package wecross.stub.demo;

import com.baidu.xuper.crypto.xchain.sign.ECKeyPair;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.xuperchain.XuperChainAccount;
import com.webank.wecross.stub.xuperchain.XuperChainDriver;
import org.junit.Test;

import java.util.Random;

/**
 * @author SDKany
 * @ClassName XuperChainDriverTest
 * @Date 2023/8/13 16:01
 * @Version V1.0
 * @Description
 */
public class XuperChainDriverTest {
    @Test
    public void mulTest(){
        for (int i = 0; i < 10000; i ++){
            SignVerifyTest();
        }
    }


    public static void SignVerifyTest(){
        ECKeyPair ecKeyPair = ECKeyPair.create();
        XuperChainAccount account = new XuperChainAccount(ecKeyPair);
        byte[] message = new byte[128];
        new Random().nextBytes(message);
        XuperChainDriver driver = new XuperChainDriver();

        byte[] signature = driver.accountSign(account, message);

       // new Random().nextBytes(message);
        System.out.println(driver.accountVerify(account.getIdentity(), signature, message));
    }
}
