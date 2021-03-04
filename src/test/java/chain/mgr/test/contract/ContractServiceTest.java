package chain.mgr.test.contract;

import com.webank.webase.chain.mgr.Application;
import com.webank.webase.chain.mgr.contract.CompileService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.file.Paths;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ContractServiceTest {
    @Autowired
    private CompileService compileService;

    private int contractId = 1;

    @Test
    public void compileTest() {
        compileService.compileByContractId(contractId);
    }

    @Test
    public void tempTest() {
        File contractFile = Paths.get("./solidity/20210303093950/test1你.sol").toFile();
        System.out.println(contractFile);
    }
}



