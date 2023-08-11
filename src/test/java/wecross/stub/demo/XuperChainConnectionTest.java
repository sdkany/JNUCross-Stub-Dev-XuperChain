package wecross.stub.demo;

import com.baidu.xuper.api.Account;
import com.baidu.xuper.api.XuperClient;
import com.baidu.xuper.pb.XchainOuterClass;
import com.webank.wecross.stub.xuperchain.utils.Utils;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import static com.webank.wecross.stub.xuperchain.utils.ClientUtils.BC_NAME;

/**
 * @author SDKany
 * @ClassName XuperChainConnectionTest
 * @Date 2023/8/3 14:53
 * @Version V1.0
 * @Description
 */
public class XuperChainConnectionTest {
    @Test
    public void SimpleConnectionTest(){
        XuperClient client = new XuperClient("10.154.24.12:37101");
//        System.out.println(client.getBlockchainStatus("xuper").getMeta().getTrunkHeight());
        Account account = Account.getAccountFromPlainFile("./masterKeys");
        System.out.println(account.getAddress());
//        System.out.println(client.getSystemStatus());
//        System.out.println("------------");
//        XchainOuterClass.InternalBlock block = client.queryBlockByHeight(1);
//        System.out.println(block);
//        System.out.println("------------");
//        System.out.println(Hex.toHexString(block.getBlockid().toByteArray()));
//        System.out.println(client.queryBlock(Hex.toHexString(block.getBlockid().toByteArray())));
//        System.out.println("******************");
        //Account account = Account.getAccountFromFile("./keys", ""); // 读取密钥文件
//        //Account account = Account.create(1,1);
//        //Account account = Account.createAndSave("./keys", "", 1, 2); // 生成密钥文件
//        System.out.println("address:" + account.getAddress());
//        System.out.println("contractAccount:" + account.getContractAccount());
//        System.out.println("privateKey:" + account.getKeyPair().getJSONPrivateKey());
//        System.out.println("publicKey:" + account.getKeyPair().getJSONPublicKey());
        //System.out.println("balance: " + client.getBalance("nvVmempofmcz9wMa1FTsbv98tnQ2iuC5R"));
        //System.out.println(client.queryTx("3352911eebb77ffe9e1a9f062d8c338a8a330c7354c4326880b3d35e76018131"));
        //System.out.println("balance: " + ClientUtils.getBalance(client, "nvVmempofmcz9wMa1FTsbv98tnQ2iuC5R"));

        //System.out.println("balance: " + ClientUtils.getBalance(client, "TeyyPLpp9L7QAcxHangtcHTu7HUZ6iydY"));
        XchainOuterClass.Transaction transaction = client.queryTx("79af80d44507001653f33ce79193f637ad941685c2a395b927533ea8e842925e");
        System.out.println(transaction);
////
//        String blockid = Hex.toHexString(transaction.getBlockid().toByteArray());
////
//        System.out.println("blockID = " + blockid);
////        System.out.println("-------------");
//        System.out.println(client.queryBlock(blockid));
//        System.out.println("-------------");
//
//        XchainOuterClass.BlockHeight blockHeight = XchainOuterClass.BlockHeight.newBuilder().setBcname(BC_NAME).setHeight(1L).build();
//
//
//        //System.out.println(client.getBlockchainStatus(ClientUtils.BC_NAME));
//        System.out.println("-------------");
//        //System.out.println(Hex.toHexString(client.getBlockchainStatus(ClientUtils.BC_NAME).getMeta().getTipBlockid().toByteArray()));
//
        System.out.println("**************");
        System.out.println("**************");
//        //System.out.println(client.queryBlock(Hex.toHexString(client.getBlockchainStatus(ClientUtils.BC_NAME).getMeta().getTipBlockid().toByteArray())));

        long height = client.getHeight();
        System.out.println(height);
        System.out.println(client.queryBlockByHeight(659301));
        System.out.println(client.queryBlockByHeight(659301 + 1));
    }
}
