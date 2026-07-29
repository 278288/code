<template>
  <el-form ref="activityRefForm" :model="activityQuery" label-width="110px" :rules="activityRules">
    <el-form-item label="负责人" prop="ownerId">
      <el-select
          v-if="isAdmin || isEditMode"
          v-model="activityQuery.ownerId"
          placeholder="请选择"
          class="width">
        <el-option
            v-for="item in ownerOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"/>
      </el-select>
      <!-- 普通用户录入时：负责人锁定为当前用户，不可修改 -->
      <el-input v-else :model-value="currentUserName" disabled class="width" />
    </el-form-item>

    <el-form-item label="活动名称" prop="name">
      <el-input v-model="activityQuery.name" />
    </el-form-item>

    <el-form-item label="开始时间" prop="startTime">
      <el-date-picker
          v-model="activityQuery.startTime"
          type="datetime"
          placeholder="请选择创建时间"
          value-format="YYYY-MM-DD HH:mm:ss" style="width:100%;"/>
    </el-form-item>

    <el-form-item label="结束时间"  prop="endTime">
      <el-date-picker
          v-model="activityQuery.endTime"
          type="datetime"
          placeholder="请选择创建时间"
          value-format="YYYY-MM-DD HH:mm:ss" style="width:100%;"/>
    </el-form-item>

    <el-form-item label="活动预算" prop="cost">
      <el-input v-model="activityQuery.cost" />
    </el-form-item>

    <el-form-item label="活动描述" prop="description">
      <el-input
          v-model="activityQuery.description"
          :rows="6"
          type="textarea"
          placeholder="请输入活动描述"/>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="activitySubmit">提 交</el-button>
      <el-button @click="goBack">返 回</el-button>
    </el-form-item>

  </el-form>
</template>

<script>
import {defineComponent} from 'vue'
import {doGet, doPost, doPut} from "../http/httpRequest.js";
import {messageTip} from "../util/util.js";

export default defineComponent({
  name: "ActivityRecordView",

  inject : ['reload'],

  data() {
    return {
      activityQuery : {},
      activityRules : {
        ownerId : [
          { required: true, message: '请选择负责人', trigger: 'blur' }
        ],
        name : [
          { required: true, message: '请输入活动名称', trigger: 'blur' }
        ],
        startTime : [
          { required: true, message: '请选择开始时间', trigger: 'blur' }
        ],
        endTime : [
          { required: true, message: '请选择结束时间', trigger: 'blur' }
        ],
        cost : [
          { required: true, message: '请输入活动预算', trigger: 'blur' },
          { pattern : /^[0-9]+(\.[0-9]{2})?$/, message: '活动预算必须是整数或者两位小数', trigger: 'blur'}
        ],
        description : [
          { required: true, message: '请输入活动描述', trigger: 'blur' },
          { min: 5, max: 255, message: '活动描述长度为5-255个字符', trigger: 'blur' }
        ]
      },
      ownerOptions : [{}],
      isAdmin: false,
      isEditMode: false,
      currentUserId: null,
      currentUserName: ''
    }
  },

  mounted() {
    // 先加载当前登录用户信息，再加载负责人列表
    this.loadLoginUser();
    this.loadActivity();
  },

  methods : {
    // 加载当前登录用户，判断是否管理员
    loadLoginUser() {
      doGet("/api/login/info", {}).then(resp => {
        if (resp.data.code === 200) {
          const user = resp.data.data;
          this.currentUserId = user.id;
          this.currentUserName = user.name;
          // 判断是否管理员
          this.isAdmin = user.roleList && user.roleList.includes('admin');
          // 加载负责人列表
          this.loadOwner();
          // 普通用户录入时，默认负责人为当前用户
          if (!this.isAdmin && !this.isEditMode) {
            this.$set(this.activityQuery, 'ownerId', this.currentUserId);
          }
        }
      })
    },

    // 加载负责人
    loadOwner() {
      doGet("/api/owner", {}).then(resp => {
        if (resp.data.code === 200)  {
          this.ownerOptions = resp.data.data;
        }
      })
    },

    goBack() {
      this.$router.go(-1);
    },

    activitySubmit()  {
      let formData = new FormData();
      for (let field in this.activityQuery) {
        console.log(field  + " -- " + this.activityQuery[field])
        if (this.activityQuery[field])  {
          formData.append(field, this.activityQuery[field]);
        }
      }
      // 普通用户录入时，确保负责人为当前用户
      if (!this.isAdmin && !this.isEditMode) {
        formData.set('ownerId', this.currentUserId);
      }
      this.$refs.activityRefForm.validate( (isValid) => {
        if (isValid) {
          if (this.activityQuery.id > 0) { /*编辑*/
            doPut("/api/activity", formData).then(resp => {
              if (resp.data.code === 200) {
                messageTip("编辑成功", "success");
                this.$router.push("/dashboard/activity");
              } else {
                messageTip("编辑失败", "error");
              }
            })
          } else  {
            doPost("/api/activity", formData).then(resp => {/*新增*/
              if (resp.data.code === 200) {
                messageTip("提交成功", "success");
                this.$router.push("/dashboard/activity");
              } else {
                messageTip("提交失败", "error");
              }
            })
          }
        }
      })
    },

    loadActivity() {
      let id =  this.$route.params.id;
      if (id) {
        this.isEditMode = true;
        doGet("/api/activity/" + id, {}).then(resp => {
          if (resp.data.code === 200) {
            this.activityQuery = resp.data.data;
          }
        })
      }
    }
  }
})
</script>

<style scoped>
.width {
  width: 100%;
}
</style>
