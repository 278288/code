<template>
  <el-button type="primary" class="btn" @click="$router.push('/dashboard/dictype/add')">新增类型</el-button>

  <el-table :data="typeList" style="width: 100%">
    <el-table-column type="index" label="序号" width="65"/>
    <el-table-column property="typeCode" label="类型代码" width="180"/>
    <el-table-column property="typeName" label="类型名称" width="180"/>
    <el-table-column property="remark" label="备注"/>
    <el-table-column label="操作" width="160" fixed="right">
      <template #default="scope">
        <el-button type="warning" size="small" @click="$router.push('/dashboard/dictype/edit/' + scope.row.id)">编辑</el-button>
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
  name: "DicTypeView",
  data() { return { typeList: [], pageSize: 0, total: 0 } },
  mounted() { this.getData(1); },
  methods: {
    getData(current) {
      doGet("/api/dictypes", {current}).then(resp => {
        if (resp.data.code === 200) {
          this.typeList = resp.data.data.list;
          this.pageSize = resp.data.data.pageSize;
          this.total = resp.data.data.total;
        }
      })
    },
    page(n) { this.getData(n); },
    del(id) {
      messageConfirm("确认删除?").then(() => {
        doDelete("/api/dictype/" + id, {}).then(resp => {
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