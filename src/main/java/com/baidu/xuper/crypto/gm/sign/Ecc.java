package com.baidu.xuper.crypto.gm.sign;

import com.baidu.xuper.crypto.gm.utils.sm2.SM2Utils;

public class Ecc {

    public static byte[] sign(byte[] data, byte[] privateKey) throws Exception {
       return SM2Utils.sign(privateKey, data);
    }

}
