import { createRouter, createWebHistory } from "vue-router";
import { doGet } from "../http/httpRequest.js";

let router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', component : () => import('../view/LoginView.vue') },
        { path: '/dashboard', component : () => import('../view/DashboardView.vue'),
            children : [
                { path: '', component : () => import('../view/StatisticView.vue') },
                { path: 'user', component : () => import('../view/UserView.vue') },
                { path: 'user/:id', component : () => import('../view/UserDetailView.vue') },
                { path: 'activity', component : () => import('../view/ActivityView.vue') },
                { path: 'activity/add', component : () => import('../view/ActivityRecordView.vue') },
                { path: 'activity/edit/:id', component : () => import('../view/ActivityRecordView.vue') },
                { path: 'activity/:id', component : () => import('../view/ActivityDetailView.vue') },
                { path: 'clue', component : () => import('../view/ClueView.vue') },
                { path: 'clue/add', component : () => import('../view/ClueRecordView.vue') },
                { path: 'clue/edit/:id', component : () => import('../view/ClueRecordView.vue') },
                { path: 'clue/detail/:id', component : () => import('../view/ClueDetailView.vue') },
                { path: 'customer', component : () => import('../view/CustomerView.vue') },
                { path: 'customer/:id', component : () => import('../view/CustomerDetailView.vue') },
                { path: 'tran', component : () => import('../view/TranView.vue') },
                { path: 'tran/add', component : () => import('../view/TranRecordView.vue') },
                { path: 'tran/edit/:id', component : () => import('../view/TranRecordView.vue') },
                { path: 'tran/:id', component : () => import('../view/TranDetailView.vue') },
                { path: 'product', component : () => import('../view/ProductView.vue') },
                { path: 'product/add', component : () => import('../view/ProductRecordView.vue') },
                { path: 'product/edit/:id', component : () => import('../view/ProductRecordView.vue') },
                { path: 'dictype', component : () => import('../view/DicTypeView.vue') },
                { path: 'dictype/add', component : () => import('../view/DicTypeRecordView.vue') },
                { path: 'dictype/edit/:id', component : () => import('../view/DicTypeRecordView.vue') },
                { path: 'dicvalue', component : () => import('../view/DicValueView.vue') },
                { path: 'dicvalue/add', component : () => import('../view/DicValueRecordView.vue') },
                { path: 'dicvalue/edit/:id', component : () => import('../view/DicValueRecordView.vue') },
                { path: 'system', component : () => import('../view/SystemView.vue') },
                { path: 'system/add', component : () => import('../view/SystemRecordView.vue') },
                { path: 'system/edit/:id', component : () => import('../view/SystemRecordView.vue') },
                { path: 'perm', component : () => import('../view/PermissionManageView.vue') },
            ]
        },
        { path: '/hello', component : () => import('../components/HelloWorld.vue') }
    ]
})

// 权限管理页面仅 admin 可进入（后端接口同样有 hasAuthority('admin') 校验，这里是前端兜底）
router.beforeEach(async (to, from, next) => {
    if (to.path === '/dashboard/perm') {
        try {
            const resp = await doGet('/api/login/info', {});
            const user = resp.data && resp.data.data;
            const roles = (user && user.roleList) || [];
            if (roles.includes('admin')) {
                next();
            } else {
                next('/dashboard');
            }
        } catch (e) {
            next('/');
        }
    } else {
        next();
    }
});

export default router;
