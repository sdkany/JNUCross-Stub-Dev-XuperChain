package com.baidu.xuper.crypto.xchain.sign;

import com.webank.wecross.stub.xuperchain.utils.Numeric;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class Ecc {
    static final String curveName = "P-256";
    static final X9ECParameters curve = NISTNamedCurves.getByName(curveName);
    public static final ECDomainParameters domain = new ECDomainParameters(curve.getCurve(), curve.getG(), curve.getN(), curve.getH());

    public static final BigInteger p = Numeric.toBigInt("0xffffffff00000001000000000000000000000000ffffffffffffffffffffffff");
    public static final BigInteger a = Numeric.toBigInt("0xffffffff00000001000000000000000000000000fffffffffffffffffffffffc");
    public static final BigInteger b = Numeric.toBigInt("0x5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b");
    public static final BigInteger n = Numeric.toBigInt("0xffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551");

    static public byte[] sign(byte[] hash, BigInteger privateKey) throws IOException {
        ECDSASigner signer = new ECDSASigner();
        signer.init(true, new ECPrivateKeyParameters(privateKey, domain));
        BigInteger[] signature = signer.generateSignature(hash);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DERSequenceGenerator seq = new DERSequenceGenerator(baos);
        seq.addObject(new ASN1Integer(signature[0]));
        seq.addObject(new ASN1Integer(signature[1]));
        seq.close();
        return baos.toByteArray();
    }

    public static BigInteger calculateE(BigInteger n, byte[] message)
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
