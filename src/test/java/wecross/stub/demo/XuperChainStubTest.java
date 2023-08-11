package wecross.stub.demo;

import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.xuperchain.XuperChainStubFactory;
import org.junit.Assert;
import org.junit.Test;

public class XuperChainStubTest {
    @Test
    public void DemoStubFactoryTest() {
        StubFactory stubFactory = new XuperChainStubFactory();
        Assert.assertNotNull("stubFactory object is null", stubFactory);
    }
}
