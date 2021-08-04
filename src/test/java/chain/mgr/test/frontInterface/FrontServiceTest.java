/**
 * Copyright 2014-2019  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package chain.mgr.test.frontInterface;

import com.webank.webase.chain.mgr.Application;
import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.chain.mgr.node.entity.PeerInfo;
import org.fisco.bcos.sdk.client.protocol.response.ConsensusStatus.ConsensusInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class FrontServiceTest {

    @Autowired
    private FrontInterfaceService frontInterface;
    private Integer chainId = 100001;
    private Integer groupId = 1;
    private BigInteger blockNumber = new BigInteger("1");
    private String frontPeerName = "";
    private String frontIp = "127.0.0.1";
    private Integer frontPort = 5302;

    @Test
    public void getContractCodeTest() {
        String contractAddress = "0xb68b0ca60cc4d8b207875c9a0ab6c3a782db9318";
        String str = frontInterface.getContractCode(chainId, groupId, contractAddress, blockNumber);
        assert (str != null);
        System.out.println(str);
    }

    @Test
    public void getGroupPeersTest() {
        List<String> list = frontInterface.getGroupPeers(chainId, groupId);
        assert (list != null && list.size() > 0);
        System.out.println(JsonTools.toJSONString(list));
    }

    @Test
    public void getGroupListTest() {
        List<String> list = frontInterface.getGroupListFromSpecificFront(frontPeerName,frontIp, frontPort);
        assert (list != null && list.size() > 0);
        System.out.println("=====================list:" + JsonTools.toJSONString(list));
    }

    @Test
    public void getObserverList() {
        List<String> list = frontInterface.getObserverList(chainId, groupId);
        assert (list != null && list.size() > 0);
        System.out.println("=====================list:" + JsonTools.toJSONString(list));
    }

    @Test
    public void getPeersTest() {
        PeerInfo[] list = frontInterface.getPeers(chainId, groupId);
        assert (list != null && list.length > 0);
        System.out.println("=====================list:" + JsonTools.toJSONString(list));
    }

    @Test
    public void getConsensusStatusTest() {
        ConsensusInfo consensusStatus = frontInterface.getConsensusStatus(chainId, groupId);
        assert (consensusStatus != null);
        System.out.println("=====================consensusStatus:" + consensusStatus);
    }

    @Test
    public void syncStatusTest() {
        SyncStatus status = frontInterface.getSyncStatus(chainId, groupId);
        assert (status != null);
        System.out.println("=====================status:" + JsonTools.toJSONString(status));
    }
}