/**
 * Copyright 2014-2020 the original author or authors.
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

package chain.mgr.test.data;

import com.webank.webase.chain.mgr.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class DataGroupControllerTest {

    private static final int chainId = 1;
    private static final int groupId = 1;
    private static final int pageNumber = 1;
    private static final int pageSize = 5;
    private String transHash = "0xfcb5e3a06c90b3b82757aa014e8fdef56976f4c77f83511b52b6ec228ba5d99b";
    private String blockNumber = "1";
    private String contractName = "HelloWorld";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testGetGroupList() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
            .get("/datagroup/list" + "?chainId=" +  chainId));
        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print());
        System.out.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testRefreshTables() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
            .get("/datagroup/refresh"));
        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print());
        System.out.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetGeneral() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
            .get("/datagroup/general" + "/" + chainId +  "/" + groupId ));
        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print());
        System.out.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetTxnDaily() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
            .get("/datagroup/txnDaily" + "/" + chainId + "/" + groupId ));
        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print());
        System.out.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }


    @Test
    public void testListNode() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
            .get("/datagroup/nodeList"
                + "/" + chainId + "/" + groupId
                + "/" + pageNumber + "/" + pageSize));
        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print());
        System.out.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    /**
     * todo add search block hash, block number
     * @throws Exception
     */
    @Test
    public void testListBlock() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
            .get("/datagroup/blockList"
                + "/" + chainId + "/" + groupId
                + "/" + pageNumber + "/" + pageSize));
        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print());
        System.out.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    /**
     * todo add search trans hash, block number
     * @throws Exception
     */
    @Test
    public void testListTxn() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
            .get("/datagroup/transList"
                + "/" + chainId + "/" + groupId
                + "/" + pageNumber + "/" + pageSize));
        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print());
        System.out.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetTxnReceipt() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
            .get("/datagroup/transactionReceipt"
                + "/" + chainId + "/" + groupId
                + "/" + transHash));
        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print());
        System.out.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }


    /**
     * transaction info
     * @throws Exception
     */
    @Test
    public void testGetTxnInfo() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
            .get("/datagroup/transInfo"
                + "/" + chainId + "/" + groupId
                + "/" + transHash));
        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print());
        System.out.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testListTransCountByContract() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
            .get("/datagroup/contractList"
                + "/" + chainId + "/" + groupId
                + "/" + pageNumber + "/" + pageSize
                + "/" + contractName));
        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print());
        System.out.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }


}
