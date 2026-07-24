<template>
  <div class="product-container">
    <div class="toolbar">
      <el-button type="primary" @click="$router.push('/dashboard/product/add')">新增产品</el-button>
    </div>

    <div class="card-grid">
      <el-card v-for="item in productList" :key="item.id" class="product-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="product-name">{{ item.name }}</span>
            <el-tag :type="item.state === 0 ? 'success' : 'info'" size="small">
              {{ item.state === 0 ? '在售' : '售罄' }}
            </el-tag>
          </div>
        </template>
        <div class="card-body">
          <div class="price-row">
            <span class="label">官方指导价</span>
            <span class="value">{{ item.guidePriceS }} ~ {{ item.guidePriceE }} 万</span>
          </div>
          <div class="price-row highlight">
            <span class="label">经销商报价</span>
            <span class="value">{{ item.quotation }} 万</span>
          </div>
          <div class="card-time">创建于 {{ item.createTime }}</div>
        </div>
        <div class="card-footer">
          <el-button type="warning" size="small" @click="$router.push('/dashboard/product/edit/' + item.id)">编辑</el-button>
          <el-button type="danger" size="small" @click="del(item.id)">删除</el-button>
        </div>
      </el-card>
    </div>

    <div v-if="productList.length === 0" class="empty-tip">暂无产品数据</div>

    <div class="pagination-bar">
      <el-pagination background layout="prev, pager, next"
          :page-size="pageSize" :total="total"
          @current-change="page"/>
    </div>
  </div>
</template>

<script>
import {defineComponent} from "vue";
import {doDelete, doGet} from "../http/httpRequest.js";
import {messageConfirm, messageTip} from "../util/util.js";

export default defineComponent({
  name: "ProductView",

  data() {
    return { productList: [], pageSize: 0, total: 0 }
  },

  mounted() { this.getData(1); },

  methods: {
    getData(current) {
      doGet("/api/products", {current}).then(resp => {
        if (resp.data.code === 200) {
          this.productList = resp.data.data.list;
          this.pageSize = resp.data.data.pageSize;
          this.total = resp.data.data.total;
        }
      })
    },
    page(number) { this.getData(number); },

    del(id) {
      messageConfirm("确认删除该产品?").then(() => {
        doDelete("/api/product/" + id, {}).then(resp => {
          if (resp.data.code === 200) { messageTip("删除成功", "success"); this.getData(1); }
          else { messageTip("删除失败", "error"); }
        })
      }).catch(() => { messageTip("取消删除", "warning"); })
    }
  }
})
</script>

<style scoped>
.product-container {
  padding: 0;
}

.toolbar {
  margin-bottom: 20px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.product-card {
  border-radius: 6px;
}

.product-card :deep(.el-card__header) {
  padding: 14px 20px;
  border-bottom: 1px solid #ebeef5;
}

.product-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-body {
  min-height: 80px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 14px;
  color: #606266;
}

.price-row .label {
  color: #909399;
}

.price-row.highlight .value {
  color: #e6a23c;
  font-weight: 600;
  font-size: 16px;
}

.card-time {
  margin-top: 10px;
  font-size: 12px;
  color: #c0c4cc;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  margin-top: 8px;
}

.empty-tip {
  text-align: center;
  color: #c0c4cc;
  padding: 60px 0;
  font-size: 16px;
}

.pagination-bar {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>