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
package chain.mgr.test.user;

import com.webank.webase.chain.mgr.Application;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.sign.req.ReqNewUser;
import com.webank.webase.chain.mgr.sign.req.ReqUpdateUserVo;
import com.webank.webase.chain.mgr.sign.rsp.RspUserInfo;
import com.webank.webase.chain.mgr.util.CommUtils;
import org.apache.commons.lang3.RandomStringUtils;
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

/**
 * test chain controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class UserControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }


    private static final Integer chainId = 1;
    private static String signUserId = null;
    private static final String appId = "chain_1_group_1";

    @Test
    public void batchTest() throws Exception {
        System.out.println(">>>> start testNewUser.....");
        testNewUser();
        System.out.println(">>>> start testUpdateUser.....");
        testUpdateUser();
        System.out.println(">>>> start testQueryUserPage.....");
        testQueryUserPage();
        System.out.println(">>>> all success.....");
    }

    @Test
    public void testNewUser() throws Exception {
        signUserId = "randomUser" + RandomStringUtils.randomAlphanumeric(5);
        ReqNewUser param = new ReqNewUser();
        param.setSignUserName(signUserId);
        param.setChainId(chainId);
        param.setAppId(appId);
        param.setDescription("test");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/user/newUser").
                content(JsonTools.toJSONString(param)).
                contentType(MediaType.APPLICATION_JSON)
        );
        resultActions.
                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        System.out.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
        RspUserInfo resultData = CommUtils.getResultData(resultActions.andReturn().getResponse().getContentAsString(), RspUserInfo.class);
        signUserId = resultData.getSignUserId();
    }

    @Test
    public void testUpdateUser() throws Exception {
        ReqUpdateUserVo param = new ReqUpdateUserVo();
        param.setSignUserId(signUserId);
        param.setDescription("my update");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.patch("/user/update").
                content(JsonTools.toJSONString(param)).
                contentType(MediaType.APPLICATION_JSON)
        );
        resultActions.
                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        System.out.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }


    @Test
    public void testQueryUserPage() throws Exception {
        String uri = String.format("/user/list/%s/%s/%s", appId, 1, 10);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(uri));
        resultActions.
                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        System.out.println("response:" + resultActions.andReturn().getResponse().getContentAsString());
    }
}
