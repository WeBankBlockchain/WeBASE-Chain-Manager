import Vue from 'vue'
import Router from 'vue-router'
import HelloWorld from '@/components/HelloWorld'
const main = resolve => require(['@/views/index/main'], resolve);
const group = resolve => require(['@/views/group'], resolve);
const front = resolve => require(['@/views/front'], resolve);
const node = resolve => require(['@/views/group/compontents/node'], resolve);
const chain = resolve => require(['@/views/chain/chain'], resolve);
const hostDetail = resolve => require(['@/views/front/components/hostDetail'], resolve);


Vue.use(Router)

const routes = [
    {
      path: '/',
      redirect: '/chain',
    },
  {
    path: '/main',
    name: 'main',
    redirect: '/chain',
    component: main,
    children: [
          {
             path: '/front', component: front, name: '前置管理',nameKey: "group", menuShow: true, meta: { requireAuth: true }
          },
        {
          path: '/group', component: group, name: '群组管理',nameKey: "group", menuShow: true, meta: { requireAuth: true }
        },
        {
          path: '/node', component: node, name: '节点列表',nameKey: "node", menuShow: true, meta: { requireAuth: true }
        },
        {
          path: '/chain', component: chain, name: '区块链管理',nameKey: "chain", menuShow: true, meta: { requireAuth: true }
        },
        {
          path: '/hostDetail', component: hostDetail, name: '前置详情',nameKey: "hostDetail", menuShow: true, meta: { requireAuth: true }
        }
    ]
}
]
const router = new Router({
  routes
});
router.onError((error) => {
  const pattern = /Loading chunk (\d)+ failed/g;
  const isChunkLoadFailed = error.message.match(pattern);
  const targetPath = router.history.pending.fullPath;
  if (isChunkLoadFailed) {
      router.go(0);
      router.replace(targetPath);
  }
});

export default router
