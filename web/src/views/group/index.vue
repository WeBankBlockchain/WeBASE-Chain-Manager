<template>
    <div>
        <v-content-head :headTitle="'群组管理'" @changGroup='changGroup'></v-content-head>
        <div class="module-wrapper">
            <h3 style="padding: 20px 0 0 40px;">群组列表</h3>
            <!-- <div class="search-part" style="padding-top: 20px;">
                <div class="search-part-left">
                    <el-button type="primary" class="search-part-left-btn" @click="createGroup">新增群组</el-button>
                </div>
            </div> -->
            <div class="search-table">
                <el-table :data="groupData" class="search-table-content" v-loading="loading">
                    <el-table-column
                        prop="groupId"
                        label="群组编号"
                        >
                    </el-table-column>
                    <el-table-column
                        prop="groupName"
                        label="群组名称"
                        >
                        <template slot-scope="scope">
                            <span class="group-route" @click='route(scope.row)'>{{scope.row.groupName}}</span>
                        </template>
                    </el-table-column>
                    <el-table-column
                        prop="nodeCount"
                        label="节点数量"
                        >
                    </el-table-column>
                    <el-table-column
                        prop="createTime"
                        label="创建时间"
                        >
                         <template slot-scope="scope">
                            <span class="">{{scope.row.createTime|dateSet}}</span>
                        </template>
                    </el-table-column>
                    
                </el-table>
            </div>
        </div>
        <add-group v-if='addGroupShow' :show='addGroupShow' @close='addGroupClose'></add-group>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import addGroup from "./dialog/addGroup"
import { getGroups } from "@/api/api"
import router from "@/router"
import errCode from "@/util/errCode"
export default {
    name: "front",
    components: {
        "v-content-head": contentHead,
        "add-group": addGroup
    },
    data() {
        return {
            groupData: [],
            loading: false,
            addGroupShow: false
        }
    },
    mounted: function(){
        if(localStorage.getItem('chainId')){
            this.getGroupList()
        }
    },
    methods: {
        changGroup: function(){
            this.getGroupList()
        },
        createGroup: function(){
            this.addGroupShow = true
        },
        getGroupList: function(){
            getGroups(localStorage.getItem('chainId')).then(res => {
                if(res.data.code === 0){
                    this.groupData = res.data.data
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
        addGroupClose: function(){
            this.addGroupShow = false;
            if(localStorage.getItem('chainId')){
                this.getGroupList()
            }
        },
        route: function(row){
            router.push({
                path: "/node",
                query: {
                    groupName: row.groupName,
                    groupId: row.groupId
                }
            })
        }
    }
}
</script>

<style scoped>
    .group-route{
        color: #20D4D9;
        cursor: pointer;
    }
</style>