<template>
    <div class="kanban-container">
        <div class="kanban-column" v-for="col in columns" :key="col.value" @dragover.prevent
            @drop="onDrop($event, col.value)">
            <div class="column-header" :style="{ background: col.color }">
                <span class="column-title">{{ col.label }}</span>
                <span class="column-count">{{ getColumnCount(col.value) }}</span>
            </div>
            <div class="column-body">
                <div class="kanban-card" v-for="tran in grouped[col.value]" :key="tran.id" draggable="true"
                    @dragstart="onDragStart($event, tran)" @click="$router.push('/dashboard/tran/' + tran.id)">
                    <div class="card-customer">{{ tran.customerDO?.clueDO?.fullName || '--' }}</div>
                    <div class="card-info">
                        <span class="card-money">{{ formatMoney(tran.money) }}</span>
                        <span class="card-date">{{ formatDate(tran.expectedDate) }}</span>
                    </div>
                    <div class="card-tranNo">{{ tran.tranNo }}</div>
                </div>
                <div class="column-empty" v-if="!grouped[col.value]?.length">暂无交易</div>
            </div>
        </div>
    </div>
</template>

<script>
    import { defineComponent } from "vue";
    import { doGet, doPut } from "../http/httpRequest.js";
    import { messageTip } from "../util/util.js";

    export default defineComponent({
        name: "TranKanbanView",

        data() {
            return {
                tranList: [],
                dragTran: null,
                columns: [
                    { label: '创建', value: 1, color: '#6c757d' },
                    { label: '确认清单', value: 2, color: '#0d6efd' },
                    { label: '交付定金', value: 3, color: '#6f42c1' },
                    { label: '产品检验', value: 4, color: '#fd7e14' },
                    { label: '付款成交', value: 42, color: '#198754' },
                    { label: '丢失关闭', value: 0, color: '#dc3545' }
                ]
            }
        },

        computed: {
            grouped() {
                const result = {};
                this.columns.forEach(col => { result[col.value] = []; });
                this.tranList.forEach(tran => {
                    const stage = tran.stage;
                    if (result[stage] !== undefined) {
                        result[stage].push(tran);
                    }
                });
                return result;
            }
        },

        mounted() { this.loadData(); },

        methods: {
            loadData() {
                doGet("/api/trans", { current: 1 }).then(resp => {
                    if (resp.data.code === 200) {
                        this.tranList = resp.data.data.list || [];
                    }
                })
            },

            getColumnCount(stage) {
                return (this.grouped[stage] || []).length;
            },

            onDragStart(event, tran) {
                this.dragTran = tran;
                event.dataTransfer.effectAllowed = 'move';
            },

            onDrop(event, newStage) {
                if (!this.dragTran || this.dragTran.stage === newStage) return;

                const oldStage = this.dragTran.stage;
                this.dragTran.stage = newStage;

                const params = new URLSearchParams();
                params.append('id', this.dragTran.id);
                params.append('customerId', this.dragTran.customerId);
                params.append('money', this.dragTran.money);
                params.append('expectedDate', this.dragTran.expectedDate);
                params.append('stage', newStage);
                if (this.dragTran.description) params.append('description', this.dragTran.description);
                if (this.dragTran.nextContactTime) params.append('nextContactTime', this.dragTran.nextContactTime);

                doPut("/api/tran", params).then(resp => {
                    if (resp.data.code === 200) {
                        messageTip("阶段变更成功", "success");
                    } else {
                        this.dragTran.stage = oldStage;
                        messageTip("阶段变更失败", "error");
                    }
                }).catch(() => {
                    this.dragTran.stage = oldStage;
                    messageTip("阶段变更失败", "error");
                });

                this.dragTran = null;
            },

            formatMoney(val) {
                if (val == null) return '';
                return Number(val).toLocaleString();
            },

            formatDate(val) {
                if (!val) return '';
                return val.substring(0, 10);
            }
        }
    })
</script>

<style scoped>
    .kanban-container {
        display: flex;
        gap: 12px;
        height: calc(100vh - 160px);
        overflow-x: auto;
        padding: 8px 0;
    }

    .kanban-column {
        flex: 1;
        min-width: 220px;
        max-width: 300px;
        background: #f5f6f8;
        border-radius: 8px;
        display: flex;
        flex-direction: column;
        overflow: hidden;
    }

    .column-header {
        padding: 12px 16px;
        color: #fff;
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-weight: 600;
        font-size: 14px;
    }

    .column-count {
        background: rgba(255, 255, 255, 0.3);
        border-radius: 10px;
        padding: 1px 8px;
        font-size: 13px;
    }

    .column-body {
        flex: 1;
        overflow-y: auto;
        padding: 8px;
    }

    .kanban-card {
        background: #fff;
        border-radius: 6px;
        padding: 12px;
        margin-bottom: 8px;
        cursor: grab;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
        transition: transform 0.15s, box-shadow 0.15s;
    }

    .kanban-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
    }

    .kanban-card:active {
        cursor: grabbing;
    }

    .card-customer {
        font-weight: 600;
        font-size: 14px;
        margin-bottom: 6px;
        color: #303133;
    }

    .card-info {
        display: flex;
        justify-content: space-between;
        font-size: 13px;
        margin-bottom: 4px;
    }

    .card-money {
        color: #198754;
        font-weight: 600;
    }

    .card-date {
        color: #909399;
    }

    .card-tranNo {
        font-size: 11px;
        color: #c0c4cc;
        margin-top: 6px;
    }

    .column-empty {
        text-align: center;
        color: #c0c4cc;
        padding: 24px 0;
        font-size: 13px;
    }
</style>