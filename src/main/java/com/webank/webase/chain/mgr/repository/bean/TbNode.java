package com.webank.webase.chain.mgr.repository.bean;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class TbNode implements Serializable {

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_node.node_id
     *
     * @mbg.generated
     */
    private String nodeId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_node.chain_id
     *
     * @mbg.generated
     */
    private Integer chainId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_node.group_id
     *
     * @mbg.generated
     */
    private Integer groupId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_node.node_name
     *
     * @mbg.generated
     */
    private String nodeName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_node.node_ip
     *
     * @mbg.generated
     */
    private String nodeIp;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_node.p2p_port
     *
     * @mbg.generated
     */
    private Integer p2pPort;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_node.block_number
     *
     * @mbg.generated
     */
    private Long blockNumber;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_node.pbft_view
     *
     * @mbg.generated
     */
    private Long pbftView;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_node.node_active
     *
     * @mbg.generated
     */
    private Byte nodeActive;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_node.description
     *
     * @mbg.generated
     */
    private String description;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_node.create_time
     *
     * @mbg.generated
     */
    private Date createTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_node.modify_time
     *
     * @mbg.generated
     */
    private Date modifyTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table tb_node
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;
}
