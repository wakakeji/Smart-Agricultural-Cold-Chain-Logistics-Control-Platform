<template>
  <div class="login-page">
    <div class="panel">
      <div class="brand-side">
        <h1>智慧农业冷链物流管控平台</h1>
        <p>数字孪生 · 环境感知 · 质量追溯 · 智能调度</p>
      </div>
      <div class="form-side">
        <h2>登录</h2>
        <p class="hint">使用账号密码登录，系统按账号角色自动进入对应菜单</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleLogin">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="用户名/手机号" clearable />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="form.rememberMe">记住登录（7天）</el-checkbox>
          </el-form-item>
          <div class="actions">
            <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
            <el-button plain @click="handleReset">重置</el-button>
          </div>
        </el-form>
        <div class="demo">
          <p>演示密码均为：Abc@123456</p>
          <p>admin / farmer01 / logistics01 / driver01 / wholesaler01 / retailer01 / consumer01</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { homeForRole } from '@/config/roleMenus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: 'Abc@123456',
  rememberMe: true,
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
}

function handleReset() {
  form.username = ''
  form.password = ''
  form.rememberMe = false
}

async function handleLogin() {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) return
  loading.value = true
  try {
    const data = await userStore.login({
      username: form.username,
      password: form.password,
      rememberMe: form.rememberMe,
    })
    ElMessage.success('登录成功')
    const role = data.userInfo?.role || userStore.role
    const redirect = (route.query.redirect as string) || homeForRole(role)
    router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(circle at 20% 20%, rgba(64, 158, 255, .25), transparent 40%),
    radial-gradient(circle at 80% 0%, rgba(103, 194, 58, .2), transparent 35%),
    linear-gradient(135deg, #0f172a, #1e293b 50%, #0b3b2e);
}
.panel {
  width: min(920px, 100%);
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, .25);
}
.brand-side {
  padding: 48px 36px;
  color: #fff;
  background: linear-gradient(160deg, #2563eb, #0f766e);
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.brand-side h1 { font-size: 28px; line-height: 1.3; margin: 0 0 12px; }
.brand-side p { margin: 0; opacity: .9; }
.form-side { padding: 32px 28px; }
.form-side h2 { margin: 0 0 8px; }
.hint { color: #909399; font-size: 13px; margin: 0 0 16px; }
.actions { display: flex; gap: 12px; }
.demo { margin-top: 16px; color: #909399; font-size: 12px; line-height: 1.6; }
.demo p { margin: 0; }
@media (max-width: 768px) {
  .panel { grid-template-columns: 1fr; }
  .brand-side { padding: 28px 24px; }
}
</style>
