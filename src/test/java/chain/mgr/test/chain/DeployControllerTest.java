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
package chain.mgr.test.chain;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.webank.webase.chain.mgr.Application;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.chain.entity.ChainInfo;

/**
 * test chain controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class DeployControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testNewChain() throws Exception {
        ChainInfo param = new ChainInfo();
        param.setChainId(1001);
        param.setChainName("aaa");
        param.setChainType(0);
        param.setDescription("test");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post( "/chain/new").
            content(JsonTools.toJSONString(param)).
            contentType(MediaType.APPLICATION_JSON)
        );
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println("response:"+resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testQueryChainList() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/chain/all"));
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println("response:"+resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testRemoveChain() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.delete("/chain/100005"));
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println("response:"+resultActions.andReturn().getResponse().getContentAsString());
    }
}
