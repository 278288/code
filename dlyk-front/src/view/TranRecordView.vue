<template>
  <el-button type="success" @click="goBack">返回</el-button>

  <el-form
      ref="tranRefForm"
      :model="tranQuery"
      :rules="tranRules"
      label-width="120px"
      style="margin-top: 20px; width: 600px;">

    <el-form-item label="客户" prop="customerId">
      <el-select v-model="tranQuery.customerId" style="width: 100%;">
        <el-option v-for="item in customerList"
                   :key="item.id"
                   :label="item.clueDO?.fullName + ' - ' + item.clueDO?.phone"
                   :value="item.id"/>
      </el-select>
    </el-form-item>

    <el-form-item label="交易金额" prop="money">
      <el-input v-model="tranQuery.money"/>
    </el-form-item>

    <el-form-item label="预计成交日期" prop="expectedDate">
      <el-date-picker v-model="tranQuery.expectedDate" type="datetime"
        placeholder="选择时间" style="width: 100%;"
        value-format="YYYY-MM-DD HH:mm:ss"/>
    </el-form-item>

    <el-form-item label="交易阶段" prop="stage">
      <el-select v-model="tranQuery.stage" style="width: 100%;">
        <el-option v-for="item in stageList"
                   :key="item.id"
                   :label="item.typeValue"
                   :value="item.id"/>
      </el-select>
    </el-form-item>

    <el-form-item label="描述" prop="description">
      <el-input v-model="tranQuery.description" type="textarea"/>
    </el-form-item>

    <el-form-item label="下次联系时间" prop="nextContactTime">
      <el-date-picker v-model="tranQuery.nextContactTime" type="datetime"
        placeholder="选择时间" style="width: 100%;"
        value-format="YYYY-MM-DD HH:mm:ss"/>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="submit">提交</el-button>
      <el-button @click="goBack">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import {defineComponent} from "vue";
import {doGet, doPost, doPut} from "../http/httpRequest.js";
import {messageTip} from "../util/util.js";

export default defineComponent({
  name: "TranRecordView",

  data() {
    return {
      tranQuery: {},
      customerList: [],
      stageList: [],
      tranRules: {
        customerId: [
          { required: true, message: '请选择客户', trigger: 'change' }
        ],
        money: [
          { required: true, message: '请输入交易金额', trigger: 'blur' },
          { pattern: /^[0-9]+(\.[0-9]{1,2})?$/, message: '金额格式不正确', trigger: 'blur' }
        ],
        expectedDate: [
          { required: true, message: '请选择预计成交日期', trigger: 'change' }
        ],
        stage: [
          { required: true, message: '请选择交易阶段', trigger: 'change' }
        ],
        description: [
          { required: true, message: '请输入交易描述', trigger: 'blur' },
          { min: 2, max: 255, message: '描述长度为2-255个字符', trigger: 'blur' }
        ],
        nextContactTime: [
          { required: true, message: '请选择下次联系时间', trigger: 'change' }
        ]
      }
    }
  },

  mounted() {
    this.loadCustomers();
    this.loadStage();
    let id = this.$route.params.id;
    if (id) {
      this.tranQuery.id = parseInt(id);
      this.loadData();
    }
  },

  methods: {
    loadCustomers() {
      doGet("/api/customers", {current: 1}).then(resp => {
        if (resp.data.code === 200) {
          this.customerList = resp.data.data.list;
        }
      })
    },

    loadStage() {
      doGet("/api/dicvalue/stage", {}).then(resp => {
        if (resp.data.code === 200) {
          this.stageList = resp.data.data;
        }
      })
    },

    loadData() {
      doGet("/api/tran/" + this.tranQuery.id, {}).then(resp => {
        if (resp.data.code === 200) {
          let data = resp.data.data;
          this.tranQuery.customerId = data.customerId;
          this.tranQuery.money = data.money;
          this.tranQuery.expectedDate = data.expectedDate;
          this.tranQuery.stage = data.stage;
          this.tranQuery.description = data.description;
          this.tranQuery.nextContactTime = data.nextContactTime;
        }
      })
    },

    submit() {
      this.$refs.tranRefForm.validate((isValid) => {
        if (isValid) {
          let isEdit = this.tranQuery.id > 0;
          let formData = new FormData();
          for (let field in this.tranQuery) {
            if (this.tranQuery[field] !== null && this.tranQuery[field] !== undefined && this.tranQuery[field] !== '') {
              formData.append(field, this.tranQuery[field]);
            }
          }
          let req = isEdit ? doPut("/api/tran", formData) : doPost("/api/tran", formData);
          req.then(resp => {
            if (resp.data.code === 200) {
              messageTip(isEdit ? "编辑成功" : "新增成功", "success");
              this.$router.push("/dashboard/tran");
            } else {
              messageTip(isEdit ? "编辑失败" : "新增失败", "error");
            }
          })
        }
      })
    },

    goBack() { this.$router.go(-1); }
  }
})
</script>