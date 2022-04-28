// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import axios from 'axios'
import ElementUI from 'element-ui';
import VueClipboard from 'vue-clipboard2'
import 'element-ui/lib/theme-chalk/index.css';
import '@/assets/css/common.css'
import filters from './util/filter.js'


Vue.config.productionTip = false
Vue.prototype.$axios = axios;
Vue.use(ElementUI);
Vue.use(VueClipboard);

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})
