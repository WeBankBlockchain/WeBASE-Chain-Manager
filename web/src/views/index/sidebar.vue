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
    <div style="height: 100%;">
        <div style="height: 100%;background-color: #0c1220" class="sidebar-content">
            <div class="image-flex justify-center center" style="height: 54px;position:relative;" >
                <img :src="maxLog" alt="" style="width:120px" :class="{'logClass':menuShow}">
                <span class="sidebar-contract-icon">
                    <i class="el-icon-caret-left font-color-aeb1b5" @click="hideMune(true)" style="font-size: 18px;"></i>
                </span>
            </div>
            <div class="mini-sidebar-contract-icon" v-if="!menuShowC" style="padding-bottom:40px">
                <i class="el-icon-caret-right font-color-aeb1b5" @click="hideMune(false)" style="font-size: 18px;"></i>
            </div>
            <el-menu default-active="999" router class="el-menu-vertical-demo" text-color="#9da2ab" active-text-color="#37eef2" active-background-color="#1e293e" background-color="#0c1220" @select="select"  @open="handleOpen" @close="handleClose">
                <el-menu-item index="/chain">
                    <template slot="title">
                    <i class="el-icon-location"></i>
                    <span>区块链管理</span>
                    </template>
                </el-menu-item>
                <el-menu-item index="/front">
                    <template slot="title">
                    <i class="el-icon-location"></i>
                    <span>前置管理</span>
                    </template>
                </el-menu-item>
                <el-menu-item index="/group">
                    <template slot="title">
                    <i class="el-icon-location"></i>
                    <span>群组管理</span>
                    </template>
                </el-menu-item>
                 <el-menu-item index="/list">
                    <template slot="title">
                    <i class="el-icon-location"></i>
                    <span>合约列表</span>
                    </template>
                </el-menu-item>
                
            </el-menu>
        </div>
    </div>
</template>

<script>
import maxLog from "@/../static/image/logo-2 copy@1.5x.jpg";
import router from "@/router";
export default {
    name: "sidebar",
    props: ["minMenu"],
    data() {
        return {
            maxLog: maxLog,
            activeIndex: 0,
            activeRoute: "",
            userRole: localStorage.getItem("root"),
            routesList: [],
            menuShow:false
        };
    },
    computed: {
        menuShowC() {
            if (this.minMenu) {
                return this.minMenu;
            } else {
                return false;
            }
        }
    },
    // mounted: function() {
    //     this.$nextTick(function() {
    //         localStorage.setItem("sidebarHide", false);
    //         this.changeRouter();
    //     });
    // },
    methods: {
        select: function(index, indexPath) {
            this.activeIndex = indexPath[0];
            this.activeRoute = index;
        },
        handleOpen(key, keyPath) {
        },
        handleClose(key, keyPath) {
        },
        hideMune: function(val) {
            this.$emit("sidebarChange", val);
            if (this.menuShow) {
                this.menuShow = false;
            } else {
                this.menuShow = true;
            }
            if (this.$route.path === "contract" && val) {
                localStorage.setItem("sidebarHide", true);
            } else {
                localStorage.setItem("sidebarHide", false);
            }
        },
        getAdmin: function() {}
    }
};
</script>

<style scoped>
.el-menu-vertical-demo {
    padding-top: 31px;
    border: none;
}
.el-menu-vertical-demo>>>.el-menu-item {
    font-size: 14px;
    color: #9da2ab;
    text-align: left;
}
.el-menu-vertical-demo>>>.el-submenu__title {
    padding-left: 33px;
}
.el-menu-item-group > ul > .el-menu-item {
    font-size: 14px;
    color: #9da2ab;
    text-align: left;
    padding-left: 57px !important;
    height: 46px;
    line-height: 46px;
}
.sidebar-content>>>.el-menu--collapse {
    width: 56px;
}
.sidebar-content>>>.el-menu--collapse .is-active .el-tooltip {
    padding-left: 17px !important;
    background-color: #1e293e;
}
.mini-sidebar-contract-icon {
    position: relative;
    text-align: center;
}
.mini-sidebar-contract-icon i {
    position: absolute;
    top: 20px;
    right: 10px;
    z-index: 9999;
    cursor: pointer;
}

.image-flex {
    display: -webkit-box !important;
    display: -webkit-flex !important;
    display: -ms-flexbox !important;
    display: flex !important;
    -webkit-flex-wrap: wrap;
    -ms-flex-wrap: wrap;
    flex-wrap: wrap;
}
.image-flex.justify-center {
    -webkit-box-pack: center;
    -webkit-justify-content: center;
    -ms-flex-pack: center;
    justify-content: center;
}
.image-flex.center {
    -webkit-box-pack: center;
    -webkit-justify-content: center;
    -ms-flex-pack: center;
    justify-content: center;
    -webkit-box-align: center;
    -webkit-align-items: center;
    -ms-flex-align: center;
    align-items: center;
}
.sidebar-icon {
    font-size: 15px;
    padding-right: 5px;
}
.sidebar-contract-icon {
    position: absolute;
    display: inline-block;
    left: 180px;
    top: 18px;
    font-size: 12px;
    letter-spacing: 0;
    text-align: right;
    cursor: pointer;
    z-index: 6666;
}
.sidebar-contract-icon i {
    cursor: pointer;
}
.logClass{
    width: 50px !important;
}
</style>
