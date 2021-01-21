/**
 * Copyright 2014-2019 the original author or authors.
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
package chain.mgr.test.contract;

import com.webank.webase.chain.mgr.util.CommUtils;
import com.webank.webase.chain.mgr.Application;
import com.webank.webase.chain.mgr.base.tools.CommonUtils;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.contract.entity.*;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.sign.req.ReqNewUser;
import com.webank.webase.chain.mgr.trans.entity.ReqSendByContractIdVO;
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

import java.io.File;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class ContractControllerTest {

    private MockMvc mockMvc;
    private Integer chainId = 1;
    private String nodeId = "53060c93c5c7bfdc2b35ffae766e5e9f0ca16340f8e4ed09421cbbdb86cc974d57eb6460d41c33a71634f033a898d92486dd5081e2db1672bd426fff6e4af5f8";
    private Integer groupId = 1;
    private String signUserId = null;
    private Integer contractId = null;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }



    @Test
    public void batchTest() throws Exception {
        System.out.println(">>>> start newUserTest.....");
        newUserTest();
        System.out.println(">>>> start testSaveContract.....");
        testSaveContract();
        System.out.println(">>>> start testDeployByContractId.....");
        testDeployByContractId();
        System.out.println(">>>> start testTrans_set.....");
        testTrans_set();
        System.out.println(">>>> start testTrans_get.....");
        testTrans_get();
        System.out.println(">>>> all success.....");
    }


    public void newUserTest() throws Exception {
        signUserId = RandomStringUtils.randomAlphanumeric(12);
        ReqNewUser param = new ReqNewUser();
        param.setSignUserId(signUserId);
        param.setAppId(String.valueOf(groupId));
        param.setEncryptType(0);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/user/newUser")
                .content(JsonTools.toJSONString(param)).contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
        System.out.println(
                "response:" + resultActions.andReturn().getResponse().getContentAsString());
    }


    @Test
    public void testSaveContract() throws Exception {
        Contract testNew = new Contract();
        testNew.setChainId(chainId);
        testNew.setGroupId(groupId);
        testNew.setContractName("Ok");
        testNew.setContractPath("myPath");
        testNew.setContractSource(
                "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsNCmNvbnRyYWN0IE9rew0KICAgIA0KICAgIHN0cnVjdCBBY2NvdW50ew0KICAgICAgICBhZGRyZXNzIGFjY291bnQ7DQogICAgICAgIHVpbnQgYmFsYW5jZTsNCiAgICB9DQogICAgDQogICAgc3RydWN0ICBUcmFuc2xvZyB7DQogICAgICAgIHN0cmluZyB0aW1lOw0KICAgICAgICBhZGRyZXNzIGZyb207DQogICAgICAgIGFkZHJlc3MgdG87DQogICAgICAgIHVpbnQgYW1vdW50Ow0KICAgIH0NCiAgICANCiAgICBBY2NvdW50IGZyb207DQogICAgQWNjb3VudCB0bzsNCiAgICANCiAgICBUcmFuc2xvZ1tdIGxvZzsNCg0KICAgIGZ1bmN0aW9uIE9rKCl7DQogICAgICAgIGZyb20uYWNjb3VudD0weDE7DQogICAgICAgIGZyb20uYmFsYW5jZT0xMDAwMDAwMDAwMDsNCiAgICAgICAgdG8uYWNjb3VudD0weDI7DQogICAgICAgIHRvLmJhbGFuY2U9MDsNCg0KICAgIH0NCiAgICBmdW5jdGlvbiBnZXQoKWNvbnN0YW50IHJldHVybnModWludCl7DQogICAgICAgIHJldHVybiB0by5iYWxhbmNlOw0KICAgIH0NCiAgICBmdW5jdGlvbiB0cmFucyh1aW50IG51bSl7DQogICAgCWZyb20uYmFsYW5jZT1mcm9tLmJhbGFuY2UtbnVtOw0KICAgIAl0by5iYWxhbmNlKz1udW07DQogICAgDQogICAgCWxvZy5wdXNoKFRyYW5zbG9nKCIyMDE3MDQxMyIsZnJvbS5hY2NvdW50LHRvLmFjY291bnQsbnVtKSk7DQogICAgfQ0KDQoNCg0KfQ==");
        testNew.setBytecodeBin(
                "6060604052341561000c57fe5b5b6001600060000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506402540be4006000600101819055506002600260000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060006002600101819055505b5b610443806100c26000396000f30060606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806366c99139146100465780636d4ce63c14610066575bfe5b341561004e57fe5b610064600480803590602001909190505061008c565b005b341561006e57fe5b610076610264565b6040518082815260200191505060405180910390f35b806000600101540360006001018190555080600260010160008282540192505081905550600480548060010182816100c49190610272565b916000526020600020906004020160005b608060405190810160405280604060405190810160405280600881526020017f32303137303431330000000000000000000000000000000000000000000000008152508152602001600060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600260000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200185815250909190915060008201518160000190805190602001906101c49291906102a4565b5060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160020160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550606082015181600301555050505b50565b600060026001015490505b90565b81548183558181151161029f5760040281600402836000526020600020918201910161029e9190610324565b5b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e557805160ff1916838001178555610313565b82800160010185558215610313579182015b828111156103125782518255916020019190600101906102f7565b5b50905061032091906103aa565b5090565b6103a791905b808211156103a357600060008201600061034491906103cf565b6001820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff02191690556002820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff021916905560038201600090555060040161032a565b5090565b90565b6103cc91905b808211156103c85760008160009055506001016103b0565b5090565b90565b50805460018160011615610100020316600290046000825580601f106103f55750610414565b601f01602090049060005260206000209081019061041391906103aa565b5b505600a165627a7a72305820d453cb481a312519166e409e7248d76d8c2672458c08b9500945a4004a1b69020029");
        testNew.setContractAbi(
                "[{\"constant\":false,\"inputs\":[{\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"trans\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"type\":\"constructor\"}]");
        testNew.setContractBin(
                "60606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806366c99139146100465780636d4ce63c14610066575bfe5b341561004e57fe5b610064600480803590602001909190505061008c565b005b341561006e57fe5b610076610264565b6040518082815260200191505060405180910390f35b806000600101540360006001018190555080600260010160008282540192505081905550600480548060010182816100c49190610272565b916000526020600020906004020160005b608060405190810160405280604060405190810160405280600881526020017f32303137303431330000000000000000000000000000000000000000000000008152508152602001600060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600260000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200185815250909190915060008201518160000190805190602001906101c49291906102a4565b5060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160020160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550606082015181600301555050505b50565b600060026001015490505b90565b81548183558181151161029f5760040281600402836000526020600020918201910161029e9190610324565b5b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e557805160ff1916838001178555610313565b82800160010185558215610313579182015b828111156103125782518255916020019190600101906102f7565b5b50905061032091906103aa565b5090565b6103a791905b808211156103a357600060008201600061034491906103cf565b6001820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff02191690556002820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff021916905560038201600090555060040161032a565b5090565b90565b6103cc91905b808211156103c85760008160009055506001016103b0565b5090565b90565b50805460018160011615610100020316600290046000825580601f106103f55750610414565b601f01602090049060005260206000209081019061041391906103aa565b5b505600a165627a7a72305820d453cb481a312519166e409e7248d76d8c2672458c08b9500945a4004a1b69020029");

//        Contract testUpdate = new Contract();
//        testUpdate.setChainId(chainId);
//        testUpdate.setGroupId(groupId);
//        testUpdate.setContractId(400001);
//        testUpdate.setContractName("Ooook");
//        testUpdate.setContractPath("myPath");
//        testUpdate.setContractSource(
//                "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsNCmNvbnRyYWN0IE9rew0KICAgIA0KICAgIHN0cnVjdCBBY2NvdW50ew0KICAgICAgICBhZGRyZXNzIGFjY291bnQ7DQogICAgICAgIHVpbnQgYmFsYW5jZTsNCiAgICB9DQogICAgDQogICAgc3RydWN0ICBUcmFuc2xvZyB7DQogICAgICAgIHN0cmluZyB0aW1lOw0KICAgICAgICBhZGRyZXNzIGZyb207DQogICAgICAgIGFkZHJlc3MgdG87DQogICAgICAgIHVpbnQgYW1vdW50Ow0KICAgIH0NCiAgICANCiAgICBBY2NvdW50IGZyb207DQogICAgQWNjb3VudCB0bzsNCiAgICANCiAgICBUcmFuc2xvZ1tdIGxvZzsNCg0KICAgIGZ1bmN0aW9uIE9rKCl7DQogICAgICAgIGZyb20uYWNjb3VudD0weDE7DQogICAgICAgIGZyb20uYmFsYW5jZT0xMDAwMDAwMDAwMDsNCiAgICAgICAgdG8uYWNjb3VudD0weDI7DQogICAgICAgIHRvLmJhbGFuY2U9MDsNCg0KICAgIH0NCiAgICBmdW5jdGlvbiBnZXQoKWNvbnN0YW50IHJldHVybnModWludCl7DQogICAgICAgIHJldHVybiB0by5iYWxhbmNlOw0KICAgIH0NCiAgICBmdW5jdGlvbiB0cmFucyh1aW50IG51bSl7DQogICAgCWZyb20uYmFsYW5jZT1mcm9tLmJhbGFuY2UtbnVtOw0KICAgIAl0by5iYWxhbmNlKz1udW07DQogICAgDQogICAgCWxvZy5wdXNoKFRyYW5zbG9nKCIyMDE3MDQxMyIsZnJvbS5hY2NvdW50LHRvLmFjY291bnQsbnVtKSk7DQogICAgfQ0KDQoNCg0KfQ==");
//        testUpdate.setBytecodeBin(
//                "6060604052341561000c57fe5b5b6001600060000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506402540be4006000600101819055506002600260000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060006002600101819055505b5b610443806100c26000396000f30060606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806366c99139146100465780636d4ce63c14610066575bfe5b341561004e57fe5b610064600480803590602001909190505061008c565b005b341561006e57fe5b610076610264565b6040518082815260200191505060405180910390f35b806000600101540360006001018190555080600260010160008282540192505081905550600480548060010182816100c49190610272565b916000526020600020906004020160005b608060405190810160405280604060405190810160405280600881526020017f32303137303431330000000000000000000000000000000000000000000000008152508152602001600060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600260000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200185815250909190915060008201518160000190805190602001906101c49291906102a4565b5060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160020160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550606082015181600301555050505b50565b600060026001015490505b90565b81548183558181151161029f5760040281600402836000526020600020918201910161029e9190610324565b5b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e557805160ff1916838001178555610313565b82800160010185558215610313579182015b828111156103125782518255916020019190600101906102f7565b5b50905061032091906103aa565b5090565b6103a791905b808211156103a357600060008201600061034491906103cf565b6001820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff02191690556002820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff021916905560038201600090555060040161032a565b5090565b90565b6103cc91905b808211156103c85760008160009055506001016103b0565b5090565b90565b50805460018160011615610100020316600290046000825580601f106103f55750610414565b601f01602090049060005260206000209081019061041391906103aa565b5b505600a165627a7a72305820d453cb481a312519166e409e7248d76d8c2672458c08b9500945a4004a1b69020029");
//        testUpdate.setContractAbi(
//                "[{\"constant\":false,\"inputs\":[{\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"trans\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"type\":\"constructor\"}]");
//        testUpdate.setContractBin(
//                "60606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806366c99139146100465780636d4ce63c14610066575bfe5b341561004e57fe5b610064600480803590602001909190505061008c565b005b341561006e57fe5b610076610264565b6040518082815260200191505060405180910390f35b806000600101540360006001018190555080600260010160008282540192505081905550600480548060010182816100c49190610272565b916000526020600020906004020160005b608060405190810160405280604060405190810160405280600881526020017f32303137303431330000000000000000000000000000000000000000000000008152508152602001600060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600260000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200185815250909190915060008201518160000190805190602001906101c49291906102a4565b5060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160020160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550606082015181600301555050505b50565b600060026001015490505b90565b81548183558181151161029f5760040281600402836000526020600020918201910161029e9190610324565b5b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e557805160ff1916838001178555610313565b82800160010185558215610313579182015b828111156103125782518255916020019190600101906102f7565b5b50905061032091906103aa565b5090565b6103a791905b808211156103a357600060008201600061034491906103cf565b6001820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff02191690556002820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff021916905560038201600090555060040161032a565b5090565b90565b6103cc91905b808211156103c85760008160009055506001016103b0565b5090565b90565b50805460018160011615610100020316600290046000825580601f106103f55750610414565b601f01602090049060005260206000209081019061041391906103aa565b5b505600a165627a7a72305820d453cb481a312519166e409e7248d76d8c2672458c08b9500945a4004a1b69020029");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/contract/save")
                .content(JsonTools.toJSONString(testNew)).contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
        System.out.println(
                "response:" + resultActions.andReturn().getResponse().getContentAsString());
        TbContract tbContract =  CommUtils.getResultData(resultActions.andReturn().getResponse().getContentAsString(), TbContract.class);
        contractId = tbContract.getContractId();
    }

    @Test
    public void testQueryContractList() throws Exception {
        QueryContractParam param = new QueryContractParam();
        param.setChainId(chainId);
        param.setGroupId(groupId);
        // param.setContractStatus(2);
        // param.setContractName("OK");
        // param.setContractAddress("0x19146d3a2f138aacb97ac52dd45dd7ba7cb3e04a");
        param.setPageNumber(1);
        param.setPageSize(10);

        ResultActions resultActions =
                mockMvc.perform(MockMvcRequestBuilders.post("/contract/contractList")
                        .content(JsonTools.toJSONString(param)).contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
        System.out.println(
                "response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testDeploy() throws Exception {
        // param
        DeployInputParam deployInputParam = new DeployInputParam();
        deployInputParam.setGroupId(groupId);
        deployInputParam.setSignUserId("");
        deployInputParam.setContractPath("myPath");
        deployInputParam.setContractId(200001);
        deployInputParam.setContractName("Ok");
        deployInputParam.setContractSource(
                "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsNCmNvbnRyYWN0IE9rew0KICAgIA0KICAgIHN0cnVjdCBBY2NvdW50ew0KICAgICAgICBhZGRyZXNzIGFjY291bnQ7DQogICAgICAgIHVpbnQgYmFsYW5jZTsNCiAgICB9DQogICAgDQogICAgc3RydWN0ICBUcmFuc2xvZyB7DQogICAgICAgIHN0cmluZyB0aW1lOw0KICAgICAgICBhZGRyZXNzIGZyb207DQogICAgICAgIGFkZHJlc3MgdG87DQogICAgICAgIHVpbnQgYW1vdW50Ow0KICAgIH0NCiAgICANCiAgICBBY2NvdW50IGZyb207DQogICAgQWNjb3VudCB0bzsNCiAgICANCiAgICBUcmFuc2xvZ1tdIGxvZzsNCg0KICAgIGZ1bmN0aW9uIE9rKCl7DQogICAgICAgIGZyb20uYWNjb3VudD0weDE7DQogICAgICAgIGZyb20uYmFsYW5jZT0xMDAwMDAwMDAwMDsNCiAgICAgICAgdG8uYWNjb3VudD0weDI7DQogICAgICAgIHRvLmJhbGFuY2U9MDsNCg0KICAgIH0NCiAgICBmdW5jdGlvbiBnZXQoKWNvbnN0YW50IHJldHVybnModWludCl7DQogICAgICAgIHJldHVybiB0by5iYWxhbmNlOw0KICAgIH0NCiAgICBmdW5jdGlvbiB0cmFucyh1aW50IG51bSl7DQogICAgCWZyb20uYmFsYW5jZT1mcm9tLmJhbGFuY2UtbnVtOw0KICAgIAl0by5iYWxhbmNlKz1udW07DQogICAgDQogICAgCWxvZy5wdXNoKFRyYW5zbG9nKCIyMDE3MDQxMyIsZnJvbS5hY2NvdW50LHRvLmFjY291bnQsbnVtKSk7DQogICAgfQ0KDQoNCg0KfQ==");
        deployInputParam.setBytecodeBin(
                "6060604052341561000c57fe5b5b6001600060000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506402540be4006000600101819055506002600260000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060006002600101819055505b5b610443806100c26000396000f30060606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806366c99139146100465780636d4ce63c14610066575bfe5b341561004e57fe5b610064600480803590602001909190505061008c565b005b341561006e57fe5b610076610264565b6040518082815260200191505060405180910390f35b806000600101540360006001018190555080600260010160008282540192505081905550600480548060010182816100c49190610272565b916000526020600020906004020160005b608060405190810160405280604060405190810160405280600881526020017f32303137303431330000000000000000000000000000000000000000000000008152508152602001600060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600260000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200185815250909190915060008201518160000190805190602001906101c49291906102a4565b5060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160020160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550606082015181600301555050505b50565b600060026001015490505b90565b81548183558181151161029f5760040281600402836000526020600020918201910161029e9190610324565b5b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e557805160ff1916838001178555610313565b82800160010185558215610313579182015b828111156103125782518255916020019190600101906102f7565b5b50905061032091906103aa565b5090565b6103a791905b808211156103a357600060008201600061034491906103cf565b6001820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff02191690556002820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff021916905560038201600090555060040161032a565b5090565b90565b6103cc91905b808211156103c85760008160009055506001016103b0565b5090565b90565b50805460018160011615610100020316600290046000825580601f106103f55750610414565b601f01602090049060005260206000209081019061041391906103aa565b5b505600a165627a7a72305820d453cb481a312519166e409e7248d76d8c2672458c08b9500945a4004a1b69020029");
        deployInputParam.setContractAbi(
                "[{\"constant\":false,\"inputs\":[{\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"trans\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"type\":\"constructor\"}]");
        deployInputParam.setContractBin(
                "60606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806366c99139146100465780636d4ce63c14610066575bfe5b341561004e57fe5b610064600480803590602001909190505061008c565b005b341561006e57fe5b610076610264565b6040518082815260200191505060405180910390f35b806000600101540360006001018190555080600260010160008282540192505081905550600480548060010182816100c49190610272565b916000526020600020906004020160005b608060405190810160405280604060405190810160405280600881526020017f32303137303431330000000000000000000000000000000000000000000000008152508152602001600060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600260000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200185815250909190915060008201518160000190805190602001906101c49291906102a4565b5060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160020160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550606082015181600301555050505b50565b600060026001015490505b90565b81548183558181151161029f5760040281600402836000526020600020918201910161029e9190610324565b5b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e557805160ff1916838001178555610313565b82800160010185558215610313579182015b828111156103125782518255916020019190600101906102f7565b5b50905061032091906103aa565b5090565b6103a791905b808211156103a357600060008201600061034491906103cf565b6001820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff02191690556002820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff021916905560038201600090555060040161032a565b5090565b90565b6103cc91905b808211156103c85760008160009055506001016103b0565b5090565b90565b50805460018160011615610100020316600290046000825580601f106103f55750610414565b601f01602090049060005260206000209081019061041391906103aa565b5b505600a165627a7a72305820d453cb481a312519166e409e7248d76d8c2672458c08b9500945a4004a1b69020029");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/contract/deploy").content(JsonTools.toJSONString(deployInputParam))
                .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
        System.out.println(
                "response:" + resultActions.andReturn().getResponse().getContentAsString());
    }


    @Test
    public void testDeployByContractId() throws Exception {
        // param
        ReqDeployByContractIdVO deployInputParam = new ReqDeployByContractIdVO();
        deployInputParam.setSignUserId(signUserId);
        deployInputParam.setContractId(contractId);
        deployInputParam.setConstructorParams(Arrays.asList());

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/contract/deployByContractId").content(JsonTools.toJSONString(deployInputParam))
                .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
        System.out.println(
                "response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testTrans_set() throws Exception {
        // param
        ReqSendByContractIdVO param = new ReqSendByContractIdVO();
        param.setContractId(contractId);
        param.setSignUserId(signUserId);
        param.setFuncName("trans");
        param.setFuncParam(Arrays.asList(2));

        ResultActions resultActions =
                mockMvc.perform(MockMvcRequestBuilders.post("/trans/sendByContractId")
                        .content(JsonTools.toJSONString(param)).contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
        System.out.println(
                "response:" + resultActions.andReturn().getResponse().getContentAsString());
    }


    @Test
    public void testTrans_get() throws Exception {
        // param
        ReqSendByContractIdVO param = new ReqSendByContractIdVO();
        param.setContractId(contractId);
        param.setSignUserId(signUserId);
        param.setFuncName("get");
        param.setFuncParam(Arrays.asList());

        ResultActions resultActions =
                mockMvc.perform(MockMvcRequestBuilders.post("/trans/sendByContractId")
                        .content(JsonTools.toJSONString(param)).contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
        System.out.println(
                "response:" + resultActions.andReturn().getResponse().getContentAsString());
    }


//    @Test
//    public void testSendTransaction() throws Exception {
//        // abi
//        String abiStr =
//                "[{\"constant\":false,\"inputs\":[{\"name\":\"num\",\"type\":\"uint256\"}],\"name\":\"trans\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"a\",\"type\":\"string\"}],\"name\":\"abb\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"b\",\"type\":\"string\"}],\"name\":\"bba\",\"type\":\"event\"}]";
//        List<Object> abiList = JsonTools.toJavaObjectList(abiStr, Object.class);
//
//        // param
//        TransactionInputParam param = new TransactionInputParam();
//        param.setContractId(200069);
//        param.setGroupId(groupId);
//        param.setSignUserId("");
//        param.setContractName("Ok");
//        param.setFuncName("trans");
//        param.setFuncParam(Arrays.asList(3));
//
//        // if make exception
//        param.setFuncParam(Arrays.asList("asdfasfasd"));
//
//        ResultActions resultActions =
//                mockMvc.perform(MockMvcRequestBuilders.post("/contract/transaction")
//                        .content(JsonTools.toJSONString(param)).contentType(MediaType.APPLICATION_JSON));
//        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
//                .andDo(MockMvcResultHandlers.print());
//        System.out.println(
//                "response:" + resultActions.andReturn().getResponse().getContentAsString());
//    }


    @Test
    public void testCompileContract() throws Exception {
        List<File> fileList = new ArrayList<File>();
        fileList.add(new File("D:\\云盘共享\\solidity\\HeHe.sol"));
//        fileList.add(new File("D:\\project\\sol\\EvidenceFactory.sol"));
        String base64 = CommonUtils.fileToZipBase64(fileList);
        System.out.println("base64" + base64);

        Map<String, Object> param = new HashMap<>();
        param.put("chainId", chainId);
        param.put("nodeId", nodeId);
        param.put("contractZipBase64", base64);

        ResultActions resultActions =
                mockMvc.perform(MockMvcRequestBuilders.post("/contract/compile")
                        .content(JsonTools.toJSONString(param)).contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
        System.out.println(
                "response:" + resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testCompileContract2() throws Exception {
        String filePath = "D:\\project\\sol\\HelloWorld.zip";
        String base64 = CommonUtils.fileToBase64(filePath);
        System.out.println("base64：" + base64);

        Map<String, Object> param = new HashMap<>();
        param.put("chainId", chainId);
        param.put("nodeId", nodeId);
        param.put("contractZipBase64", base64);

        ResultActions resultActions =
                mockMvc.perform(MockMvcRequestBuilders.post("/contract/compile")
                        .content(JsonTools.toJSONString(param)).contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
        System.out.println(
                "response:" + resultActions.andReturn().getResponse().getContentAsString());
    }
}
