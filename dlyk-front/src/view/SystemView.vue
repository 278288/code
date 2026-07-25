<template>
  <div class="system-page">
    <div class="toolbar">
      <span class="page-title">SystemConfig</span>
      <el-button type="primary" @click="$router.push('/dashboard/system/add')">新增配置</el-button>
    </div>

    <div class="content-area" v-if="systemList.length > 0">
      <div class="config-list">
        <div
          v-for="item in systemList"
          :key="item.id"
          class="config-item"
          :class="{ active: activeId === item.id }"
          @click="selectItem(item)"
        >
          <div class="config-name">{{ item.name }}</div>
          <div class="config-code">{{ item.systemCode }}</div>
          <el-tag :type="item.isopen === '1' ? 'success' : 'info'" size="small" class="config-tag">
            {{ item.isopen === '1' ? 'Running' : 'Stopped' }}
          </el-tag>
        </div>

        <div class="pagination-mini">
          <el-pagination small background layout="prev, pager, next"
              :page-size="pageSize" :total="total" @current-change="page"/>
        </div>
      </div>

      <div class="config-detail" v-if="selected">
        <div class="detail-header">
          <span class="detail-title">{{ selected.name }}</span>
          <div>
            <el-button type="warning" size="small" @click="$router.push('/dashboard/system/edit/' + selected.id)">Edit</el-button>
            <el-button type="danger" size="small" @click="del(selected.id)">Delete</el-button>
          </div>
        </div>
        <el-divider />
        <div class="detail-grid">
          <div class="field"><span class="field-label">Code</span><span class="field-value">{{ selected.systemCode }}</span></div>
          <div class="field"><span class="field-label">Site</span><span class="field-value">{{ selected.site }}</span></div>
          <div class="field"><span class="field-label">Version</span><span class="field-value">{{ selected.version }}</span></div>
          <div class="field"><span class="field-label">Tel</span><span class="field-value">{{ selected.tel }}</span></div>
          <div class="field"><span class="field-label">WeChat</span><span class="field-value">{{ selected.weixin }}</span></div>
          <div class="field"><span class="field-label">Email</span><span class="field-value">{{ selected.email }}</span></div>
          <div class="field"><span class="field-label">Address</span><span class="field-value">{{ selected.address }}</span></div>
          <div class="field"><span class="field-label">Keywords</span><span class="field-value">{{ selected.keywords }}</span></div>
          <div class="field"><span class="field-label">Logo</span><span class="field-value">{{ selected.logo }}</span></div>
          <div class="field"><span class="field-label">Icon</span><span class="field-value">{{ selected.shortcuticon }}</span></div>
          <div class="field field-full"><span class="field-label">Title</span><span class="field-value">{{ selected.title }}</span></div>
          <div class="field field-full"><span class="field-label">Description</span><span class="field-value">{{ selected.description }}</span></div>
          <div class="field field-full"><span class="field-label">CloseMsg</span><span class="field-value">{{ selected.closemsg }}</span></div>
          <div class="field"><span class="field-label">Created</span><span class="field-value">{{ selected.createTime }}</span></div>
        </div>
      </div>

      <div class="config-detail empty-detail" v-else>
        <span>Select a configuration from the left</span>
      </div>
    </div>

    <div class="empty-state" v-else>
      <span>No configs yet. Click "新增配置" to add one.</span>
    </div>
  </div>
</template>

<script>
import {defineComponent} from "vue";
import {doDelete, doGet} from "../http/httpRequest.js";
import {messageConfirm, messageTip} from "../util/util.js";

export default defineComponent({
  name: "SystemView",
  data() {
    return {
      systemList: [], pageSize: 0, total: 0,
      activeId: null, selected: null
    }
  },
  mounted() { this.getData(1); },
  methods: {
    getData(current) {
      doGet("/api/systems", {current}).then(resp => {
        if (resp.data.code === 200) {
          this.systemList = resp.data.data.list;
          this.pageSize = resp.data.data.pageSize;
          this.total = resp.data.data.total;
          if (this.systemList.length > 0 && !this.activeId) {
            this.selectItem(this.systemList[0]);
          }
        }
      })
    },
    page(n) { this.getData(n); },
    selectItem(item) {
      this.activeId = item.id;
      this.selected = item;
    },
    del(id) {
      messageConfirm("确认删除?").then(() => {
        doDelete("/api/system/" + id, {}).then(resp => {
          if (resp.data.code === 200) {
            messageTip("删除成功", "success");
            this.selected = null;
            this.activeId = null;
            this.getData(1);
          } else { messageTip("删除失败", "error"); }
        })
      }).catch(() => { messageTip("取消删除", "warning"); })
    }
  }
})
</script>

<style scoped>
.system-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.content-area {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
}

.config-list {
  width: 260px;
  flex-shrink: 0;
  background: #fafafa;
  border-radius: 6px;
  padding: 8px;
  display: flex;
  flex-direction: column;
}

.config-item {
  padding: 12px 14px;
  border-radius: 4px;
  cursor: pointer;
  transition: background .15s;
}

.config-item:hover { background: #ecf5ff; }
.config-item.active { background: #d9ecff; }

.config-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.config-code {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.config-tag { margin-top: 6px; }

.pagination-mini {
  margin-top: auto;
  padding-top: 8px;
  display: flex;
  justify-content: center;
}

.config-detail {
  flex: 1;
  background: #fff;
  border-radius: 6px;
  padding: 20px 24px;
  border: 1px solid #ebeef5;
  overflow-y: auto;
}

.empty-detail {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 15px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-title {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 24px;
}

.field {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
}

.field-full { grid-column: 1 / -1; }

.field-label {
  color: #909399;
  font-size: 13px;
  flex-shrink: 0;
  margin-right: 16px;
}

.field-value {
  color: #303133;
  font-size: 13px;
  text-align: right;
  word-break: break-all;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 15px;
}
</style>