/*
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.chain.mgr.data.table;

import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * table init.
 *
 */
@Configuration
public class TableInitConfig implements InitializingBean {
    @Autowired
    private TableService tableService;
    @Autowired
    private GroupService groupService;

    @Override
    public void afterPropertiesSet() throws Exception {
        tableService.newCommonTable();
        List<TbGroup> groupList = groupService.getGroupList(null, DataStatus.NORMAL.getValue());
        // create sub table
        groupList.forEach(group -> tableService.newSubTable(group.getChainId(), group.getGroupId()));
    }
}
