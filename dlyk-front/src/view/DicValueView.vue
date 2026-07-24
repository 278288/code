<template>
  <el-container>
    <el-aside width="200px" style="background:#f5f7fa; padding: 16px;">
      <el-menu :default-active="activeType" @select="onTypeSelect">
        <el-menu-item v-for="item in typeList" :key="item.typeCode" :index="item.typeCode">
          <span>{{ item.typeName }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-main>
      <div>
        <el-button type="primary" class="btn" @click="goAdd">新增数据</el-button>
        <el-button type="danger" class="btn" @click="batchDel" :disabled="selectedIds.length === 0">批量删除</el-button>
      </div>
      <el-table :data="valueList" style="width: 100%; margin-top: 10px;" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50"/>
        <el-table-column type="index" label="序号" width="65"/>
        <el-table-column property="typeValue" label="字典值" width="200"/>
        <el-table-column property="order" label="排序" width="80"/>
        <el-table-column property="remark" label="备注"/>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <el-button type="warning" size="small" @click="$router.push('/dashboard/dicvalue/edit/' + scope.row.id)">编辑</el-button>
            <el-button type="danger" size="small" @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <p>
        <el-pagination background layout="prev, pager, next"
            :page-size="pageSize" :total="total" @current-change="page"/>
      </p>
    </el-main>
  </el-container>
</template>

<script>
import {defineComponent} from "vue";
import {doDelete, doGet} from "../http/httpRequest.js";
import {messageConfirm, messageTip} from "../util/util.js";

export default defineComponent({
  name: "DicValueView",
  data() {
    return { typeList: [], valueList: [], activeType: '', pageSize: 0, total: 0, selectedIds: [] }
  },
  mounted() {
    doGet("/api/dictypes", {current: 1}).then(resp => {
      if (resp.data.code === 200) {
        this.typeList = resp.data.data.list;
        if (this.typeList.length > 0) {
          this.activeType = this.typeList[0].typeCode;
          this.getData(1);
        }
      }
    })
  },
  methods: {
    onTypeSelect(typeCode) { this.activeType = typeCode; this.getData(1); },
    getData(current) {
      doGet("/api/dicvalues", {current, typeCode: this.activeType}).then(resp => {
        if (resp.data.code === 200) {
          this.valueList = resp.data.data.list;
          this.pageSize = resp.data.data.pageSize;
          this.total = resp.data.data.total;
        }
      })
    },
    page(n) { this.getData(n); },
    handleSelectionChange(data) { this.selectedIds = data.map(d => d.id); },
    goAdd() {
      if (!this.activeType) { messageTip("请先选择一个字典类型", "warning"); return; }
      this.$router.push("/dashboard/dicvalue/add?typeCode=" + this.activeType);
    },
    del(id) {
      messageConfirm("确认删除?").then(() => {
        doDelete("/api/dicvalue/" + id, {}).then(resp => {
          if (resp.data.code === 200) { messageTip("删除成功", "success"); this.getData(1); }
          else { messageTip("删除失败", "error"); }
        })
      }).catch(() => { messageTip("取消删除", "warning"); })
    },
    batchDel() {
      messageConfirm("确认批量删除?").then(() => {
        doDelete("/api/dicvalue", {ids: this.selectedIds.join(",")}).then(resp => {
          if (resp.data.code === 200) { messageTip("批量删除成功", "success"); this.getData(1); }
          else { messageTip("批量删除失败", "error"); }
        })
      }).catch(() => { messageTip("取消删除", "warning"); })
    }
  }
})
</script>

<style scoped>
.el-aside { border-right: 1px solid #e4e7ed; }
</style>