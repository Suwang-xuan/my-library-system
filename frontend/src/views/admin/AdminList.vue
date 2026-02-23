<template>
  <div class="admin-list">
    <div class="page-header">
      <h2 class="page-title">管理员管理</h2>
    </div>

    <div class="filter-container">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加管理员
      </el-button>
    </div>

    <div class="table-container">
      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="adminId" label="管理员ID" width="100" />
        <el-table-column prop="adminAccount" label="管理员账号" width="150" />
        <el-table-column prop="adminName" label="管理员姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column prop="roleId" label="角色ID" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.roleId === 1 ? 'danger' : ''" size="small">
              {{ row.roleId === 1 ? '超级管理员' : '普通管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              v-if="row.adminId !== 1"
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑管理员' : '添加管理员'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="管理员账号" prop="adminAccount">
          <el-input v-model="formData.adminAccount" placeholder="请输入管理员账号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="管理员姓名" prop="adminName">
          <el-input v-model="formData.adminName" placeholder="请输入管理员姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="formData.roleId" placeholder="请选择角色" style="width: 100%;">
            <el-option label="超级管理员" :value="1" />
            <el-option label="普通管理员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="adminPassword" v-if="!isEdit">
          <el-input v-model="formData.adminPassword" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ submitting ? '保存中...' : '确 定' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, roleApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const roles = ref([])

const formData = reactive({
  adminId: null,
  adminAccount: '',
  adminName: '',
  phone: '',
  roleId: 2,
  adminPassword: ''
})

const formRules = {
  adminAccount: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' }
  ],
  adminName: [
    { required: true, message: '请输入管理员姓名', trigger: 'blur' }
  ],
  adminPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.getList()
    tableData.value = res.data
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  try {
    const res = await roleApi.getList()
    roles.value = res.data
  } catch (error) {
    console.error('加载角色失败:', error)
  }
}

const handleAdd = () => {
  isEdit.value = false
  formData.adminId = null
  formData.adminAccount = ''
  formData.adminName = ''
  formData.phone = ''
  formData.roleId = 2
  formData.adminPassword = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  formData.adminId = row.adminId
  formData.adminAccount = row.adminAccount
  formData.adminName = row.adminName
  formData.phone = row.phone || ''
  formData.roleId = row.roleId || 2
  formData.adminPassword = ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        await adminApi.update(formData)
        ElMessage.success('更新成功')
      } else {
        await adminApi.add(formData)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确定要${statusText}管理员"${row.adminName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.updateStatus(row.adminId, newStatus)
    ElMessage.success(`${statusText}成功`)
    loadData()
  } catch (e) {}
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除管理员"${row.adminName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.delete(row.adminId)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

onMounted(() => {
  loadData()
  loadRoles()
})
</script>
