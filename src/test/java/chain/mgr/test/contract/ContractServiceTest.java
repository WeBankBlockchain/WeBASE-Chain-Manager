package chain.mgr.test.contract;

import com.webank.webase.chain.mgr.Application;
import com.webank.webase.chain.mgr.contract.CompileService;
import com.webank.webase.chain.mgr.group.GroupManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ContractServiceTest {
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
        groupManager.saveGroup("String groupName", BigInteger.valueOf(2323L), 1, 1, Arrays.asList("asd"), 5, "String description",
                1);
    }
}



