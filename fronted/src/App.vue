<template>
  <main class="app-shell">
    <ListView
      v-if="view === 'list'"
      :rows="rows"
      :loading="loading"
      :filters="filters"
      :options="options"
      :page="page"
      :total="total"
      @search="loadList"
      @reset="resetFilters"
      @view="viewDetail"
      @edit="editDetail"
      @delete="deleteBill"
      @new="newBill"
      @page="changePage"
      @page-size="changePageSize"
    />

    <DetailView
      v-else-if="selectedBill"
      :bill="selectedBill"
      :options="options"
      :mode="detailMode"
      :notify="showToast"
      :confirm-action="confirmDialog"
      @back="returnToList"
      @submitted="returnToList"
      @changed="handleDetailChanged"
    />

    <AppToast :toast="toast" />
    <ConfirmDialog :state="confirmState" @close="closeConfirm" />
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { deleteReimbursement, getReimbursement, listReimbursements } from './api/travelReimbursements'
import { loadMasterData } from './api/masterData'
import { useFeedback } from './composables/useFeedback'
import ListView from './components/ListView.vue'
import DetailView from './components/DetailView.vue'
import AppToast from './components/common/AppToast.vue'
import ConfirmDialog from './components/common/ConfirmDialog.vue'
import { canEditReimbursement, createEmptyBill } from './utils/reimbursement'

const view = ref('list')
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const page = reactive({ pageNo: 1, pageSize: 10 })
const selectedBill = ref(null)
const detailMode = ref('view')
const { toast, confirmState, showToast, confirmDialog, closeConfirm } = useFeedback()

const filters = reactive({
  reimbursementNo: '',
  title: '',
  reason: '',
  reimCompanyId: '',
  reimDepartmentId: '',
  reimburserId: '',
  businessTypeId: ''
})

const options = reactive({
  companies: [],
  departments: [],
  employees: [],
  businessTypes: [],
  cities: [],
  projects: []
})

async function loadOptions() {
  try {
    Object.assign(options, await loadMasterData())
  } catch (error) {
    showToast(`基础数据加载失败：${error.message}`, 'error')
  }
}

async function loadList() {
  loading.value = true
  const params = new URLSearchParams({
    pageNo: String(page.pageNo),
    pageSize: String(page.pageSize)
  })
  Object.entries(filters).forEach(([key, value]) => {
    if (value) params.set(key, value)
  })
  try {
    const data = await listReimbursements(params)
    rows.value = data.records || []
    total.value = data.total || 0
    const maxPage = Math.max(1, Math.ceil(total.value / page.pageSize))
    if (page.pageNo > maxPage) {
      page.pageNo = maxPage
      await loadList()
    }
  } catch (error) {
    showToast(`列表加载失败：${error.message}`, 'error')
  } finally {
    loading.value = false
  }
}

async function loadDetail(reimbursementId) {
  selectedBill.value = await getReimbursement(reimbursementId)
}

function resetFilters() {
  Object.keys(filters).forEach((key) => {
    filters[key] = ''
  })
  page.pageNo = 1
  loadList()
}

async function viewDetail(row) {
  await loadDetail(row.reimbursementId)
  detailMode.value = 'view'
  view.value = 'detail'
}

async function editDetail(row) {
  if (!canEditReimbursement(row)) {
    showToast('只有草稿状态的报销单可以编辑', 'warning')
    return
  }
  await loadDetail(row.reimbursementId)
  detailMode.value = 'edit'
  view.value = 'detail'
}

function newBill() {
  detailMode.value = 'edit'
  selectedBill.value = createEmptyBill()
  view.value = 'detail'
}

async function deleteBill(row) {
  if (!canEditReimbursement(row)) {
    showToast('只有草稿状态的报销单可以删除', 'warning')
    return
  }
  if (!(await confirmDialog(`确定删除报销单 ${row.reimbursementNo} 吗？`))) {
    return
  }
  await deleteReimbursement(row.reimbursementId)
  showToast('删除成功', 'success')
  await loadList()
}

async function handleDetailChanged(reimbursementId) {
  if (reimbursementId) {
    await loadDetail(reimbursementId)
    return
  }
  if (selectedBill.value?.header?.reimbursementId) {
    await loadDetail(selectedBill.value.header.reimbursementId)
  }
}

function changePage(nextPage) {
  page.pageNo = Math.max(1, Number(nextPage || 1))
  loadList()
}

function changePageSize(nextSize) {
  page.pageSize = Number(nextSize || 10)
  page.pageNo = 1
  loadList()
}

async function returnToList() {
  view.value = 'list'
  selectedBill.value = null
  await loadList()
}

onMounted(async () => {
  await loadOptions()
  await loadList()
})
</script>
