<template>
  <el-button type="success" @click="$router.push('/dashboard/tran')">返回</el-button>
  <el-button type="warning" @click="$router.push('/dashboard/tran/edit/' + $route.params.id)">编辑</el-button>

  <el-form label-width="120px" style="margin-top: 20px;">
    <el-form-item label="交易流水号"><span>{{ tran.tranNo }}</span></el-form-item>
    <el-form-item label="客户"><span>{{ tran.customerDO?.clueDO?.fullName }}</span></el-form-item>
    <el-form-item label="手机"><span>{{ tran.customerDO?.clueDO?.phone }}</span></el-form-item>
    <el-form-item label="交易金额"><span>{{ tran.money }}</span></el-form-item>
    <el-form-item label="预计成交日期"><span>{{ tran.expectedDate }}</span></el-form-item>
    <el-form-item label="阶段"><span>{{ tran.stage }}</span></el-form-item>
    <el-form-item label="描述"><span>{{ tran.description }}</span></el-form-item>
    <el-form-item label="下次联系时间"><span>{{ tran.nextContactTime }}</span></el-form-item>
    <el-form-item label="创建时间"><span>{{ tran.createTime }}</span></el-form-item>
  </el-form>
</template>

<script>
import {defineComponent} from "vue";
import {doGet} from "../http/httpRequest.js";

export default defineComponent({
  name: "TranDetailView",

  data() {
    return { tran: {customerDO: {clueDO: {}}} }
  },

  mounted() {
    doGet("/api/tran/" + this.$route.params.id, {}).then(resp => {
      if (resp.data.code === 200) { this.tran = resp.data.data; }
    })
  }
})
</script>