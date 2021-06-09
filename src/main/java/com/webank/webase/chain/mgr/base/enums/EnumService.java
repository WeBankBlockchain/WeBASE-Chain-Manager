package com.webank.webase.chain.mgr.base.enums;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EnumService {

    @Autowired
    private ConstantProperties constantProperties;


    /**
     * check id of enum.
     *
     * @param enumId
     * @return
     */
    public DockerImageTypeEnum verifyDockerImageTypeEnumId(byte enumId) {
        log.info("start exec method[verifyDockerImageTypeEnumId]. enumId:{}", enumId);
        DockerImageTypeEnum imageTypeEnum = DockerImageTypeEnum.getById(enumId);
        if (imageTypeEnum == null) {
            log.info("fail exec method[verifyDockerImageTypeEnumId]. invalid enumId:{}", enumId);

            throw new BaseException(ConstantCode.UNKNOWN_DOCKER_IMAGE_TYPE);
        }
        log.info("success exec method[verifyDockerImageTypeEnumId]. enumId:{} result:{}", enumId, JsonTools.toJSONString(imageTypeEnum));
        return imageTypeEnum;
    }
}
