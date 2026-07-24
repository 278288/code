<template>
  <el-button type="success" @click="$router.push('/dashboard/customer')">返回</el-button>

  <el-form label-width="120px" style="margin-top: 20px;">
    <el-form-item label="负责人">
      <span>{{ customer.ownerDO?.name }}</span>
    </el-form-item>
    <el-form-item label="所属活动">
      <span>{{ customer.activityDO?.name }}</span>
    </el-form-item>
    <el-form-item label="姓名">
      <span>{{ customer.clueDO?.fullName }}</span>
    </el-form-item>
    <el-form-item label="称呼">
      <span>{{ customer.appellationDO?.typeValue }}</span>
    </el-form-item>
    <el-form-item label="手机">
      <span>{{ customer.clueDO?.phone }}</span>
    </el-form-item>
    <el-form-item label="微信">
      <span>{{ customer.clueDO?.weixin }}</span>
    </el-form-item>
    <el-form-item label="QQ">
      <span>{{ customer.clueDO?.qq }}</span>
    </el-form-item>
    <el-form-item label="邮箱">
      <span>{{ customer.clueDO?.email }}</span>
    </el-form-item>
    <el-form-item label="年龄">
      <span>{{ customer.clueDO?.age }}</span>
    </el-form-item>
    <el-form-item label="职业">
      <span>{{ customer.clueDO?.job }}</span>
    </el-form-item>
    <el-form-item label="年收入">
      <span>{{ customer.clueDO?.yearIncome }}</span>
    </el-form-item>
    <el-form-item label="地址">
      <span>{{ customer.clueDO?.address }}</span>
    </el-form-item>
    <el-form-item label="是否贷款">
      <span>{{ customer.needLoanDO?.typeValue }}</span>
    </el-form-item>
    <el-form-item label="意向状态">
      <span>{{ customer.intentionStateDO?.typeValue }}</span>
    </el-form-item>
    <el-form-item label="意向产品">
      <span>{{ customer.intentionProductDO?.name }}</span>
    </el-form-item>
    <el-form-item label="客户描述">
      <span>{{ customer.description }}</span>
    </el-form-item>
    <el-form-item label="下次联系时间">
      <span>{{ customer.nextContactTime }}</span>
    </el-form-item>
    <el-form-item label="创建时间">
      <span>{{ customer.createTime }}</span>
    </el-form-item>
  </el-form>
</template>

<script>
import {defineComponent} from "vue";
import {doGet} from "../http/httpRequest.js";

export default defineComponent({
  name: "CustomerDetailView",

  data() {
    return {
      customer: {
        clueDO: {},
        ownerDO: {},
        activityDO: {},
        appellationDO: {},
        needLoanDO: {},
        intentionStateDO: {},
        stateDO: {},
        sourceDO: {},
        intentionProductDO: {},
      }
    }
  },

  mounted() {
    let id = this.$route.params.id;
    doGet("/api/customer/" + id, {}).then(resp => {
      if (resp.data.code === 200) {
        this.customer = resp.data.data;
      }
    })
  }
})
</script>