<template>
  <div class="login-page">
    <div class="panel">
      <div class="brand-side">
        <h1>智慧农业冷链物流管控平台</h1>
        <p>数字孪生 · 环境感知 · 质量追溯 · 智能调度</p>
      </div>
      <div class="form-side">
        <h2>登录</h2>
        <el-tabs v-model="form.role" @tab-change="onRoleChange">
          <el-tab-pane v-for="r in roles" :key="r.code" :label="r.name" :name="r.code" />
        </el-tabs>
        <p class="hint">{{ currentHint }}</p>
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
        <p class="demo">演示密码：Abc@123456（如 admin / farmer01）</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const roles = [
  { code: 'admin', name: '管理员', hint: '系统配置、用户权限、审计', user: 'admin' },
  { code: 'farmer', name: '农户', hint: '录入产品、查看批次赋码', user: 'farmer01' },
  { code: 'logistics', name: '物流', hint: '运输调度、路线规划、在途监控', user: 'logistics01' },
  { code: 'driver', name: '司机', hint: '接单、上报状态、扫码确认', user: 'driver01' },
  { code: 'wholesaler', name: '批发商', hint: '入库验收、库存、质检', user: 'wholesaler01' },
  { code: 'retailer', name: '零售商', hint: '收货确认、追溯查询', user: 'retailer01' },
  { code: 'consumer', name: '消费者', hint: '扫码溯源、查看履历', user: 'consumer01' },
]

const form = reactive({
  username: 'admin',
  password: 'Abc@123456',
  role: 'admin',
  rememberMe: true,
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
}

const currentHint = computed(() => roles.find((r) => r.code === form.role)?.hint || '')

function onRoleChange(name: string | number) {
  const role = roles.find((r) => r.code === String(name))
  if (role) form.username = role.user
}

function handleReset() {
  form.password = ''
  form.rememberMe = false
}

async function handleLogin() {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) return
  loading.value = true
  try {
    await userStore.login({ ...form })
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || (form.role === 'admin' ? '/service-monitor' : '/map')
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
.hint { color: #909399; font-size: 13px; margin: 0 0 12px; min-height: 20px; }
.actions { display: flex; gap: 12px; }
.demo { margin-top: 16px; color: #909399; font-size: 12px; }
@media (max-width: 768px) {
  .panel { grid-template-columns: 1fr; }
  .brand-side { padding: 28px 24px; }
}
</style>
