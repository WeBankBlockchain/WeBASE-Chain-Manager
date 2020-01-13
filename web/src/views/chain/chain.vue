<template>
    <div>
        <v-content-head :headTitle="'区块链管理'"></v-content-head>
        <div class="module-wrapper">
            <div class="search-part" style="padding-top: 20px;">
                <div class="search-part-left">
                    <el-button type="primary" class="search-part-left-btn" @click="createGroup">新增区块链</el-button>
                </div>
            </div>
            <div class="search-table">
                <el-table :data="chainData" class="search-table-content" v-loading="loading">
                    <el-table-column
                        prop="chainName"
                        label="区块链名称"
                        >
                    </el-table-column>
                    <el-table-column
                        prop="chainId"
                        label="区块链编号"
                        >
                    </el-table-column>
                    <el-table-column
                        prop="chainType"
                        label="区块链类型"
                        >
                        <template slot-scope="scope">
                            <span>{{scope.row.chainType | Type}}</span>
                        </template>
                    </el-table-column>
                    <el-table-column
                        prop="createTime"
                        label="创建时间"
                        >
                    </el-table-column>
                    <el-table-column
                        prop="description"
                        label="备注"
                        >
                    </el-table-column>
                    <el-table-column
                        fixed="right"
                        label="操作"
                        width="100">
                        <template slot-scope="scope">
                            <el-button @click="handleClick(scope.row)" type="text" size="small">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </div>
        <add-chain v-if='addChainShow' :show='addChainShow' @close='addChainClose'></add-chain>
    </div>
</template>

<script>
import contentHead from "@/components/contentHead";
import { getChains,deleteChain } from "@/api/api"
import addChain from "./dialog/addChain"
import errCode from "@/util/errCode"
import Bus from "@/bus"
export default {
    name: "chain",
    components: {
        "v-content-head": contentHead,
        "add-chain": addChain
    },
    data() {
        return {
            chainData: [],
            addChainShow: false,
            loading: false,
        }
    },
    mounted: function(){
        this.getChainList()
    },
    methods: {
        createGroup: function(){
            this.addChainShow = true;
        },
        getChainList: function(){
            getChains().then(res => {
                if(res.data.code == 0){
                    this.chainData = res.data.data;
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
        addChainClose: function(){
            Bus.$emit("delete")
            this.addChainShow = false;
            this.getChainList()
        },
        handleClick: function(val){
            this.$confirm('此操作将删除该链, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
                }).then(() => {
                    this.deleteData(val)
                }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });          
            });
        },
        deleteData: function(val){
            deleteChain(val.chainId).then(res => {
                if(res.data.code === 0){
                    this.$message({
                        type: 'success',
                        message: '删除成功'
                    }); 
                    Bus.$emit("delete")
                    this.getChainList()
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
        }
    },
    filters: {
        Type: function(val){
            if(val){
                return "sm2/sm3"
            }else{
                return "secp256k1/sha3"
            }
        }
    }
}
</script>

<style>

</style>