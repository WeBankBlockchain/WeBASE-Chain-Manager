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

package chain.mgr.test.precompiled;

import chain.mgr.test.TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.webank.webase.chain.mgr.precompiledapi.PrecompiledService;

public class PrecompiledServiceTest extends TestBase {

    @Autowired
    private PrecompiledService precompiledService;

    private static final String chainId = "chain0";
    private static final String groupId = "group0";
    private static final String signUserId = "1SSSaFN1NXH9tfb5";
    private static final String nodeId = "518bfaf917c3ce06b1269ef03a88966a9c47123ffaf5a8a1e08da6bc274f172174a75f0e909e6bfc8f48ed1fa24aa25ef500e9d78b63a5750790c8efcbb64e52";

    @Test
    public void testAddSealer() {
        precompiledService.addSealer(chainId, groupId, signUserId, nodeId);
    }

}
