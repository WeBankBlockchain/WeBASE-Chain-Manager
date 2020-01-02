<template>
    <div>
        <v-content-head :headTitle="'前置管理'" @changGroup='changGroup'></v-content-head>
        <div class="module-wrapper">
            <h3 style="padding: 20px 0 0 40px;">前置列表</h3>
            <div class="search-part" style="padding-top: 20px;">
                <div class="search-part-left">
                    <el-button type="primary" class="search-part-left-btn" @click="createFront">新增节点前置</el-button>
                </div>
                <div class="search-part-right">
                    群组切换：
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
             <div class="search-table">
                <el-table :data="frontData" class="search-table-content" v-loading="loading">
                    <el-table-column
                        prop="frontId"
                        label="前置编号"
                        width="180">
                    </el-table-column>
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
                        prop="frontIp"
                        label="前置IP"
                        width="180">
                        <template slot-scope="scope">
                            <el-button type='text' size='small' @click='routeDetail(scope.row)'>{{scope.row.frontIp}}</el-button>
                        </template>
                    </el-table-column>
                    <el-table-column
                        prop="frontPort"
                        label="前置端口"
                        width="180">
                    </el-table-column>
                    <el-table-column
                        prop="agency"
                        label="所属机构"
                        width="180">
                    </el-table-column>
                    <el-table-column
                        prop="description"
                        label="备注"
                        width="180">
                    </el-table-column>
                    <el-table-column
                        prop="createTime"
                        label="创建时间"
                        width="180">
                    </el-table-column>
                </el-table>
             </div>
        </div>
        <set-front v-if='setFrontShow' :show='setFrontShow' @close='setFrontClose'></set-front>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import setFront from "./dialog/setFront"
import { getFronts,getGroups } from "@/api/api"
import router from "@/router"
import errCode from "@/util/errCode"

export default {
    name: "front",
    components: {
        "v-content-head": contentHead,
        "set-front": setFront
    },
    data() {
        return {
            frontData: [],
            loading: false,
            setFrontShow: false,
            groupList: [],
            groupId: ""
        }
    },
    mounted: function(){
        this.getFrontList();
        this.getGroupList()
    },
    methods: {
        changGroup: function(){
            this.getFrontList();
            this.getGroupList()
        },
        search: function(){
            this.getFrontList()
        },
        createFront: function(){
            this.setFrontShow = true;
        },
        setFrontClose: function(){
            this.setFrontShow = false;
            this.getFrontList()

        },
        getFrontList: function(){
            let data = {
                chainId: localStorage.getItem('chainId'),
                groupId: this.groupId
            }
            getFronts(data).then(res => {
                if(res.data.code === 0){
                    this.frontData = res.data.data
                }else {
                    this.$message({
                        type: "error",
                        message: errCode.errCode[res.data.code].zh
                    })
                }
            }).catch(err => {
                this.$message({
                    type: "error",
                    message: "系统错误"
                })
            })
        },
        routeDetail: function(row){
            router.push({
                path: "/hostDetail",
                query: {
                    frontId: row.frontId,
                    nodeIp: row.frontIp,
                    groupId: this.groupId
                }
            })
        },
        getGroupList: function(){
            getGroups(localStorage.getItem('chainId')).then(res => {
                if(res.data.code === 0){
                    this.groupList = res.data.data;
                    if(res.data.data.length){
                        this.groupId = res.data.data[0].groupId
                    }
                }else {
                    this.$message({
                        type: "error",
                        message: errCode.errCode[res.data.code].zh
                    })
                }
            }).catch(err => {
                this.$message({
                    type: "error",
                    message: "系统错误"
                })
            })
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
    }
}
</script>

<style scoped>
.search-part-left {
    float: left;
}
.search-part-right{
    float: right
}
</style>