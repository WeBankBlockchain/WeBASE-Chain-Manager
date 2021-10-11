package chain.mgr.test.contract;

import chain.mgr.test.TestBase;
import com.webank.webase.chain.mgr.contract.CompileService;
import com.webank.webase.chain.mgr.group.GroupManager;
import java.util.Collections;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContractServiceTest extends TestBase {
    @Autowired
    private CompileService compileService;
    @Autowired
    private GroupManager groupManager;

    private int contractId = 1;

    @Test
    public void compileTest() {
        compileService.compileByContractId(contractId);
    }

    @Test
    public void tempTest() {

        boolean b = CollectionUtils.isEmpty(Collections.EMPTY_LIST);
        System.out.println(b);
//
//        groupManager.saveGroup("String groupName", BigInteger.valueOf(2323L), 1, 1, Arrays.asList("asd"), 5, "String description",
//                1);
    }
}



