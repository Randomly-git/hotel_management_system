<template>
  <div style="padding: 20px;">
    <h1>测试页面</h1>
    <p>如果你能看到这个页面，说明Vue Router工作正常</p>
    <el-button type="primary">Element Plus按钮测试</el-button>
    <el-icon :size="32"><House /></el-icon>
  </div>
</template>

<script setup lang="ts">
import { House } from '@element-plus/icons-vue'
</script>
