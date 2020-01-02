<template>
    <div id="app" class="web-font">
        <router-view></router-view>
    </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
#app {
    /* min-width: 800px; */
    height: 100%;
    margin: 0;
    padding: 0;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    color: #2E384D;
}
ul,
li {
    list-style: none;
}
</style>
