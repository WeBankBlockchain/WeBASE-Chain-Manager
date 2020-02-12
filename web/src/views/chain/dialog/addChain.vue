<template>
    <div>
        <el-dialog title="新增区块链" :visible.sync="dialogVisible" :before-close="modelClose" class="dialog-wrapper" width="400px" :center="true" :show-close='true'>
            <div>
                <el-form :model="chainFrom" :rules="rules" ref="chainFrom" label-width="100px" class="demo-ruleForm">
                    <el-form-item label="区块链名称" prop="chainName" style="width:330px">
                        <el-input v-model="chainFrom.chainName"></el-input>
                    </el-form-item>
                    <el-form-item label="区块链编号" prop="chainId" style="width:330px">
                        <el-input v-model="chainFrom.chainId"></el-input>
                    </el-form-item>
                    <el-form-item label="区块链类型" prop="type" style="width:330px">
                        <el-select v-model="chainFrom.type" placeholder="请选择">
                            <el-option
                            v-for="item in options"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="区块链备注"  style="width:330px">
                        <el-input v-model="chainFrom.description"></el-input>
                    </el-form-item>
                </el-form>
            </div>
            <div slot="footer" class="dialog-footer">
                <el-button  @click="modelClose">取 消</el-button>
                <el-button type="primary" :loading="loading" @click="submit('chainFrom')">确 定</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import {addChain} from "@/api/api"
import errCode from "@/util/errCode"
export default {
    name: "addChain",
    props: ['show'],
    data() {
        return {
            dialogVisible: this.show,
            chainFrom: {
                chainName: "",
                chainId: "",
                type: 0,
                description: ""
            },
            loading: false,
            rules: {
                chainName: [
                    {
                        required: true,
                        message: "请输入区块链名称",
                        trigger: "blur"
                    }
                ],
                chainId: [
                    {
                        required: true,
                        message: "请输入区块链编号",
                        trigger: "blur"
                    },
                    {
                        pattren: /^[0-9]*$/,
                        message: "区块链编号仅允许数字和英文",
                        trigger: "blur"
                    }
                ],
                type: [
                    {
                        required: true,
                        message: "请选择区块链类型",
                        trigger: "blur"
                    }
                ]
            },
            options: [
                {
                    label: "非国密",
                    value: 0
                },
                 {
                    label: "国密",
                    value: 1,
                }
            ]
        }
    },
    mounted: function(){
        this.chainFrom.type = this.options[0].value
    },
    methods: {
        submit: function (formName) {
            this.$refs[formName].validate(valid => {
                if (valid) {
                    this.loading = true;
                    this.newChain()
                } else {
                    return false
                }
            })
        },
        newChain: function(){
            let data = {
                chainName: this.chainFrom.chainName,
                chainType: this.chainFrom.type,
                description: this.chainFrom.description,
                chainId: this.chainFrom.chainId,
            }
            addChain(data).then(res => {
                this.loading = false
                if(res.data.code === 0){
                    this.modelClose();
                }else {
                    this.$message({
                        type: "error",
                        message: errCode.errCode[res.data.code].zh
                    })
                }
            }).catch(err => {
                this.loading = false
                this.$message({
                    type: "error",
                    message: "系统错误"
                })
            })
        },
        modelClose: function(){
            this.$emit('close')
        }
    }
}
</script>

<style>

</style>