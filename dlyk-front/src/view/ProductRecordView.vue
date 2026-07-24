<template>
  <el-button type="success" @click="goBack">返回</el-button>

  <el-form ref="productRefForm" :model="productQuery" :rules="productRules" label-width="120px" style="margin-top: 20px; width: 600px;">
    <el-form-item label="产品名称" prop="name">
      <el-input v-model="productQuery.name"/>
    </el-form-item>
    <el-form-item label="指导起始价" prop="guidePriceS">
      <el-input v-model="productQuery.guidePriceS"/>
    </el-form-item>
    <el-form-item label="指导最高价" prop="guidePriceE">
      <el-input v-model="productQuery.guidePriceE"/>
    </el-form-item>
    <el-form-item label="经销商报价" prop="quotation">
      <el-input v-model="productQuery.quotation"/>
    </el-form-item>
    <el-form-item label="状态" prop="state">
      <el-select v-model="productQuery.state" style="width: 100%;">
        <el-option label="在售" :value="0"/>
        <el-option label="售罄" :value="1"/>
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
  name: "ProductRecordView",

  data() {
    return {
      productQuery: {},
      productRules: {
        name: [
          { required: true, message: '请输入产品名称', trigger: 'blur' }
        ],
        guidePriceS: [
          { required: true, message: '请输入指导起始价', trigger: 'blur' },
          { pattern: /^[0-9]+(\.[0-9]{1,2})?$/, message: '价格格式不正确', trigger: 'blur' }
        ],
        guidePriceE: [
          { required: true, message: '请输入指导最高价', trigger: 'blur' },
          { pattern: /^[0-9]+(\.[0-9]{1,2})?$/, message: '价格格式不正确', trigger: 'blur' }
        ],
        quotation: [
          { required: true, message: '请输入经销商报价', trigger: 'blur' },
          { pattern: /^[0-9]+(\.[0-9]{1,2})?$/, message: '价格格式不正确', trigger: 'blur' }
        ],
        state: [
          { required: true, message: '请选择状态', trigger: 'change' }
        ]
      }
    }
  },

  mounted() {
    let id = this.$route.params.id;
    if (id) {
      this.productQuery.id = parseInt(id);
      this.loadData();
    }
  },

  methods: {
    loadData() {
      doGet("/api/product/" + this.productQuery.id, {}).then(resp => {
        if (resp.data.code === 200) {
          let data = resp.data.data;
          this.productQuery.name = data.name;
          this.productQuery.guidePriceS = data.guidePriceS;
          this.productQuery.guidePriceE = data.guidePriceE;
          this.productQuery.quotation = data.quotation;
          this.productQuery.state = data.state;
        }
      })
    },

    submit() {
      this.$refs.productRefForm.validate((isValid) => {
        if (isValid) {
          let isEdit = this.productQuery.id > 0;
          let formData = new FormData();
          for (let field in this.productQuery) {
            if (this.productQuery[field] !== null && this.productQuery[field] !== undefined && this.productQuery[field] !== '') {
              formData.append(field, this.productQuery[field]);
            }
          }
          let req = isEdit ? doPut("/api/product", formData) : doPost("/api/product", formData);
          req.then(resp => {
            if (resp.data.code === 200) {
              messageTip(isEdit ? "编辑成功" : "新增成功", "success");
              this.$router.push("/dashboard/product");
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