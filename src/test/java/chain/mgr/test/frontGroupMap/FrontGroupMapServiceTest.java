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
package chain.mgr.test.frontGroupMap;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.webase.chain.mgr.Application;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontGroupMapMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class FrontGroupMapServiceTest {

    @Autowired
    private TbFrontGroupMapMapper tbFrontGroupMapMapper;

    @Test
    public void getListTest() {
        List<FrontGroup> list = tbFrontGroupMapMapper.selectByGroupId(1);
        assert (list != null);
        System.out.println(JsonTools.toJSONString(list));
    }

    @Test
    public void listByGroupIdTest() {
        List<FrontGroup> list = this.tbFrontGroupMapMapper.selectByGroupId(1);
        assert (list != null);
        System.out.println(JsonTools.toJSONString(list));
    }
}
