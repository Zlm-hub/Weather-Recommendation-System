<template>
  <div class="admin-container">
    <div class="admin-header">
      <div class="logo">
        <el-icon><Monitor /></el-icon>
        <span>系统控制台</span>
      </div>
      <div class="right-panel">
        <span class="welcome">管理员: {{ adminName }}</span>
        <el-button type="info" plain size="small" @click="$router.push('/home')">
          <el-icon><House /></el-icon> 返回前台
        </el-button>
        <el-button type="danger" plain size="small" @click="logout">退出</el-button>
      </div>
    </div>

    <div class="admin-content">
      <el-card shadow="never" class="table-card">
        <template #header>
          <div class="card-header">
            <span>👥 用户管理</span>
            <div class="search-box">
              <el-input
                  v-model="searchText"
                  placeholder="搜索用户名..."
                  prefix-icon="Search"
                  clearable
                  @clear="fetchUserList"
                  @keyup.enter="fetchUserList"
                  style="width: 200px; margin-right: 10px"
              />
              <el-button type="primary" @click="fetchUserList">搜索</el-button>
            </div>
          </div>
        </template>

        <el-table :data="userList" stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />

          <el-table-column label="头像" width="100">
            <template #default="scope">
              <el-avatar :size="40" :src="scope.row.avatar" />
            </template>
          </el-table-column>

          <el-table-column prop="username" label="用户名" width="150">
            <template #default="scope">
              <span style="font-weight: bold">{{ scope.row.username }}</span>
              <el-tag v-if="scope.row.role === 'admin'" type="danger" size="small" effect="dark" style="margin-left: 5px">管理员</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="location" label="城市" width="120">
            <template #default="scope">
              <el-tag type="success" size="small">{{ scope.row.location || '未知' }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="phone" label="手机号" width="150" />
          <el-table-column prop="createTime" label="注册时间" />

          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-popconfirm
                  title="确定要删除该用户吗？此操作不可恢复！"
                  confirm-button-text="删除"
                  cancel-button-text="取消"
                  @confirm="handleDelete(scope.row)"
              >
                <template #reference>
                  <el-button type="danger" link size="small" :disabled="scope.row.role === 'admin'">
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Monitor, House, Search } from '@element-plus/icons-vue';
import request from '../utils/request';

const router = useRouter();
const userList = ref([]);
const loading = ref(false);
const searchText = ref('');
const adminName = ref('Admin');

onMounted(() => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    const u = JSON.parse(userStr);
    adminName.value = u.username;
    // 安全检查：如果不是管理员，踢回首页
    if (u.role !== 'admin') {
      ElMessage.error('无权访问控制台');
      router.push('/home');
      return;
    }
  }
  fetchUserList();
});

const fetchUserList = async () => {
  loading.value = true;
  try {
    const res = await request.get('/user/list', {
      params: { username: searchText.value }
    });
    if (res.code === 200) {
      userList.value = res.data;
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleDelete = async (row) => {
  try {
    const res = await request.delete(`/user/${row.id}`);
    if (res.code === 200) {
      ElMessage.success('用户删除成功');
      fetchUserList(); // 刷新列表
    } else {
      ElMessage.error(res.msg);
    }
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

const logout = () => {
  localStorage.removeItem('user');
  localStorage.removeItem('token');
  router.push('/login');
};
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background-color: #f0f2f5;
}
.admin-header {
  height: 60px;
  background: #001529;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.logo {
  font-size: 20px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 10px;
}
.right-panel {
  display: flex;
  align-items: center;
  gap: 15px;
}
.welcome {
  font-size: 14px;
  color: #ccc;
}
.admin-content {
  padding: 20px;
}
.table-card {
  border-radius: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-box {
  display: flex;
}
</style>