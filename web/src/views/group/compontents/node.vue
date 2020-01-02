<template>
    <div>
        <v-content-head :headTitle="'节点列表'" :icon='true' :route="'/group'" @changGroup='changGroup'></v-content-head>
        <div class="module-wrapper">
            <div class="search-part" style="padding-top: 20px;">
                <div class="search-part-left">
                    <el-input placeholder="请输入节点名称" v-model="nodeName" class="input-with-select">
                        <el-button slot="append" icon="el-icon-search" @click="search"></el-button>
                    </el-input>
                    <div class="input-with-select">
                        <span>切换群组</span>
                        <el-select v-model="groupId" placeholder="请选择" @change='search'>
                            <el-option
                            v-for="item in groupList"
                            :key="item.groupId"
                            :label="item.groupName"
                            :value="item.groupId">
                            </el-option>
                        </el-select>
                    </div>
                    
                </div>
            </div>
            <div>
                <el-table :data="nodeData" class="search-table-content" v-loading="loading">
                    <el-table-column
                        prop="nodeId"
                        label="节点编号"
                        show-overflow-tooltip
                        >
                        <template slot-scope='scope'>
                            <i class="el-icon-document-copy" @click="copyNodeIdKey(scope.row.nodeId)" title="复制"></i>
                            <span>{{scope.row.nodeId}}</span>
                        </template>
                    </el-table-column>
                    <el-table-column
                        prop="groupId"
                        label="所属群组编号"
                        width='140'
                        >
                    </el-table-column>
                    <el-table-column
                        prop="nodeActive"
                        label="状态"
                        width='80'
                        >
                        <template slot-scope='scope'>
                            <span>{{scope.row.nodeActive | Status}}</span>
                        </template>
                    </el-table-column>
                    <!-- <el-table-column
                        prop="nodeIp"
                        label="节点ip"
                        width='120'
                        >
                    </el-table-column>
                    <el-table-column
                        prop="P2pPort"
                        label="节点p2p端口"
                        width='120'
                        >
                    </el-table-column> -->
                    <el-table-column
                        prop="blockNumber"
                        label="节点块高"
                        width='140'
                        >
                    </el-table-column>
                    <el-table-column
                        prop="pbftView"
                        label="pbftView"
                        width='140'
                        >
                    </el-table-column>
                    <el-table-column
                        prop="createTime"
                        label="创建时间"
                        width='160'
                        >
                    </el-table-column>
                </el-table>
                <el-pagination class="page" @size-change="handleSizeChange" 
                @current-change="handleCurrentChange" :current-page="pageNumber" 
                :page-sizes="[10, 20, 30, 50]" :page-size="pageSize" layout=" sizes, prev, pager, next, jumper" :total="total">
                </el-pagination>
            </div>
        </div>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import { getNodes,getGroups } from "@/api/api"
export default {
    name: "node",
    data() {
        return {
            nodeData: [],
            pageNumber: 1,
            pageSize: 10,
            nodeName: "",
            loading: false,
            total: 0,
            groupId: this.$route.query.groupId,
            groupList: []
        }
    },
    components: {
        "v-content-head": contentHead,
    },
    mounted: function(){
        this.getNodeList();
        this.getGroupList();
    },
    methods: {
        changGroup: function(){
            this.getNodeList();
            this.getGroupList();
        },
        getNodeList: function(){
            let data = {
                chainId: localStorage.getItem('chainId'),
                groupId: this.groupId,
                pageNumber: this.pageNumber,
                pageSize: this.pageSize
            };
            let query = {}
            if(this.nodeName){
                query.nodeName = this.nodeName
            }
            getNodes(data,query).then(res => {
                if(res.data.code === 0){
                    this.total = res.data.totalCount;
                    this.nodeData = res.data.data
                }
            })
        },
        getGroupList: function (type) {
            getGroups(localStorage.getItem('chainId')).then(res => {
                if (res.data.code === 0) {
                    if (res.data.data && res.data.data.length) {
                        this.groupList = res.data.data || []
                    } 
                } else {
                    this.groupList = [];
                }
            }).catch(err => {
                this.groupList = [];
                this.$message({
                    message: "系统错误！",
                    type: "error",
                    duration: 2000
                });
                this.$message.closeAll()
            })
        },
        search: function(){
            this.getNodeList()
        },
        handleSizeChange: function(val){
            this.pageSize = val;
            this.pageNumber = 1;
            this.getNodeList()
        },
        handleCurrentChange: function(val){
            this.pageNumber = val;
            this.getNodeList();
        },
        copyNodeIdKey(val) {
            if (!val) {
                this.$message({
                    type: "fail",
                    showClose: true,
                    message: '复制失败',
                    duration: 2000
                });
            } else {
                this.$copyText(val).then(e => {
                    this.$message({
                        type: "success",
                        showClose: true,
                        message: '复制成功',
                        duration: 2000
                    });
                });
            }
        },
    },
    filters: {
        Status: function(val){
            if(val == 1) {
                return "存活"
            }else{
                return "不存活"
            }
        }
    }
}
</script>

<style scoped>
.search-part {
    padding: 30px 0px;
    overflow: hidden;
    margin: 0;
}
.input-with-select {
    float: right;
    margin-right: 20px;
    width: 310px;
}
.input-with-select>>>.el-input__inner {
    border-top-left-radius: 20px;
    border-bottom-left-radius: 20px;
    border: 1px solid #eaedf3;
    box-shadow: 0 3px 11px 0 rgba(159, 166, 189, 0.11);
}
.input-with-select>>>.el-input--suffix > .el-input__inner {
    box-shadow: none;
}
.input-with-select>>>.el-input-group__prepend {
    border-left-color: #fff;
}
.input-with-select>>>.el-input-group__append {
    border-top-right-radius: 20px;
    border-bottom-right-radius: 20px;
    box-shadow: 0 3px 11px 0 rgba(159, 166, 189, 0.11);
}
.input-with-select>>>.el-button {
    border: 1px solid #20d4d9;
    border-radius: inherit;
    background: #20d4d9;
    color: #fff;
}
.block-table-content {
    width: 100%;
    padding-bottom: 16px;
    font-size: 12px;
}
.block-table-content /deep/ .el-table__row {
    cursor: pointer;
}
</style>