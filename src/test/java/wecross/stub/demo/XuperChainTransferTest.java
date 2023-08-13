package wecross.stub.demo;

import com.baidu.xuper.api.Account;
import com.baidu.xuper.api.XuperClient;
import com.baidu.xuper.crypto.Base58;
import com.baidu.xuper.crypto.Common;
import com.baidu.xuper.crypto.xchain.hash.Hash;
import com.baidu.xuper.crypto.xchain.sign.Ecc;
import com.webank.wecross.stub.xuperchain.utils.Numeric;
import org.bouncycastle.asn1.*;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256R1FieldElement;
import org.bouncycastle.math.ec.custom.sec.SecP256R1Point;
import org.junit.Test;
import com.webank.wecross.stub.xuperchain.utils.ClientUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

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
    public void TransferTest() throws IOException {
        //client.setChainName("xuper");
        Account account1 = Account.getAccountFromPlainFile("./masterKeys");
//        Account account2 = Account.getAccountFromFile("./account2", "");
        System.out.println("account1 address = " + account1.getAddress());
//        System.out.println("account2 address = " + account2.getAddress());

        String message = "abc";
        byte[] hash = Hash.doubleSha256(message.getBytes());
        byte[] result = new byte[0];
        try {
            result = Ecc.sign(hash, account1.getKeyPair().privateKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(Numeric.toHexString(result));

        System.out.println(result.length);

        ByteArrayInputStream baos = new ByteArrayInputStream(result);
        ASN1StreamParser asn1StreamParser = new ASN1StreamParser(baos);
        ASN1Sequence a1 = (ASN1Sequence) asn1StreamParser.readObject().toASN1Primitive();
        BigInteger r = ((ASN1Integer)a1.getObjectAt(0)).getValue();
        BigInteger s = ((ASN1Integer)a1.getObjectAt(1)).getValue();
        System.out.println(r);
        System.out.println(s);
        System.out.println("*******************************");
        BigInteger p = Numeric.toBigInt("0xffffffff00000001000000000000000000000000ffffffffffffffffffffffff");
        BigInteger a = Numeric.toBigInt("0xffffffff00000001000000000000000000000000fffffffffffffffffffffffc");
        BigInteger b = Numeric.toBigInt("0x5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b");
        BigInteger n = Numeric.toBigInt("0xffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551");

        //System.out.println("n = " + n);
        //System.out.println("Ecc.n = " + Ecc.domain.getN() );

        ECPoint G = Ecc.domain.getG();

        //ECPoint G_Point = new SecP256R1Point(Ecc.domain.getCurve(), new ECFieldElement.Fp(p, G.getAffineXCoord().toBigInteger()), new ECFieldElement.Fp(p, G.getAffineYCoord().toBigInteger()));


        System.out.println("G is SecP256R1Point?" + (G instanceof SecP256R1Point));

        BigInteger y_2 = r.modPow(BigInteger.valueOf(3), p).add(b).add(r.multiply(a)).mod(p);
        BigInteger y = y_2;
        System.out.println("y_2 =" + y_2);
        System.out.println();
        //System.out.println(ecPoint.isValid());
        System.out.println(p.isProbablePrime(30));
        BigInteger m = p.subtract(BigInteger.valueOf(3)).divide(BigInteger.valueOf(4));
        System.out.println("p = " + p);
        System.out.println("m = " + m);
        System.out.println("4m+3 = " + m.multiply(BigInteger.valueOf(4)).add(BigInteger.valueOf(3)));
        BigInteger y0 = y.modPow(m.add(BigInteger.ONE), p);
        BigInteger y1 = y.modPow(m.add(BigInteger.ONE), p).multiply(BigInteger.valueOf(-1)).mod(p);

        System.out.println("y0 = " + y0);
        System.out.println("y1 = " + y1);
        System.out.println("y0^2 mod p = " + y0.pow(2).mod(p));
        System.out.println("y1^2 mod p = " + y1.pow(2).mod(p));
        ECPoint ecPoint0_ = new ECPoint.Fp(Ecc.domain.getCurve(), new ECFieldElement.Fp(p, r),new ECFieldElement.Fp(p, y0), false);
        ECPoint ecPoint1_ = new ECPoint.Fp(Ecc.domain.getCurve(), new ECFieldElement.Fp(p, r),new ECFieldElement.Fp(p, y1), false);

        System.out.println(ecPoint0_);
        System.out.println(ecPoint1_);

        //ECPoint ecPoint0 = new SecP256R1Point(Ecc.domain.getCurve(), new ECFieldElement.Fp(p, r), new ECFieldElement.Fp(p, y0));
        //ECPoint ecPoint1 = new SecP256R1Point(Ecc.domain.getCurve(), new ECFieldElement.Fp(p, r), new ECFieldElement.Fp(p, y1));
        System.out.println(ecPoint0_.isValid());
        System.out.println(ecPoint1_.isValid());

        BigInteger s_ = s.modInverse(n);
        BigInteger hs_ = s_.multiply(calculateE(n, hash));
        BigInteger rs_ = r.multiply(s_);
        ECPoint P1 = G.multiply(hs_);

        System.out.println("ecPoint0_ is SecP256R1Point?" + (ecPoint0_ instanceof SecP256R1Point));

        System.out.println("ecPoint1_ is SecP256R1Point?" + (ecPoint1_ instanceof SecP256R1Point));

        ECPoint pk_0 = ecPoint0_.subtract(P1).multiply(rs_.modInverse(n));
        ECPoint pk_1 = ecPoint1_.subtract(P1).multiply(rs_.modInverse(n));

        byte[] pubKey0 = pk_0.getEncoded(false);
        byte[] pubKey1 = pk_1.getEncoded(false);

        byte[] pkHash0 = Hash.ripeMD160(Hash.hashUsingSha256(pubKey0));
        byte[] pkHash1 = Hash.ripeMD160(Hash.hashUsingSha256(pubKey1));

        String address0 = Base58.encodeChecked(Common.nist, pkHash0);
        String address1 = Base58.encodeChecked(Common.nist, pkHash1);

        System.out.println("address0 = " + address0);
        System.out.println("address1 = " + address1);

        System.out.println("account1 address = " + account1.getAddress());




//        System.out.println("account1 balance = " + client.getBalance(account1.getAddress(),false));
//        System.out.println("account2 balance = " + client.getBalance(account2.getAddress(),false));
//
//        client.transfer(account1, account2.getAddress(), BigInteger.valueOf(10000000L), "1");
//
//        System.out.println("account1 balance = " + client.getBalance(account1.getAddress(),false));
//        System.out.println("account2 balance = " + client.getBalance(account2.getAddress(),false));
    }

    @Test
    public void NewAccount(){
        //Account account = Account.createAndSaveToFile("./account2", "", 1, 1);
        Account account = Account.getAccountFromFile("./account2", "");
        System.out.println(account.getAddress());
        String message = "123456";

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

    protected BigInteger calculateE(BigInteger n, byte[] message)
    {
        int log2n = n.bitLength();
        int messageBitLength = message.length * 8;

        BigInteger e = new BigInteger(1, message);
        if (log2n < messageBitLength)
        {
            e = e.shiftRight(messageBitLength - log2n);
        }
        return e;
    }
}
