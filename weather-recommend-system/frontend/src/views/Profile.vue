<template>
  <div class="profile-container">
    <div class="content-wrapper">

      <div class="page-header">
        <el-button link class="back-link" @click="$router.push('/home')">
          <el-icon><ArrowLeft /></el-icon> 返回首页
        </el-button>
      </div>

      <el-row :gutter="24">

        <el-col :span="8">
          <el-card class="box-card left-card" shadow="never">
            <div class="user-info-header">
              <div class="avatar-container">
                <el-avatar :size="100" :src="userForm.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
              </div>
              <h2 class="username">{{ userForm.username || '用户' }}</h2>
              <div class="user-tags">
                <el-tag size="small" type="info" effect="plain">普通用户</el-tag>
                <el-tag size="small" type="success" effect="plain" style="margin-left: 5px;">在线</el-tag>
              </div>
              <p class="register-time">注册于 {{ userForm.createTime || '2026-01-01' }}</p>
            </div>
            <el-divider />
            <div class="info-list">
              <div class="info-item"><div class="label"><el-icon><Location /></el-icon> 城市</div><div class="val">{{ userForm.location || '未设置' }}</div></div>
              <div class="info-item"><div class="label"><el-icon><Message /></el-icon> 邮箱</div><div class="val">{{ userForm.email || '未绑定' }}</div></div>
              <div class="info-item"><div class="label"><el-icon><Iphone /></el-icon> 手机</div><div class="val">{{ userForm.phone || '未绑定' }}</div></div>
            </div>
            <div class="bio-box">“ {{ userForm.preference || '这个人很懒，什么都没有写...' }} ”</div>
          </el-card>
        </el-col>

        <el-col :span="16">
          <el-card class="box-card right-card" shadow="never">
            <template #header>
              <div class="card-header"><span>⚙️ 资料设置</span></div>
            </template>

            <el-form :model="userForm" label-position="top" class="settings-form">

              <div class="form-section-title">更换头像</div>
              <div class="upload-area">
                <el-upload
                    class="avatar-uploader-square"
                    action="http://localhost:8080/api/files/upload"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    :on-error="handleAvatarError"
                    :before-upload="beforeAvatarUpload"
                    name="file"
                >
                  <img v-if="tempAvatar" :src="tempAvatar" class="avatar" />
                  <div v-else class="upload-placeholder">
                    <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
                  </div>
                </el-upload>
                <div class="upload-tip">支持 JPG/PNG，小于 2MB。<br>上传后点击下方保存生效。</div>
              </div>

              <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="12">
                  <el-form-item label="所在城市">
                    <el-select
                        v-model="userForm.location"
                        placeholder="请选择或搜索城市"
                        style="width: 100%;"
                        filterable
                        clearable
                        no-match-text="无匹配城市"
                    >
                      <el-option
                          v-for="city in majorCities"
                          :key="city"
                          :label="city"
                          :value="city"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="旧密码 (修改密码时必填)">
                    <el-input v-model="userForm.oldPassword" type="password" placeholder="请输入旧密码以验证身份" show-password />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="新密码">
                <el-input v-model="userForm.password" type="password" placeholder="如果不修改密码，请留空" show-password />
              </el-form-item>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="手机号码">
                    <el-input v-model="userForm.phone" placeholder="请输入手机号" prefix-icon="Iphone" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="邮箱地址">
                    <el-input v-model="userForm.email" placeholder="请输入邮箱" prefix-icon="Message" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="个性签名">
                <el-input v-model="userForm.preference" type="textarea" :rows="3" placeholder="写点什么..." maxlength="100" show-word-limit />
              </el-form-item>

              <div class="form-footer">
                <el-button type="primary" size="large" class="save-btn" @click="saveProfile" :loading="loading">保存所有修改</el-button>
              </div>

            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Location, Message, Iphone, Plus } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const loading = ref(false)
const tempAvatar = ref('')

const userForm = ref({
  username: '', avatar: '', location: '', preference: '',
  email: '', phone: '', createTime: '', password: '', oldPassword: ''
})

// ✅✅✅ 新增：主要城市列表数据
const majorCities = [
  "北京", "上海", "广州", "深圳", "成都", "杭州", "武汉", "西安", "重庆", "天津",
  "南京", "苏州", "长沙", "郑州", "东莞", "青岛", "沈阳", "宁波", "昆明", "无锡",
  "佛山", "合肥", "大连", "福州", "厦门", "哈尔滨", "济南", "温州", "南宁", "长春",
  "泉州", "石家庄", "贵阳", "南昌", "金华", "常州", "珠海", "惠州", "嘉兴", "南通",
  "中山", "太原", "兰州", "乌鲁木齐", "徐州", "绍兴", "台州", "烟台", "海口", "三亚",
  "保定", "扬州", "呼和浩特", "银川", "西宁", "拉萨", "香港", "澳门", "台北", "高雄",
  "洛阳", "桂林", "张家界", "丽江", "大理", "三门峡", "咸阳", "宝鸡", "汉中", "延安"
];

// 计算 Headers
const uploadHeaders = computed(() => {
  let token = localStorage.getItem('token');
  if (!token) {
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      if (user && user.token) token = user.token;
    } catch (e) {}
  }
  if (token) token = token.replace(/^"|"$/g, '');
  return { 'token': token }
})

onMounted(() => { loadUserInfo() })

const loadUserInfo = async () => {
  const res = await request.get('/user/info')
  if (res.code === 200) {
    userForm.value = res.data
    userForm.value.password = ''
    userForm.value.oldPassword = ''
    tempAvatar.value = res.data.avatar
  }
}

const handleAvatarSuccess = (res) => {
  let url = '';
  if (typeof res === 'string') url = res;
  else if (res.code === 200) url = res.data;
  else if (res.url) url = res.url;

  if (url) {
    tempAvatar.value = url;
    userForm.value.avatar = url;
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error('上传失败：无法解析图片地址');
  }
}

const handleAvatarError = (err) => {
  ElMessage.error('上传失败，请检查网络或后端地址');
}

const beforeAvatarUpload = (file) => {
  const isImg = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isImg) ElMessage.error('头像只能是 JPG/PNG 格式!');
  if (!isLt2M) ElMessage.error('头像大小不能超过 2MB!');
  return isImg && isLt2M;
}

const saveProfile = async () => {
  if (userForm.value.password && !userForm.value.oldPassword) {
    ElMessage.warning('修改密码时必须输入旧密码');
    return;
  }
  loading.value = true;
  const postData = { ...userForm.value, password: userForm.value.password || undefined, oldPassword: userForm.value.oldPassword || undefined };
  if(!postData.phone) postData.phone = '';
  if(!postData.email) postData.email = '';

  try {
    const res = await request.post('/user/update', postData)
    if (res.code === 200) {
      ElMessage.success('保存成功');
      const localUser = JSON.parse(localStorage.getItem('user') || '{}');
      localUser.avatar = postData.avatar;
      localUser.location = postData.location;
      localStorage.setItem('user', JSON.stringify(localUser));
      loadUserInfo();
    } else {
      ElMessage.error(res.msg || '保存失败');
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.profile-container { min-height: 100vh; background-color: #f5f7fa; padding: 20px; }
.content-wrapper { max-width: 1200px; margin: 0 auto; }
.page-header { margin-bottom: 20px; }
.back-link { font-size: 14px; color: #606266; }
.back-link:hover { color: #409EFF; }
.box-card { border-radius: 8px; border: 1px solid #ebeef5; background: #fff; }
.left-card { text-align: center; }
.user-info-header { padding: 20px 0; }
.username { margin: 15px 0 10px; font-size: 22px; color: #303133; }
.register-time { margin-top: 10px; font-size: 12px; color: #909399; }
.info-list { padding: 10px 20px; text-align: left; }
.info-item { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; font-size: 14px; color: #606266; }
.info-item .label { display: flex; align-items: center; gap: 8px; color: #909399; }
.bio-box { background: #f8f9fa; padding: 15px; border-radius: 6px; margin: 20px; font-size: 13px; color: #666; font-style: italic; line-height: 1.5; }
.card-header { font-size: 16px; font-weight: 600; color: #303133; display: flex; align-items: center; gap: 5px; }
.card-header::before { content: ''; width: 4px; height: 16px; background: #409EFF; border-radius: 2px; }
.settings-form { padding: 10px 0; }
.form-section-title { font-size: 14px; color: #606266; margin-bottom: 15px; }
.upload-area { display: flex; gap: 20px; align-items: center; margin-bottom: 30px; }
.avatar-uploader-square .el-upload { border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; width: 100px; height: 100px; display: flex; justify-content: center; align-items: center; transition: .2s; }
.avatar-uploader-square .el-upload:hover { border-color: #409EFF; }
.upload-placeholder { width: 100%; height: 100%; display: flex; justify-content: center; align-items: center; background: #f9f9f9; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; }
.avatar { width: 100px; height: 100px; display: block; object-fit: cover; }
.upload-tip { font-size: 12px; color: #999; line-height: 1.6; }
.save-btn { width: 100%; background-color: #409EFF; font-weight: 500; letter-spacing: 1px; }
</style>