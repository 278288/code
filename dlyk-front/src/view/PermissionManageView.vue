<template>
  <div class="container">
    <div class="header">
      <h3>权限管理</h3>
    </div>

    <div class="search-box">
      <el-input
        v-model="query.loginAct"
        placeholder="请输入账号"
        style="width: 200px; margin-right: 10px;"
        @keyup.enter="getData(1)"
      />
      <el-input
        v-model="query.name"
        placeholder="请输入姓名"
        style="width: 200px; margin-right: 10px;"
        @keyup.enter="getData(1)"
      />
      <el-button type="primary" @click="getData(1)">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <el-table
      :data="userList"
      style="width: 100%; margin-top: 20px;"
      v-loading="loading"
    >
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="loginAct" label="账号" width="120" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="phone" label="手机" width="150" />
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button
            type="primary"
            size="small"
            @click="openPermDialog(scope.row)"
          >
            分配权限
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      background
      layout="prev, pager, next"
      :page-size="pageSize"
      :total="total"
      @prev-click="page"
      @next-click="page"
      @current-change="page"
      style="margin-top: 20px;"
    />

    <!-- 权限分配对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="'为【' + currentUser.name + '】分配模块权限'"
      width="65%"
      destroy-on-close
    >
      <div style="max-height: 500px; overflow-y: auto;">
        <el-card v-for="module in modules" :key="module.id" style="margin-bottom: 12px;">
          <template #header>
            <el-checkbox
              :model-value="isModuleAllChecked(module)"
              :indeterminate="isModuleIndeterminate(module)"
              @change="toggleModule(module, $event)"
            >
              <strong>{{ module.name }}</strong>
            </el-checkbox>
          </template>
          <el-checkbox-group v-model="checkedPermIds">
            <el-checkbox
              v-for="perm in module.subPermissionList"
              :key="perm.id"
              :label="perm.id"
              :value="perm.id"
            >
              {{ perm.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-card>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermissions" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import { doGet, doPut } from '../http/httpRequest.js'
import { messageTip } from '../util/util.js'

export default defineComponent({
  name: 'PermissionManageView',

  data() {
    return {
      userList: [],
      pageSize: 0,
      total: 0,
      loading: false,
      query: { loginAct: '', name: '' },
      dialogVisible: false,
      currentUser: {},
      modules: [],
      checkedPermIds: [],
      saving: false
    }
  },

  mounted() {
    this.getData(1)
  },

  methods: {
    getData(current) {
      this.loading = true
      doGet('/api/users', {
        current,
        loginAct: this.query.loginAct,
        name: this.query.name
      }).then(resp => {
        if (resp.data.code === 200) {
          this.userList = resp.data.data.list
          this.pageSize = resp.data.data.pageSize
          this.total = resp.data.data.total
        }
      }).finally(() => {
        this.loading = false
      })
    },

    page(n) {
      this.getData(n)
    },

    resetQuery() {
      this.query.loginAct = ''
      this.query.name = ''
      this.getData(1)
    },

    openPermDialog(user) {
      this.currentUser = user
      this.checkedPermIds = []
      this.loadModules().then(() => {
        return this.loadUserPerms(user.id)
      }).then(() => {
        this.dialogVisible = true
      })
    },

    loadModules() {
      return doGet('/api/permissions/assignable-modules').then(resp => {
        if (resp.data.code === 200) {
          this.modules = resp.data.data
        }
      })
    },

    loadUserPerms(userId) {
      return doGet('/api/user/' + userId + '/permissions').then(resp => {
        if (resp.data.code === 200) {
          // 后端已过滤不可授权权限（权限管理模块），这里只保留当前模块列表中的按钮权限ID
          const allIds = resp.data.data || []
          // 只保留可授权模块中的按钮权限ID
          const validBtnIds = new Set()
          this.modules.forEach(m => {
            if (m.subPermissionList) {
              m.subPermissionList.forEach(p => validBtnIds.add(p.id))
            }
          })
          this.checkedPermIds = allIds.filter(id => validBtnIds.has(id))
        }
      })
    },

    isModuleAllChecked(module) {
      if (!module.subPermissionList || module.subPermissionList.length === 0) return false
      return module.subPermissionList.every(p => this.checkedPermIds.includes(p.id))
    },

    isModuleIndeterminate(module) {
      if (!module.subPermissionList || module.subPermissionList.length === 0) return false
      const count = module.subPermissionList.filter(p => this.checkedPermIds.includes(p.id)).length
      return count > 0 && count < module.subPermissionList.length
    },

    toggleModule(module, checked) {
      const childIds = (module.subPermissionList || []).map(p => p.id)
      if (checked) {
        childIds.forEach(id => {
          if (!this.checkedPermIds.includes(id)) this.checkedPermIds.push(id)
        })
      } else {
        this.checkedPermIds = this.checkedPermIds.filter(id => !childIds.includes(id))
      }
    },

    savePermissions() {
      // 前端只提交勾选的按钮权限ID，菜单链由后端根据权限父子关系自动补齐
      const uniqueIds = [...new Set(this.checkedPermIds)]

      this.saving = true
      doPut('/api/user/' + this.currentUser.id + '/permissions', uniqueIds).then(resp => {
        if (resp.data.code === 200) {
          messageTip('权限保存成功，该用户重新登录后生效', 'success')
          this.dialogVisible = false
        } else {
          messageTip(resp.data.msg || '权限保存失败', 'error')
        }
      }).finally(() => {
        this.saving = false
      })
    }
  }
})
</script>

<style scoped>
.container {
  padding: 10px;
}
.header {
  margin-bottom: 16px;
}
.search-box {
  display: flex;
  align-items: center;
}
.el-checkbox {
  margin-right: 16px;
  margin-bottom: 8px;
}
</style>
