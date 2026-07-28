<template>
  <el-button type="primary" class="btn" @click="$router.push('/dashboard/tran/add')">新增交易</el-button>
  <el-radio-group v-model="viewMode" size="small" style="margin-bottom:10px">
    <el-radio-button label="list">列表</el-radio-button>
    <el-radio-button label="kanban">看板</el-radio-button>
  </el-radio-group>
  <el-button type="danger" class="btn" @click="batchDel">批量删除</el-button>
  <div v-if="viewMode === 'list'">
    <!-- 原来的 el-table 和 el-pagination -->
    <el-table :data="tranList" style="width: 100%" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column type="index" label="序号" width="65" />
      <el-table-column property="tranNo" label="交易流水号" width="180" />
      <el-table-column label="客户" width="120">
        <template #default="scope">
          <a href="javascript:" @click="$router.push('/dashboard/tran/' + scope.row.id)">
            {{ scope.row.customerDO?.clueDO?.fullName }}
          </a>
        </template>
      </el-table-column>
      <el-table-column label="手机" width="120">
        <template #default="scope">
          <span>{{ scope.row.customerDO?.clueDO?.phone }}</span>
        </template>
      </el-table-column>
      <el-table-column property="money" label="交易金额" width="120" />
      <el-table-column property="expectedDate" label="预计成交日期" width="165" />
      <el-table-column property="stage" label="阶段" width="100" />
      <el-table-column property="description" label="描述" />
      <el-table-column property="nextContactTime" label="下次联系时间" width="165" />
      <el-table-column property="createTime" label="创建时间" width="165" />
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="$router.push('/dashboard/tran/' + scope.row.id)">详情</el-button>
          <el-button type="warning" size="small"
            @click="$router.push('/dashboard/tran/edit/' + scope.row.id)">编辑</el-button>
          <el-button type="danger" size="small" @click="del(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <p>
      <el-pagination background layout="prev, pager, next" :page-size="pageSize" :total="total" @prev-click="page"
        @next-click="page" @current-change="page" />
    </p>
  </div>
  <div v-if="viewMode === 'kanban'">
    <TranKanbanView />
  </div>
</template>

<script>
  import { defineComponent } from "vue";
  import { doDelete, doGet } from "../http/httpRequest.js";
  import { messageConfirm, messageTip } from "../util/util.js";
  import TranKanbanView from "./TranKanbanView.vue";

  export default defineComponent({
    name: "TranView",

    data() {
      return {
        tranList: [{ customerDO: { clueDO: {} } }],
        pageSize: 0,
        total: 0,
        tranIdArray: [],
        viewMode: 'list',
        viewOptions: [
          { label: '列表', value: 'list' },
          { label: '看板', value: 'kanban' }
        ]
      }
    },
    components: { TranKanbanView },
    mounted() { this.getData(1); },

    methods: {
      getData(current) {
        doGet("/api/trans", { current }).then(resp => {
          if (resp.data.code === 200) {
            this.tranList = resp.data.data.list;
            this.pageSize = resp.data.data.pageSize;
            this.total = resp.data.data.total;
          }
        })
      },

      page(number) { this.getData(number); },

      handleSelectionChange(data) { this.tranIdArray = data.map(d => d.id); },

      del(id) {
        messageConfirm("确认删除该交易?").then(() => {
          doDelete("/api/tran/" + id, {}).then(resp => {
            if (resp.data.code === 200) {
              messageTip("删除成功", "success");
              this.getData(1);
            } else {
              messageTip("删除失败", "error");
            }
          })
        }).catch(() => {
          messageTip("取消删除", "warning");
        })
      },

      batchDel() {
        if (this.tranIdArray.length <= 0) {
          messageTip("请选择要删除的数据", "warning");
          return;
        }
        messageConfirm("确认批量删除?").then(() => {
          doDelete("/api/tran", { ids: this.tranIdArray.join(",") }).then(resp => {
            if (resp.data.code === 200) {
              messageTip("批量删除成功", "success");
              this.getData(1);
            } else {
              messageTip("批量删除失败", "error");
            }
          })
        }).catch(() => {
          messageTip("取消删除", "warning");
        })
      }
    }
  })
</script>

<style scoped>
  .el-table {
    margin-top: 10px;
  }
</style>