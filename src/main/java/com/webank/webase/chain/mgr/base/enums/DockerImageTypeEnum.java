/**
 * Copyright 2014-2020  the original author or authors.
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
 
package com.webank.webase.chain.mgr.base.enums;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum DockerImageTypeEnum {
    MANUAL((byte) 0, "Download docker image and unzip manually.","手动上传镜像到节点主机"),
    PULL_OFFICIAL((byte) 1, "Pull image from docker registry.","节点主机从 Docker 官方拉取镜像"),
    LOCAL_OFFLINE((byte) 2, "Use local image tar file and scp to host.","部署服务主机使用离线镜像，直接使用 scp 发送到节点主机"),
    DOWNLOAD_CDN((byte) 3, "Download docker image from CDN and scp to host.","部署服务主机统一从 CDN 拉取镜像后，再使用 scp 发送到节点主机"),
    HOST_DOWNLOAD_CDN((byte) 4, "Download docker image from CDN by each host.","每台节点主机单独从 CDN 拉取镜像包"),
    ;

    private byte id;
    private String description;
    private String tip;

    /**
     * @param id
     * @return
     */
    public static DockerImageTypeEnum getById(byte id) {
        for (DockerImageTypeEnum value : DockerImageTypeEnum.values()) {
            if (value.id == id) {
                return value;
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    public static Map<Integer,String> getTypeMap(){
        Map<Integer,String> map = new HashMap<>();
        for (DockerImageTypeEnum value : DockerImageTypeEnum.values()) {
            map.put(Integer.valueOf(value.getId()),value.getTip());
        }
        return map;
    }


}

