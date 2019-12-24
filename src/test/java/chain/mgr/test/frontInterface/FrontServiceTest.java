/**
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package chain.mgr.test.frontInterface;

import com.alibaba.fastjson.JSON;
import com.webank.webase.chain.mgr.Application;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.chain.mgr.node.entity.PeerInfo;
import java.math.BigInteger;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class FrontServiceTest {

    @Autowired
    private FrontInterfaceService frontInterface;
    private Integer groupId = 1;
    private BigInteger blockNumber = new BigInteger("1");
    private String frontIp = "10.107.105.138";
    private Integer frontPort = 5302;

    @Test
    public void getContractCodeTest() {
        String contractAddress = "0xb68b0ca60cc4d8b207875c9a0ab6c3a782db9318";
        String str = frontInterface.getContractCode(groupId, contractAddress, blockNumber);
        assert (str != null);
        System.out.println(str);
    }
    
    @Test
    public void getGroupPeersTest() {
        List<String> list = frontInterface.getGroupPeers(groupId);
        assert (list != null && list.size() > 0);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void getGroupListTest() {
        List<String> list = frontInterface.getGroupListFromSpecificFront(frontIp, frontPort);
        assert (list != null && list.size() > 0);
        System.out.println("=====================list:" + JSON.toJSONString(list));
    }
    
    @Test
    public void getObserverList() {
        List<String> list = frontInterface.getObserverList(groupId);
        assert (list != null && list.size() > 0);
        System.out.println("=====================list:" + JSON.toJSONString(list));
    }

    @Test
    public void getPeersTest() {
        PeerInfo[] list = frontInterface.getPeers(groupId);
        assert (list != null && list.length > 0);
        System.out.println("=====================list:" + JSON.toJSONString(list));
    }

    @Test
    public void getConsensusStatusTest() {
        String consensunsStatus = frontInterface.getConsensusStatus(groupId);
        assert (consensunsStatus != null);
        System.out.println("=====================consensunsStatus:" + consensunsStatus);
    }

    @Test
    public void syncStatusTest() {
        SyncStatus status = frontInterface.getSyncStatus(groupId);
        assert (status != null);
        System.out.println("=====================status:" + JSON.toJSONString(status));
    }
}