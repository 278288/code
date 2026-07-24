<template>
  <el-button type="success" @click="goBack">返回</el-button>
  <el-form ref="formRef" :model="query" :rules="rules" label-width="100px" style="margin-top: 20px; width: 500px;">
    <el-form-item label="类型代码" prop="typeCode">
      <el-input v-model="query.typeCode" :disabled="query.id > 0"/>
    </el-form-item>
    <el-form-item label="类型名称" prop="typeName">
      <el-input v-model="query.typeName"/>
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
  name: "DicTypeRecordView",
  data() {
    return {
      query: {},
      rules: {
        typeCode: [{ required: true, message: '请输入类型代码', trigger: 'blur' }],
        typeName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
      }
    }
  },
  mounted() {
    let id = this.$route.params.id;
    if (id) { this.query.id = parseInt(id); this.loadData(); }
  },
  methods: {
    loadData() {
      doGet("/api/dictype/" + this.query.id, {}).then(resp => {
        if (resp.data.code === 200) {
          let d = resp.data.data;
          this.query.typeCode = d.typeCode;
          this.query.typeName = d.typeName;
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
          let req = isEdit ? doPut("/api/dictype", fd) : doPost("/api/dictype", fd);
          req.then(resp => {
            if (resp.data.code === 200) {
              messageTip(isEdit ? "编辑成功" : "新增成功", "success");
              this.$router.push("/dashboard/dictype");
            } else { messageTip(isEdit ? "编辑失败" : "新增失败", "error"); }
          })
        }
      })
    },
    goBack() { this.$router.go(-1); }
  }
})
</script>