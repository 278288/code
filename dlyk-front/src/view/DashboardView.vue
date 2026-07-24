<template>
  <el-container>
    <el-aside :width="isCollapse ? '64px' : '200px'">
      <div class="menuTitle">客户关系管理系统</div>
      <el-menu active-text-color="#ffd04b" background-color="#334157" class="el-menu-vertical-demo"
        :default-active="currentRouterPath" text-color="#fff" style="border-right: solid 0px;" :collapse="isCollapse"
        :collapse-transition="false" :router="true" :unique-opened="true">
        <el-sub-menu :index="index" v-for="(menuPermission, index) in user.menuPermissionList" :key="menuPermission.id">
          <template #title>
            <el-icon><component :is="menuPermission.icon"></component></el-icon>
            <span> {{menuPermission.name}} </span>
          </template>
          <el-menu-item v-for="subPermission in menuPermission.subPermissionList" :key="subPermission.id" :index="subPermission.url">
            <el-icon><component :is="subPermission.icon"></component></el-icon>
            {{subPermission.name}}
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container class="rightContent">
      <el-header>
        <el-icon class="show" @click="showMenu"><Fold /></el-icon>
        <el-dropdown :hide-on-click="false">
          <span class="el-dropdown-link">
            {{ user.name }}
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="showProfileDialog">我的资料</el-dropdown-item>
              <el-dropdown-item @click="showPasswordDialog">修改密码</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main>
        <router-view v-if="isRouterAlive" />
      </el-main>

      <el-footer>@版权所有 zzx个人项目</el-footer>
    </el-container>
  </el-container>

  <!-- 我的资料弹窗 -->
  <el-dialog v-model="profileDialogVisible" title="我的资料" width="450px">
    <el-form :model="profileForm" label-width="80px">
      <el-form-item label="姓名">
        <el-input v-model="profileForm.name"/>
      </el-form-item>
      <el-form-item label="手机">
        <el-input v-model="profileForm.phone"/>
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="profileForm.email"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="profileDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitProfile">保存</el-button>
    </template>
  </el-dialog>

  <!-- 修改密码弹窗 -->
  <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
    <el-form :model="passwordForm" label-width="100px">
      <el-form-item label="原密码">
        <el-input v-model="passwordForm.oldPwd" type="password" show-password/>
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="passwordForm.newPwd" type="password" show-password/>
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input v-model="passwordForm.confirmPwd" type="password" show-password/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="passwordDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitPassword">保存</el-button>
    </template>
  </el-dialog>
</template>

<script>
import { defineComponent } from 'vue'
import { doGet, doPut } from "../http/httpRequest.js";
import { messageConfirm, messageTip, removeToken } from "../util/util.js";

export default defineComponent({
  name: "DashboardView",

  data() {
    return {
      isCollapse: false,
      user: {},
      isRouterAlive: true,
      currentRouterPath: '',
      profileDialogVisible: false,
      profileForm: { id: 0, name: '', phone: '', email: '' },
      passwordDialogVisible: false,
      passwordForm: { oldPwd: '', newPwd: '', confirmPwd: '' }
    }
  },

  provide() {
    return {
      reload: () => {
        this.isRouterAlive = false;
        this.$nextTick(() => { this.isRouterAlive = true; })
      },
      content: "是对负荷计算东方红郭凯撒的合法户籍卡",
      age: 28,
      user: { id: 1098, name: "张三", age: 18 },
      arr: [12, 56, 109, 356, 8901]
    }
  },

  mounted() {
    this.loadLoginUser();
    this.loadCurrentRouterPath();
  },

  methods: {
    showMenu() { this.isCollapse = !this.isCollapse; },

    loadLoginUser() {
      doGet("/api/login/info", {}).then((resp) => {
        this.user = resp.data.data;
      })
    },

    showProfileDialog() {
      this.profileForm.id = this.user.id;
      this.profileForm.name = this.user.name;
      this.profileForm.phone = this.user.phone;
      this.profileForm.email = this.user.email;
      this.profileDialogVisible = true;
    },

    submitProfile() {
      let fd = new FormData();
      fd.append("id", this.profileForm.id);
      if (this.profileForm.name) fd.append("name", this.profileForm.name);
      if (this.profileForm.phone) fd.append("phone", this.profileForm.phone);
      if (this.profileForm.email) fd.append("email", this.profileForm.email);
      doPut("/api/user/profile", fd).then(resp => {
        if (resp.data.code === 200) {
          messageTip("保存成功", "success");
          this.profileDialogVisible = false;
          this.loadLoginUser();
        } else { messageTip("保存失败", "error"); }
      })
    },

    showPasswordDialog() {
      this.passwordForm = { oldPwd: '', newPwd: '', confirmPwd: '' };
      this.passwordDialogVisible = true;
    },

    submitPassword() {
      if (!this.passwordForm.oldPwd || !this.passwordForm.newPwd) {
        messageTip("密码不能为空", "warning"); return;
      }
      if (this.passwordForm.newPwd !== this.passwordForm.confirmPwd) {
        messageTip("两次密码不一致", "warning"); return;
      }
      let fd = new FormData();
      fd.append("oldPwd", this.passwordForm.oldPwd);
      fd.append("newPwd", this.passwordForm.newPwd);
      doPut("/api/user/password", fd).then(resp => {
        if (resp.data.code === 200) {
          messageTip("修改成功", "success");
          this.passwordDialogVisible = false;
        } else { messageTip(resp.data.msg || "原密码错误", "error"); }
      })
    },

    logout() {
      doGet("/api/logout", {}).then(resp => {
        if (resp.data.code === 200) {
          removeToken();
          messageTip("退出成功", "success");
          window.location.href = "/";
        } else {
          messageConfirm("退出异常，是否要强制退出？").then(() => {
            removeToken();
            window.location.href = "/";
          }).catch(() => { messageTip("取消强制退出", "warning"); })
        }
      })
    },

    loadCurrentRouterPath() {
      let path = this.$route.path;
      let arr = path.split("/");
      if (arr.length > 3) {
        this.currentRouterPath = "/" + arr[1] + "/" + arr[2];
      } else {
        this.currentRouterPath = path;
      }
    }
  }
})
</script>

<style scoped>
.el-aside { background: #1a1a1a; }
.el-header { background: azure; height: 35px; line-height: 35px; }
.el-footer { background: aliceblue; height: 35px; line-height: 35px; text-align: center; }
.rightContent { height: calc(100vh); }
.menuTitle { height: 35px; line-height: 35px; color: #f9f9f9; text-align: center; }
.show { cursor: pointer; }
.el-dropdown { float: right; line-height: 35px; }
</style>