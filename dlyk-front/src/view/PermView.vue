<template>
  <el-table :data="userList" style="width:100%">
    <el-table-column type="index" label="序号" width="60" />
    <el-table-column prop="loginAct" label="账号" />
    <el-table-column prop="name" label="姓名" />
    <el-table-column prop="phone" label="手机" />
    <el-table-column prop="email" label="邮箱" />
    <el-table-column label="操作">
      <template #default="scope">
        <el-button type="primary" size="small" @click="managePerm(scope.row.id)">管理权限</el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-pagination background layout="prev,pager,next" :page-size="pageSize" :total="total"
    @prev-click="page" @next-click="page" @current-change="page" style="margin-top:12px"/>
</template>

<script>
import { defineComponent } from "vue";
import { doGet } from "../http/httpRequest.js";

export default defineComponent({
  name: "PermView",
  data() {
    return { userList: [], pageSize: 0, total: 0 };
  },
  mounted() { this.getData(1); },
  methods: {
    getData(current) {
      doGet("/api/users", { current }).then(resp => {
        if (resp.data.code === 200) {
          this.userList = resp.data.data.list;
          this.pageSize = resp.data.data.pageSize;
          this.total = resp.data.data.total;
        }
      });
    },
    page(n) { this.getData(n); },
    managePerm(id) { this.$router.push("/dashboard/user/perm/" + id); }
  }
});
</script>
