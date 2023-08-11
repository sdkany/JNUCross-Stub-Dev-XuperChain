package com.webank.wecross.stub.xuperchain.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class Utils {

    private static final String HEX_PREFIX = "0x";

    public static ObjectMapper objectMapper = new ObjectMapper();


    public static String getResourceName(String path) {
        String[] sp = path.split("\\.");

        return sp[sp.length - 1];
    }

    public static String hexRemove0x(String hex) {
        if (hex.contains("0x")) {
            return hex.substring(2);
        }
        return hex;
    }

    public static String hexStr2Str(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, StandardCharsets.UTF_8); // UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();
        return buffer.getLong();
    }

    public static byte[] toByteArray (Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray ();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    public static Object toObject (byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return obj;
    }
    public static String toHexStringWithPrefix(BigInteger value) {
        return HEX_PREFIX + value.toString(16);
    }

    public static String toHexStringNoPrefix(BigInteger value) {
        return value.toString(16);
    }

    public static String toHexStringNoPrefix(byte[] input) {
        return toHexString(input, 0, input.length, false);
    }


    public static String toHexString(byte[] input, int offset, int length, boolean withPrefix) {
        StringBuilder stringBuilder = new StringBuilder();
        if (withPrefix) {
            stringBuilder.append("0x");
        }
        for (int i = offset; i < offset + length; i++) {
            stringBuilder.append(String.format("%02x", input[i] & 0xFF));
        }

        return stringBuilder.toString();
    }

    public static String toHexString(byte[] input) {
        return toHexString(input, 0, input.length, true);
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
}
