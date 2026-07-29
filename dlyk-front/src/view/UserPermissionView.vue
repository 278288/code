<template>
  <el-button type="success" @click="goBack">返回</el-button>

  <h3 style="margin: 16px 0;">用户权限管理 - {{ userName }}</h3>

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

  <el-button type="primary" size="large" @click="save" :loading="saving" style="margin-top: 16px;">
    保存权限
  </el-button>
</template>

<script>
import { defineComponent } from "vue";
import { doGet, doPut } from "../http/httpRequest.js";
import { messageTip } from "../util/util.js";

export default defineComponent({
  name: "UserPermissionView",

  data() {
    return {
      userId: null,
      userName: "",
      modules: [],
      checkedPermIds: [],
      saving: false
    };
  },

  mounted() {
    this.userId = parseInt(this.$route.params.id);
    this.loadUser();
    this.loadModules().then(() => {
      return this.loadUserPerms();
    });
  },

  methods: {
    loadUser() {
      doGet("/api/user/" + this.userId, {}).then(resp => {
        if (resp.data.code === 200) {
          this.userName = resp.data.data.loginAct + " - " + resp.data.data.name;
        }
      });
    },

    // 从API加载可授权模块（排除权限管理本身）
    loadModules() {
      return doGet("/api/permissions/assignable-modules").then(resp => {
        if (resp.data.code === 200) {
          this.modules = resp.data.data;
        }
      });
    },

    // 加载用户当前的个人权限
    loadUserPerms() {
      return doGet("/api/user/" + this.userId + "/permissions").then(resp => {
        if (resp.data.code === 200) {
          // 过滤掉权限管理模块自身的ID
          const allIds = (resp.data.data || []).filter(id => id !== 67 && id !== 68);
          // 只保留可授权模块中的按钮权限ID
          const validBtnIds = new Set();
          this.modules.forEach(m => {
            if (m.subPermissionList) {
              m.subPermissionList.forEach(p => validBtnIds.add(p.id));
            }
          });
          this.checkedPermIds = allIds.filter(id => validBtnIds.has(id));
        }
      });
    },

    isModuleAllChecked(module) {
      if (!module.subPermissionList || module.subPermissionList.length === 0) return false;
      return module.subPermissionList.every(child => this.checkedPermIds.includes(child.id));
    },

    isModuleIndeterminate(module) {
      if (!module.subPermissionList || module.subPermissionList.length === 0) return false;
      const checked = module.subPermissionList.filter(child => this.checkedPermIds.includes(child.id)).length;
      return checked > 0 && checked < module.subPermissionList.length;
    },

    toggleModule(module, checked) {
      const childIds = (module.subPermissionList || []).map(c => c.id);
      if (checked) {
        childIds.forEach(id => {
          if (!this.checkedPermIds.includes(id)) this.checkedPermIds.push(id);
        });
      } else {
        this.checkedPermIds = this.checkedPermIds.filter(id => !childIds.includes(id));
      }
    },

    save() {
      // 模块ID到其子菜单ID的映射
      const moduleMenuMap = {
        1: 2, 10: 12, 19: 20, 24: 25, 28: 29,
        35: [36, 42], 48: 49
      };

      const allPermIds = [...this.checkedPermIds];
      this.modules.forEach(module => {
        const hasChecked = (module.subPermissionList || []).some(p => this.checkedPermIds.includes(p.id));
        if (hasChecked) {
          allPermIds.push(module.id);
          const childMenuIds = moduleMenuMap[module.id];
          if (Array.isArray(childMenuIds)) {
            allPermIds.push(...childMenuIds);
          } else if (childMenuIds) {
            allPermIds.push(childMenuIds);
          }
        }
      });
      const uniqueIds = [...new Set(allPermIds)];

      this.saving = true;
      doPut("/api/user/" + this.userId + "/permissions", uniqueIds).then(resp => {
        if (resp.data.code === 200) {
          messageTip("权限保存成功", "success");
        } else {
          messageTip("权限保存失败", "error");
        }
      }).finally(() => {
        this.saving = false;
      });
    },

    goBack() { this.$router.go(-1); }
  }
});
</script>

<style scoped>
.el-checkbox {
  margin-right: 16px;
  margin-bottom: 8px;
}
</style>
