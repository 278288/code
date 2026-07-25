<template>
  <el-button type="success" @click="goBack">返回</el-button>
  <el-form ref="formRef" :model="query" :rules="rules" label-width="100px" style="margin-top: 20px; width: 600px;">
    <el-form-item label="系统编码" prop="systemCode">
      <el-input v-model="query.systemCode" :disabled="query.id > 0"/>
    </el-form-item>
    <el-form-item label="系统名称" prop="name">
      <el-input v-model="query.name"/>
    </el-form-item>
    <el-form-item label="站点地址" prop="site">
      <el-input v-model="query.site"/>
    </el-form-item>
    <el-form-item label="版本">
      <el-input v-model="query.version"/>
    </el-form-item>
    <el-form-item label="标题" prop="title">
      <el-input v-model="query.title"/>
    </el-form-item>
    <el-form-item label="描述" prop="description">
      <el-input v-model="query.description" type="textarea"/>
    </el-form-item>
    <el-form-item label="关键词" prop="keywords">
      <el-input v-model="query.keywords"/>
    </el-form-item>
    <el-form-item label="Logo地址">
      <el-input v-model="query.logo"/>
    </el-form-item>
    <el-form-item label="快捷图标">
      <el-input v-model="query.shortcuticon"/>
    </el-form-item>
    <el-form-item label="电话">
      <el-input v-model="query.tel"/>
    </el-form-item>
    <el-form-item label="微信">
      <el-input v-model="query.weixin"/>
    </el-form-item>
    <el-form-item label="邮箱">
      <el-input v-model="query.email"/>
    </el-form-item>
    <el-form-item label="地址">
      <el-input v-model="query.address"/>
    </el-form-item>
    <el-form-item label="关闭信息">
      <el-input v-model="query.closemsg"/>
    </el-form-item>
    <el-form-item label="是否开启" prop="isopen">
      <el-select v-model="query.isopen" style="width: 100%;">
        <el-option label="开启" value="1"/>
        <el-option label="关闭" value="0"/>
      </el-select>
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
  name: "SystemRecordView",
  data() {
    return {
      query: {},
      rules: {
        systemCode: [{ required: true, message: '请输入系统编码', trigger: 'blur' }],
        name: [{ required: true, message: '请输入系统名称', trigger: 'blur' }],
        site: [{ required: true, message: '请输入站点地址', trigger: 'blur' }],
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
        keywords: [{ required: true, message: '请输入关键词', trigger: 'blur' }],
        isopen: [{ required: true, message: '请选择是否开启', trigger: 'change' }],
      }
    }
  },
  mounted() {
    let id = this.$route.params.id;
    if (id) { this.query.id = parseInt(id); this.loadData(); }
  },
  methods: {
    loadData() {
      doGet("/api/system/" + this.query.id, {}).then(resp => {
        if (resp.data.code === 200) {
          let d = resp.data.data;
          this.query.systemCode = d.systemCode;
          this.query.name = d.name;
          this.query.site = d.site;
          this.query.version = d.version;
          this.query.title = d.title;
          this.query.description = d.description;
          this.query.keywords = d.keywords;
          this.query.logo = d.logo;
          this.query.shortcuticon = d.shortcuticon;
          this.query.tel = d.tel;
          this.query.weixin = d.weixin;
          this.query.email = d.email;
          this.query.address = d.address;
          this.query.closemsg = d.closemsg;
          this.query.isopen = d.isopen;
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
          let req = isEdit ? doPut("/api/system", fd) : doPost("/api/system", fd);
          req.then(resp => {
            if (resp.data.code === 200) {
              messageTip(isEdit ? "编辑成功" : "新增成功", "success");
              this.$router.push("/dashboard/system");
            } else { messageTip(isEdit ? "编辑失败" : "新增失败", "error"); }
          })
        }
      })
    },
    goBack() { this.$router.go(-1); }
  }
})
</script>