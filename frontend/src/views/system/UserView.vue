<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索用户名/姓名/手机" clearable style="width: 220px" @keyup.enter="load" />
        <el-select v-model="roleCode" clearable placeholder="角色" style="width: 140px" @change="load">
          <el-option v-for="r in roles" :key="r.roleCode" :label="r.roleName" :value="r.roleCode" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button type="success" @click="openCreate">新增用户</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column prop="roleCode" label="角色" min-width="110">
          <template #default="{ row }">{{ roleName(row.roleCode) }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机" min-width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'info'">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="load"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑用户' : '新增用户'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" :prop="editing ? undefined : 'password'">
          <el-input v-model="form.password" type="password" show-password :placeholder="editing ? '不填则不修改' : '必填'" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="form.roleCode" style="width: 100%">
            <el-option v-for="r in roles" :key="r.roleCode" :label="r.roleName" :value="r.roleCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createUser, fetchRoles, fetchUsers, updateUser, updateUserStatus } from '@/api/system'
import type { SysRole, SysUser } from '@/types/api'

const loading = ref(false)
const saving = ref(false)
const list = ref<SysUser[]>([])
const roles = ref<SysRole[]>([])
const keyword = ref('')
const roleCode = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editing = ref(false)
const editId = ref<number>()
const formRef = ref<FormInstance>()

const form = reactive({
  username: '',
  password: '',
  realName: '',
  roleCode: 'farmer',
  phone: '',
  email: '',
  status: 1,
})

const rules: FormRules = {
  username: [{ required: true, message: '必填', trigger: 'blur' }],
  password: [{ required: true, message: '必填', trigger: 'blur' }],
  realName: [{ required: true, message: '必填', trigger: 'blur' }],
  roleCode: [{ required: true, message: '必选', trigger: 'change' }],
}

function roleName(code: string) {
  return roles.value.find((r) => r.roleCode === code)?.roleName || code
}

function statusText(s: number) {
  return s === 1 ? '启用' : s === 2 ? '锁定' : '禁用'
}

async function load() {
  loading.value = true
  try {
    const data = await fetchUsers({ page: page.value, size: size.value, keyword: keyword.value || undefined, roleCode: roleCode.value || undefined })
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.username = ''
  form.password = ''
  form.realName = ''
  form.roleCode = 'farmer'
  form.phone = ''
  form.email = ''
  form.status = 1
}

function openCreate() {
  editing.value = false
  editId.value = undefined
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: SysUser) {
  editing.value = true
  editId.value = row.userId
  form.username = row.username
  form.password = ''
  form.realName = row.realName
  form.roleCode = row.roleCode
  form.phone = row.phone || ''
  form.email = row.email || ''
  form.status = row.status
  dialogVisible.value = true
}

async function save() {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok && !editing.value) return
  if (!editing.value) {
    const v = await formRef.value?.validate().catch(() => false)
    if (!v) return
  }
  saving.value = true
  try {
    const payload = { ...form }
    if (editing.value && editId.value) {
      await updateUser(editId.value, payload)
    } else {
      await createUser(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: SysUser) {
  const next = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(`确认${next === 1 ? '启用' : '禁用'}用户 ${row.username}？`, '提示')
  await updateUserStatus(row.userId, next)
  ElMessage.success('状态已更新')
  await load()
}

onMounted(async () => {
  roles.value = await fetchRoles()
  await load()
})
</script>

<style scoped>
.toolbar { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
