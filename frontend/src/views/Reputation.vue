<template>
  <div class="reputation">
    <div class="page-header">
      <h1>声誉管理</h1>
      <el-button type="primary">
        <el-icon><Plus /></el-icon>
        导入评价
      </el-button>
    </div>

    <el-card>
      <el-empty description="声誉管理功能开发中..." />
    </el-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.reputation h1 {
  margin-bottom: 20px;
  color: #303133;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>