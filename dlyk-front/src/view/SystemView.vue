<template>
  <el-button type="primary" class="btn" @click="$router.push('/dashboard/system/add')">新增系统信息</el-button>

  <el-table :data="systemList" style="width: 100%">
    <el-table-column type="index" label="序号" width="65"/>
    <el-table-column property="name" label="系统名称" width="160"/>
    <el-table-column property="systemCode" label="系统编码" width="140"/>
    <el-table-column property="site" label="站点地址" width="200"/>
    <el-table-column property="version" label="版本" width="100"/>
    <el-table-column property="tel" label="电话" width="140"/>
    <el-table-column property="isopen" label="是否开启" width="90">
      <template #default="scope">
        <el-tag :type="scope.row.isopen === '1' ? 'success' : 'info'" size="small">
          {{ scope.row.isopen === '1' ? '开启' : '关闭' }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column property="createTime" label="创建时间" width="165"/>
    <el-table-column label="操作" width="160" fixed="right">
      <template #default="scope">
        <el-button type="warning" size="small" @click="$router.push('/dashboard/system/edit/' + scope.row.id)">编辑</el-button>
        <el-button type="danger" size="small" @click="del(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <p>
    <el-pagination background layout="prev, pager, next"
        :page-size="pageSize" :total="total" @current-change="page"/>
  </p>
</template>

<script>
import {defineComponent} from "vue";
import {doDelete, doGet} from "../http/httpRequest.js";
import {messageConfirm, messageTip} from "../util/util.js";

export default defineComponent({
  name: "SystemView",
  data() { return { systemList: [], pageSize: 0, total: 0 } },
  mounted() { this.getData(1); },
  methods: {
    getData(current) {
      doGet("/api/systems", {current}).then(resp => {
        if (resp.data.code === 200) {
          this.systemList = resp.data.data.list;
          this.pageSize = resp.data.data.pageSize;
          this.total = resp.data.data.total;
        }
      })
    },
    page(n) { this.getData(n); },
    del(id) {
      messageConfirm("确认删除?").then(() => {
        doDelete("/api/system/" + id, {}).then(resp => {
          if (resp.data.code === 200) { messageTip("删除成功", "success"); this.getData(1); }
          else { messageTip("删除失败", "error"); }
        })
      }).catch(() => { messageTip("取消删除", "warning"); })
    }
  }
})
</script>

<style scoped>
.el-table { margin-top: 10px; }
</style>