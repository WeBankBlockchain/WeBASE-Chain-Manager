package com.webank.webase.chain.mgr.repository.bean;

import com.webank.webase.chain.mgr.base.enums.FrontTypeEnum;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import com.webank.webase.chain.mgr.base.enums.FrontStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Data
@ToString
@NoArgsConstructor
public class TbFront implements Serializable {

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TbFront front = (TbFront) o;
        return Objects.equals(frontId, front.frontId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frontId);
    }

    public static TbFront build(int chainId, String nodeId, String ip, int frontPort, String agencyName, String description, FrontStatusEnum status, FrontTypeEnum type, String version, String containerName, int jsonrpcPort, int p2pPort, int channelPort, String chainName, int extCompanyId, int extAgencyId, int extHostId, int hostIndex, String sshUser, int sshPort, int dockerPort, String rootOnHost, String nodeRootOnHost) {
        Date now = new Date();
        TbFront tbFront = new TbFront();
        tbFront.setChainId(chainId);
        tbFront.setNodeId(nodeId);
        tbFront.setFrontIp(ip);
        tbFront.setFrontPort(frontPort);
        tbFront.setAgency(agencyName);
        tbFront.setDescription(description);
        tbFront.setCreateTime(now);
        tbFront.setModifyTime(now);
        tbFront.setFrontStatus(status.getId());
        tbFront.setFrontStatus(type.getId());
        tbFront.setVersion(version);
        tbFront.setContainerName(containerName);
        tbFront.setJsonrpcPort(jsonrpcPort);
        tbFront.setP2pPort(p2pPort);
        tbFront.setChannelPort(channelPort);
        tbFront.setChainName(chainName);
        tbFront.setExtCompanyId(extCompanyId);
        tbFront.setExtAgencyId(extAgencyId);
        tbFront.setExtHostId(extHostId);
        tbFront.setHostIndex(hostIndex);
        tbFront.setSshUser(sshUser);
        tbFront.setSshPort(sshPort);
        tbFront.setDockerPort(dockerPort);
        tbFront.setRootOnHost(rootOnHost);
        tbFront.setNodeRootOnHost(nodeRootOnHost);
        return tbFront;
    }

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.front_id
     *
     * @mbg.generated
     */
    private Integer frontId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.chain_id
     *
     * @mbg.generated
     */
    private Integer chainId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.front_peer_name
     *
     * @mbg.generated
     */
    private String frontPeerName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.node_id
     *
     * @mbg.generated
     */
    private String nodeId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.front_ip
     *
     * @mbg.generated
     */
    private String frontIp;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.front_port
     *
     * @mbg.generated
     */
    private Integer frontPort;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.agency
     *
     * @mbg.generated
     */
    private String agency;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.description
     *
     * @mbg.generated
     */
    private String description;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.create_time
     *
     * @mbg.generated
     */
    private Date createTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.modify_time
     *
     * @mbg.generated
     */
    private Date modifyTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.front_status
     *
     * @mbg.generated
     */
    private Integer frontStatus;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.version
     *
     * @mbg.generated
     */
    private String version;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.container_name
     *
     * @mbg.generated
     */
    private String containerName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.jsonrpc_port
     *
     * @mbg.generated
     */
    private Integer jsonrpcPort;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.p2p_port
     *
     * @mbg.generated
     */
    private Integer p2pPort;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.channel_port
     *
     * @mbg.generated
     */
    private Integer channelPort;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.chain_name
     *
     * @mbg.generated
     */
    private String chainName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.ext_company_id
     *
     * @mbg.generated
     */
    private Integer extCompanyId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.ext_agency_id
     *
     * @mbg.generated
     */
    private Integer extAgencyId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.ext_host_id
     *
     * @mbg.generated
     */
    private Integer extHostId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.host_index
     *
     * @mbg.generated
     */
    private Integer hostIndex;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.ssh_user
     *
     * @mbg.generated
     */
    private String sshUser;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.ssh_port
     *
     * @mbg.generated
     */
    private Integer sshPort;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.docker_port
     *
     * @mbg.generated
     */
    private Integer dockerPort;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.root_on_host
     *
     * @mbg.generated
     */
    private String rootOnHost;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.node_root_on_host
     *
     * @mbg.generated
     */
    private String nodeRootOnHost;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_front.front_type
     *
     * @mbg.generated
     */
    private Integer frontType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table tb_front
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;
}
