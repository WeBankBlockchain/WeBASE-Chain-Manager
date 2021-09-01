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
import url from './url'
import { post, get, patch, put, deleted } from './http'
import { reviseParam } from '@/util/util'
import qs from 'qs'


//get front list
export function getFronts(data) {
    return get({
        url: `${url.ORG_LIST}/front/find`,
        method: 'get',
        params: data
    })
}

//add front
export function addFront(data) {
    return post({
        url: `${url.ORG_LIST}/front/new`,
        method: 'post',
        data: data
    })
}

//delete front
export function delFront(data) {
    return deleted({
        url: `${url.ORG_LIST}/front/${data}`,
        method: 'delete',
    })
}

//mointorInfo
export function getMointorInfo(data,list) {
    return get({
        url: `${url.ORG_LIST}/front/mointorInfo/${data}`,
        method: 'get',
        params: list
    })
}

//前置节点服务器监控信息
export function getRatio(data,list) {
    return get({
        url: `${url.ORG_LIST}/front/ratio/${data}`,
        method: 'get',
        params: list
    })
}

//前置节点服务器配置信息
export function getFrontConfig(data) {
    return get({
        url: `${url.ORG_LIST}/front/config/${data}`,
        method: 'get',
    })
}

// add new group
export function addGroup(data) {
    return post({
        url: `${url.ORG_LIST}/group/generate`,
        method: 'post',
        data: data
    })
}

// start group
export function startGroup(data) {
    return get({
        url: `${url.ORG_LIST}/group/start`,
        method: 'get',
        params: data
    })
}

//batchStart gtoup
export function batchStartGroup(data) {
    return post({
        url: `${url.ORG_LIST}/group/batchStart`,
        method: 'post',
        data: data
    })
}

// update group
export function updateGroup() {
    return get({
        url: `${url.ORG_LIST}/group/update`,
        method: 'get',
    })
}

// get group detail
export function getGroupDetail(data) {
    return get({
        url: `${url.ORG_LIST}/group/${data}`,
        method: 'get'
    })
}

//get group list
export function getGroups(data) {
    return get({
        url: `${url.ORG_LIST}/group/all/${data}`,
        method: 'get',
    })
}

//get node list
export function getNodes(data,list) {
    const params = reviseParam(data, list);
    return get({
        url: `${url.ORG_LIST}/node/nodeList/${params.str}`,
        method: 'get',
        params: params.querys,
    })
}

//get node detail
export function getNodeDetail(data) {
    return get({
        url: `${url.ORG_LIST}/node/nodeInfo/${data}`,
        method: 'get'
    })
}

//add chain
export function addChain(data) {
    return post({
        url: `${url.ORG_LIST}/chain/new`,
        method: 'post',
        data: data
    })
}

//get chains
export function getChains() {
    return get({
        url: `${url.ORG_LIST}/chain/all`,
        method: 'get',
    })
}

//delete chains
export function deleteChain(data) {
    return deleted({
        url: `${url.ORG_LIST}/chain/${data}`,
        method: 'delete'
    })
}
//queryContractList
export function queryContractListUsingPOST (data) {
    return post({
        url: `${url.ORG_LIST}/contract/contractList`,
        method: 'post',
        data: data
    })
}