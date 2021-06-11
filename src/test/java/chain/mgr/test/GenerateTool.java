package chain.mgr.test;


import com.github.yuanmomo.mybatis.mbg.MyBatisGeneratorTool;
import com.github.yuanmomo.mybatis.mbg.table.TableXMLPrinter;
import org.junit.Test;

import java.io.IOException;
import java.util.List;


/**
 *
 */

public class GenerateTool {

    /**
     * 生成 generatorConfig.xml 文件中，每张表的具体配置。
     * @throws IOException
     */
    @Test
    public void generatorConfigXml() throws IOException {
        // mbg.properties
        // 指定数据库 mbg
        List<String> output = TableXMLPrinter.print("src/test/resources/mbg.properties","webasechainmanager");
        for(String str : output){
            System.out.println(str);
        }
    }

    /**
     * 通过 generatorConfig.xml 配置文件生成 Bean，Mapper 和 Provider。
     */
    @Test
    public void mbgGenerate() {
        MyBatisGeneratorTool.generate("src/test/resources/generatorConfig.xml");
    }
}