<template>
  <el-button type="success" @click="goBack">返回</el-button>
  <el-form ref="formRef" :model="query" :rules="rules" label-width="100px" style="margin-top: 20px; width: 500px;">
    <el-form-item label="所属类型" prop="typeCode">
      <el-select v-model="query.typeCode" style="width: 100%;" :disabled="query.id > 0">
        <el-option v-for="item in typeList" :key="item.typeCode" :label="item.typeName" :value="item.typeCode"/>
      </el-select>
    </el-form-item>
    <el-form-item label="字典值" prop="typeValue">
      <el-input v-model="query.typeValue"/>
    </el-form-item>
    <el-form-item label="排序" prop="order">
      <el-input v-model="query.order"/>
    </el-form-item>
    <el-form-item label="备注">
      <el-input v-model="query.remark"/>
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
  name: "DicValueRecordView",
  data() {
    return {
      query: {},
      typeList: [],
      rules: {
        typeCode: [{ required: true, message: '请选择所属类型', trigger: 'change' }],
        typeValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
        order: [{ required: true, message: '请输入排序号', trigger: 'blur' }],
      }
    }
  },
  mounted() {
    doGet("/api/dictypes", {current: 1}).then(resp => {
      if (resp.data.code === 200) { this.typeList = resp.data.data.list; }
    });
    let id = this.$route.params.id;
    if (id) { this.query.id = parseInt(id); this.loadData(); return; }
    let typeCode = this.$route.query.typeCode;
    if (typeCode) { this.query.typeCode = typeCode; }
  },
  methods: {
    loadData() {
      doGet("/api/dicvalue/" + this.query.id, {}).then(resp => {
        if (resp.data.code === 200) {
          let d = resp.data.data;
          this.query.typeCode = d.typeCode;
          this.query.typeValue = d.typeValue;
          this.query.order = d.order;
          this.query.remark = d.remark;
        }
      })
    },
    submit() {
      this.$refs.formRef.validate((isValid) => {
        if (isValid) {
          let isEdit = this.query.id > 0;
          let fd = new FormData();
          for (let f in this.query) {
            if (this.query[f] !== null && this.query[f] !== undefined && this.query[f] !== '') {
              fd.append(f, this.query[f]);
            }
          }
          let req = isEdit ? doPut("/api/dicvalue", fd) : doPost("/api/dicvalue", fd);
          req.then(resp => {
            if (resp.data.code === 200) {
              messageTip(isEdit ? "编辑成功" : "新增成功", "success");
              this.$router.push("/dashboard/dicvalue");
            } else { messageTip(isEdit ? "编辑失败" : "新增失败", "error"); }
          })
        }
      })
    },
    goBack() { this.$router.go(-1); }
  }
})
</script>