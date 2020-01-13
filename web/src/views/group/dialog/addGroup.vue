<template>
    <div>
        <el-dialog title="新增群组" :visible.sync="dialogVisible" :before-close="modelClose" class="dialog-wrapper" width="65%" :center="true" :show-close='true'>
            <div>
                <el-form :model="groupFrom" :rules="rules" ref="groupFrom" label-width="100px" class="demo-ruleForm">
                    <el-form-item label="群组编号" prop="generateGroupId" style="width:330px">
                        <el-input v-model="groupFrom.generateGroupId"></el-input>
                    </el-form-item>
                    <el-form-item label="节点列表" >
                        <el-table
                            ref="multipleTable"
                            :data="nodeList"
                            tooltip-effect="dark"
                            style="width: 100%"
                            @selection-change="handleSelectionChange">
                            <el-table-column
                                type="selection"
                                width="55">
                            </el-table-column>
                            <el-table-column
                                label="前置编号"
                                prop="frontId"
                                width="120">
                            </el-table-column>
                            <el-table-column
                                prop="nodeId"
                                label="节点编号"
                                >
                            </el-table-column>
                            <el-table-column
                                prop="frontIp"
                                label="前置IP"
                                width="120">
                            </el-table-column>
                            <el-table-column
                                prop="frontPort"
                                label="前置端口"
                                width="120">
                            </el-table-column>
                            <el-table-column
                                prop="agency"
                                label="所属机构"
                                width="180">
                            </el-table-column>
                        </el-table>
                    </el-form-item>
                    <el-form-item label="备注" style="width:330px">
                        <el-input v-model="groupFrom.description"></el-input>
                    </el-form-item>
                </el-form>
            </div>
            <div slot="footer" class="dialog-footer">
                <el-button  @click="modelClose">取 消</el-button>
                <el-button type="primary" :loading="loading" @click="submit('groupFrom')">确 定</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import { getFronts,addGroup,batchStartGroup,updateGroup } from "@/api/api"
import errCode from "@/util/errCode"
export default {
    name: "addGroup",
    props: ['show'],
    data() {
        return {
            dialogVisible: this.show,
            nodeList: [],
            loading: false,
            groupId: null,
            groupFrom: {
                generateGroupId: "",
                description: "",
                nodeList: [],
                timestamp: 0
            },
            rules: {
                generateGroupId: [
                    {
                        required: true,
                        message: "请输入群组编号",
                        trigger: "blur"
                    }
                ]
            }
        }
    },
    mounted: function(){
        this.getFrontList()
    },
    methods: {
        modelClose: function(){
            this.$emit("close")
        },
        getFrontList: function(){
            let data = {
                chainId: localStorage.getItem('chainId')
            }
            getFronts(data).then(res => {
                if(res.data.code === 0){
                    this.nodeList = res.data.data
                }
            })
        },
        handleSelectionChange: function(val){
            this.groupFrom.nodeList = [];
            if(val && val.length){
                for(let i = 0; i < val.length; i++){
                    this.groupFrom.nodeList.push(val[i].nodeId)
                }
            }
        },
        submit: function (formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    this.loading = true;
                    this.newGroup()
                } else {
                    return false
                }
            })
        },
        newGroup: function(){
            let data = {
                chainId: localStorage.getItem('chainId'),
                generateGroupId: this.groupFrom.generateGroupId,
                timestamp: (new Date()).getTime(),
                nodeList: this.groupFrom.nodeList,
                description: this.groupFrom.description
            }
            addGroup(data).then(res => {
                if(res.data.code === 0){
                    this.groupId = res.data.data.groupId
                    this.startAllGroup(this.groupId,this.groupFrom.nodeList)
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
        startAllGroup: function(id,list){
            let data = {
                chainId: localStorage.getItem('chainId'),
                generateGroupId: id,
                nodeList: list
            }
            batchStartGroup(data).then(res => {
                if(res.data.code === 0){
                    this.update()
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
        update: function(){
            updateGroup().then(res => {
                if(res.data.code === 0){
                    this.$message({
                        type: "success",
                        message: "群组创建成功！"
                    })
                    this.loading = false;
                    this.modelClose();
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
    }
}
</script>

<style>

</style>