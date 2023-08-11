package com.webank.wecross.stub.xuperchain;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.WeCrossContext;

import java.util.Map;

@Stub("StubDemo")
public class XuperChainStubFactory implements StubFactory {
    @Override
    public void init(WeCrossContext context) {

    }

    @Override
    public Driver newDriver() {
        Driver driver = new XuperChainDriver();
        // TODO, 初始化 Driver
        return driver;
    }

    @Override
    public Connection newConnection(String path) {
        Connection connection = new XuperChainConnection();
        // TODO, 初始化 Connection
        return connection;
    }

    @Override
    public Account newAccount(Map<String, Object> properties) {
        Account account = new XuperChainAccount();
        // TODO, 初始化 Account
        return account;
    }

    @Override
    public void generateAccount(String path, String[] args) {

    }

    @Override
    public void generateConnection(String path, String[] args) {

    }
}
