/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
<template>
    <div class="content-head-wrapper">
        <div class="content-head-title">
            <span class="content-head-icon" v-if='icon'  @click="skip">
                <i class="el-icon-arrow-left"></i>
            </span>
            <span :class="{ 'font-color-9da2ab': headSubTitle}">{{title}}</span>
            <span v-show="headSubTitle" class="font-color-9da2ab">/</span>
            <span>{{headSubTitle}}</span>
            <el-tooltip effect="dark" :content="headTooltip" placement="bottom-start" v-if="headTooltip">
                <i class="el-icon-info contract-icon font-15" ></i>
            </el-tooltip>
            <a v-if="headHref" target="_blank" :href="headHref.href" class="font-color-fff font-12">{{headHref.content}}</a>
        </div>
        <div class="content-head-network" style="padding-right: 40px;">
            <el-popover placement="bottom" width="120" min-width="50px" trigger="click">
                <ul class="group-item">
                    <li class="group-item-list" v-for='item in chainList' :key='item.chainId' @click='changeGroup(item)'>{{item.chainName}}</li>
                </ul>
                <span slot="reference" class="contant-head-name" style="color: #fff" @click='checkGroup'>区块链: {{chainName || '-'}}</span>
            </el-popover>

            <!-- <span @click="checkNetwork" class="select-network">切换区块链</span> -->
        </div>
    </div>
</template>

<script>
import dialog from "./groupdialog";
import router from "@/router";
import { getChains } from "@/api/api";
// import { delCookie } from '@/util/util'
import Bus from "@/bus"
export default {
    name: "conetnt-head",
    props: {
        headTitle: {
            type: String
        },
        icon: {
            type: Boolean
        },
        route: {
            type: String
        },
        headSubTitle: {
            type: String
        },
        headTooltip: {
            type: String
        },
        headHref: {
            type: Object
        }
    },
    components: {
        "v-dialog": dialog
    },
    watch: {
        headTitle: function (val) {
            this.title = val;
        }
    },
    data: function () {
        return {
            title: this.headTitle,
            chainName: "-",
            accountName: "-",
            dialogShow: false,
            path: "",
            headIcon: this.icon || false,
            way: this.route || "",
            changePasswordDialogVisible: false,
            groupList: [],
            chainList: []
        };
    },
    beforeDestroy: function () {
        Bus.$off("delete")
    },
    mounted: function () {
        if (localStorage.getItem("chainName")) {
            this.chainName = localStorage.getItem("chainName");
        }
        this.getChainList();
        Bus.$on('delete',data => {
            this.getChainList()
        })
    },
    methods: {
        getChainList: function(){
            getChains().then(res => {
                if(res.data.code === 0){
                    this.chainList = res.data.data
                    if(!localStorage.getItem('chainId')){
                        if(res.data.data.length){
                            localStorage.setItem("chainId",res.data.data[0].chainId);
                            localStorage.setItem("chainName",res.data.data[0].chainName);
                            this.chainName = localStorage.getItem('chainName')
                        }else{
                            localStorage.setItem("chainId","");
                            localStorage.setItem("chainName","");
                            this.chainName = ""
                        }
                    }else{
                        let num = 0
                        for(let i = 0; i < this.chainList.length; i++){
                            if(this.chainList[i].chainId == localStorage.getItem('chainId')){
                                num++
                            }
                        }
                        if(num == 0){
                            if(this.chainList && this.chainList.length){
                                localStorage.setItem("chainId",this.chainList[0].chainId);
                                localStorage.setItem("chainName",this.chainList[0].chainName);
                                this.chainName = localStorage.getItem('chainName')
                            }else{
                                localStorage.setItem("chainId","");
                                localStorage.setItem("chainName","");
                                this.chainName = ""
                            }
                            
                        }
                    }
                }
            }).catch(err => {
                this.$message({
                    type: "error",
                    message: "系统错误"
                })
            })
        },
        checkGroup: function () {
            if (this.dialogShow) {
                this.dialogShow = false;
            } else {
                this.dialogShow = true;
            }

            this.path = this.$route.path;
        },
        checkNetwork: function(){

        },
        changeGroup: function (val) {
            this.chainName = val.chainName
            localStorage.setItem("chainName", val.chainName);
            localStorage.setItem("chainId", val.chainId);
            this.$emit('changGroup', val.chainId);
            this.dialogShow = true;
        },
        changeNetwork: function() {
            this.chainName = localStorage.getItem("chainName");
            this.dialogShow = false;
        },
        skip: function () {
            if (this.route) {
                this.$router.push(this.way);
            } else {
                this.$router.go(-1);
            }
        },
        changePassword: function () {
            this.changePasswordDialogVisible = true;
        },
        success: function (val) {
            this.changePasswordDialogVisible = false;
        }
    }
};
</script>
<style scoped>
.content-head-wrapper {
    width: calc(100%);
    background-color: #181f2e;
    text-align: left;
    line-height: 54px;
    position: relative;
}
.content-head-wrapper::after {
    display: block;
    content: "";
    clear: both;
}
.content-head-icon {
    color: #fff;
    font-weight: bold;
    cursor: pointer;
}
.content-head-title {
    margin-left: 40px;
    display: inline-block;
    font-size: 16px;
    color: #fff;
    font-weight: bold;
}
.content-head-network {
    float: right;
    padding-right: 10px;
    position: relative;
}
.browse-user {
    text-align: center;
    text-decoration: none;
    font-size: 12px;
    cursor: pointer;
    color: #cfd7db;
}
.sign-out-wrapper {
    line-height: 32px;
    text-align: center;
}
.sign-out {
    cursor: pointer;
    color: #ed5454;
}
.change-password {
    color: #0db1c1;
    cursor: pointer;
}
.network-name {
    font-size: 12px;
    color: #9da2ab;
    padding: 3px 0px;
    /* border-right: 2px solid #e7ebf0; */
    margin-right: 16px;
}
.select-network {
    color: #2d5f9e;
    cursor: default;
}
.content-head-network a:nth-child(1) {
    text-decoration: none;
    outline: none;
    color: #cfd7db;
    padding-right: 15px;
    border-right: 1px solid #657d95;
    margin-right: 15px;
}
.contant-head-name {
    position: relative;
    cursor: pointer;
}
.contant-head-name ul {
    position: absolute;
    width: 150%;
    left: -10px;
    top: 35px;
    background-color: #fff;
    color: #666;
    text-align: center;
    z-index: 9999999;
    box-shadow: 1px 4px 4px;
}
.contant-head-name ul li {
    width: 100%;
    padding: 0 10px;
    height: 32px;
    line-height: 32px;
    cursor: pointer;
}
.group-item {
    line-height: 32px;
    text-align: center;
}
.group-item-list {
    cursor: pointer;
}
.group-item-list:hover {
    color: #0db1c1;
}
</style>
