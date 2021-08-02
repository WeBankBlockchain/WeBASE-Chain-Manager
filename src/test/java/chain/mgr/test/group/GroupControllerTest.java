/**
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package chain.mgr.test.group;

import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.Application;
import com.webank.webase.chain.mgr.group.entity.ReqGenerateGroup;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class GroupControllerTest {
    
    private MockMvc mockMvc;
    private Integer chainId = 1001;
    private String nodeId = "6d8d03b04da71c48273a19a24a34d9fe7b48155d3450e697f6a7c6012d0b22a82b53c25ecbe455c8fa439ceb556dd8c885c3d82309d375d355d6ae662f00a2ac";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    @Test
    public void testGenerateSingle() throws Exception {
        List<String> nodeList = new ArrayList<>();
        nodeList.add(nodeId);
        
        ReqGenerateGroup param = new ReqGenerateGroup();
        param.setChainId(chainId);
        param.setGenerateGroupId(2);
        param.setTimestamp(BigInteger.valueOf(new Date().getTime()));
        param.setNodeList(nodeList);
        param.setDescription("test");
        
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/group/generate/6d8d03b04da71c48273a19a24a34d9fe7b48155d3450e697f6a7c6012d0b22a82b53c25ecbe455c8fa439ceb556dd8c885c3d82309d375d355d6ae662f00a2ac").
                content(JsonTools.toJSONString(param)).
                contentType(MediaType.APPLICATION_JSON)
                );
        resultActions.
        andExpect(MockMvcResultMatchers.status().isOk()).
        andDo(MockMvcResultHandlers.print());
        System.out.println("response:"+resultActions.andReturn().getResponse().getContentAsString());
    }
    
    @Test
    public void testGenerate() throws Exception {
        List<String> nodeList = new ArrayList<>();
        nodeList.add(nodeId);
        
        ReqGenerateGroup param = new ReqGenerateGroup();
        param.setChainId(chainId);
        param.setGenerateGroupId(2);
        param.setTimestamp(BigInteger.valueOf(new Date().getTime()));
        param.setNodeList(nodeList);
        param.setDescription("test");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post( "/group/generate").
            content(JsonTools.toJSONString(param)).
            contentType(MediaType.APPLICATION_JSON)
        );
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println("response:"+resultActions.andReturn().getResponse().getContentAsString());
    }
    
    @Test
    public void testStart() throws Exception {
        String uri = "/start/2/" + nodeId;
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(uri));
        resultActions.
        andExpect(MockMvcResultMatchers.status().isOk()).
        andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }
    
    @Test
    public void testUpdate() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/group/update"));
        resultActions.
        andExpect(MockMvcResultMatchers.status().isOk()).
        andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }
    
    @Test
    public void testGeneral() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/group/general/100001/1"));
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetAll() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/group/all/100001"));
        resultActions.
            andExpect(MockMvcResultMatchers.status().isOk()).
            andDo(MockMvcResultHandlers.print());
        System.out.println("=================================response:"+resultActions.andReturn().getResponse().getContentAsString());
    }
}
