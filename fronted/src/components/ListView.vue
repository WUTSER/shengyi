<template>
  <section class="list-page">
    <form class="filter-bar list-filter-bar" @submit.prevent="loadList">
      <label>
        <span>报销单号</span>
        <input v-model.trim="filters.reimbursementNo" placeholder="请输入" />
      </label>
      <label>
        <span>标题</span>
        <input v-model.trim="filters.title" placeholder="请输入" />
      </label>
      <label>
        <span>事由</span>
        <input v-model.trim="filters.reason" placeholder="请输入" />
      </label>
      <label>
        <span>费用归属公司</span>
        <select v-model="filters.reimCompanyId">
          <option value="">请选择</option>
          <option v-for="item in options.companies" :key="item.reimCompanyId" :value="item.reimCompanyId">
            {{ item.reimCompanyName }}
          </option>
        </select>
      </label>
      <label>
        <span>报销部门</span>
        <select v-model="filters.reimDepartmentId">
          <option value="">请选择</option>
          <option v-for="item in options.departments" :key="item.reimDepartmentId" :value="item.reimDepartmentId">
            {{ item.reimDepartmentName }}
          </option>
        </select>
      </label>
      <label>
        <span>报销人</span>
        <select v-model="filters.reimburserId">
          <option value="">请选择</option>
          <option v-for="item in options.employees" :key="item.reimburserId" :value="item.reimburserId">
            {{ item.reimburserName }}
          </option>
        </select>
      </label>
      <label>
        <span>业务类型</span>
        <select v-model="filters.businessTypeId">
          <option value="">请选择</option>
          <option v-for="item in leafBusinessTypes" :key="item.businessTypeId" :value="item.businessTypeId">
            {{ item.businessTypeName }}
          </option>
        </select>
      </label>
      <label>
        <span>单据状态</span>
        <select v-model="filters.status">
          <option value="">请选择</option>
          <option v-for="(text, code) in STATUS_MAP" :key="code" :value="code">
            {{ text }}
          </option>
        </select>
      </label>
      <div class="filter-actions">
        <button class="btn ghost" type="button" @click="newBill">新增</button>
        <button class="btn ghost" type="button" @click="resetFilters">清除</button>
        <button class="btn primary" type="submit">搜索</button>
      </div>
    </form>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th class="index-col"><SlidersHorizontal :size="15" /></th>
            <th>操作</th>
            <th>报销单号</th>
            <th>单据状态</th>
            <th>单据类型</th>
            <th>报销人</th>
            <th>报销部门</th>
            <th>费用归属公司</th>
            <th>业务类型</th>
            <th>报销标题</th>
            <th>报销事由</th>
            <th class="money-cell">补助金额</th>
            <th>创建时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="13" class="empty-cell">加载中...</td>
          </tr>
          <tr v-for="(row, index) in rows" v-else :key="row.reimbursementId">
            <td class="index-col">{{ (page.pageNo - 1) * page.pageSize + index + 1 }}</td>
            <td>
              <div class="row-actions action-cell">
                <button title="查看" @click="viewDetail(row)"><FileText :size="14" /></button>
                <button title="编辑" :disabled="!canModify(row)" @click="editDetail(row)"><Pencil :size="14" /></button>
                <div class="more-actions">
                  <button title="更多" class="more-btn" @mouseenter="showMoreMenu(row.reimbursementNo)" @mouseleave="hideMoreMenu()"><MoreHorizontal :size="14" /></button>
                  <div v-show="moreMenuOpen === row.reimbursementNo" class="more-menu" @mouseenter="showMoreMenu(row.reimbursementNo)" @mouseleave="hideMoreMenu()">
                    <button @click="confirmDelete(row)">删除</button>
                    <button @click="manualPush(row)">手工推送</button>
                    <button @click="copyBill(row)">复制</button>
                  </div>
                </div>
              </div>
            </td>
            <td><button class="link" @click="viewDetail(row)">{{ row.reimbursementNo }}</button></td>
            <td><span class="status-tag" :class="'status-' + row.status">{{ row.statusName }}</span></td>
            <td>{{ row.billTypeName }}</td>
            <td>{{ row.reimburserName }}[{{ row.reimburserNo }}]</td>
            <td>[{{ row.reimDepartmentNo }}]{{ row.reimDepartmentName }}</td>
            <td class="ellipsis">{{ row.reimCompanyName }}</td>
            <td>{{ row.businessTypeName }}</td>
            <td><button class="link ellipsis inline-link" @click="viewDetail(row)">{{ row.title }}</button></td>
            <td class="ellipsis">{{ row.reason }}</td>
            <td class="money-cell">{{ formatMoney(row.allowanceAmount) }}</td>
            <td>{{ formatDate(row.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <footer class="pager">
      <span>共{{ total || 0 }}条</span>
      <select :value="page.pageSize" @change="changePageSize($event.target.value)">
        <option v-for="size in pageSizes" :key="size" :value="size">{{ size }}条/页</option>
      </select>
      <button :disabled="(Number(page.pageNo) || 1) <= 1" @click="changePage(page.pageNo - 1)"><ChevronLeft :size="15" /></button>
      <template v-for="item in pagerItems" :key="item.key">
        <span v-if="item.ellipsis" class="pager-ellipsis">...</span>
        <button
          v-else
          class="page-number"
          :class="{ active: item.page === (Number(page.pageNo) || 1) }"
          type="button"
          @click="changePage(item.page)"
        >
          {{ item.page }}
        </button>
      </template>
      <button :disabled="(Number(page.pageNo) || 1) >= totalPages" @click="changePage(page.pageNo + 1)"><ChevronRight :size="15" /></button>
      <span>前往</span>
      <input
        v-model.number="jumpPage"
        class="page-input"
        type="number"
        min="1"
        :max="totalPages"
        @keyup.enter="jumpToPage"
      />
      <button class="page-go" type="button" @click="jumpToPage">确定</button>
      <span>页</span>
    </footer>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ChevronLeft,
  ChevronRight,
  FileText,
  MoreHorizontal,
  Pencil,
  SlidersHorizontal,
  Trash2
} from 'lucide-vue-next'
import { formatDate, formatMoney } from '../utils/formatters'
import { canEditReimbursement, STATUS_MAP } from '../utils/reimbursement'
import { useMasterData } from '../stores/masterData'
import { useFeedback } from '../composables/useFeedback'
import { deleteReimbursement, listReimbursements, submitReimbursement } from '../api/travelReimbursements'

const router = useRouter()
const { options } = useMasterData()
const { showToast, confirmDialog } = useFeedback()

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const page = reactive({ pageNo: 1, pageSize: 10 })
const filters = reactive({
  reimbursementNo: '',
  title: '',
  reason: '',
  reimCompanyId: '',
  reimDepartmentId: '',
  reimburserId: '',
  businessTypeId: '',
  status: ''
})

const pageSizes = [10, 20, 50]
const jumpPage = ref(Number(page.pageNo))
const moreMenuOpen = ref(null)
const moreMenuTimer = ref(null)

const totalPages = computed(() => {
  const totalNum = Number(total.value) || 0
  const pageSizeNum = Number(page.pageSize) || 10
  return Math.max(1, Math.ceil(totalNum / pageSizeNum))
})
const pagerItems = computed(() => {
  const current = Math.max(1, Math.min(Number(page.pageNo) || 1, totalPages.value))
  const last = totalPages.value
  const pages = new Set([1, last])

  for (let pageNo = current - 1; pageNo <= current + 1; pageNo += 1) {
    if (pageNo >= 1 && pageNo <= last) pages.add(pageNo)
  }

  const sorted = [...pages].sort((a, b) => a - b)
  const items = []
  sorted.forEach((pageNo, index) => {
    const previous = sorted[index - 1]
    if (previous && pageNo - previous > 1) {
      items.push({ key: `ellipsis-${previous}-${pageNo}`, ellipsis: true })
    }
    items.push({ key: `page-${pageNo}`, page: pageNo })
  })
  return items
})

watch(
  () => page.pageNo,
  (value) => {
    jumpPage.value = Number(value) || 1
  }
)

const leafBusinessTypes = computed(() =>
  (options.businessTypes || []).filter((item) => !item.thereSubordinateNode || item.thereSubordinateNode === '0')
)

async function loadList() {
  loading.value = true
  const params = new URLSearchParams({
    pageNo: String(Math.max(1, Number(page.pageNo) || 1)),
    pageSize: String(Math.max(1, Number(page.pageSize) || 10))
  })
  Object.entries(filters).forEach(([key, value]) => {
    if (value) params.set(key, value)
  })
  try {
    const data = await listReimbursements(params)
    rows.value = data.records || []
    total.value = Number(data.total) || 0
    const maxPage = Math.max(1, Math.ceil(total.value / (Number(page.pageSize) || 10)))
    const currentPage = Number(page.pageNo) || 1
    if (currentPage > maxPage) {
      page.pageNo = maxPage
      await loadList()
    }
  } catch (error) {
    showToast(`列表加载失败：${error.message}`, 'error')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  Object.keys(filters).forEach((key) => {
    filters[key] = ''
  })
  page.pageNo = 1
  loadList()
}

function viewDetail(row) {
  router.push({ name: 'reimbursement-detail', params: { id: row.reimbursementId } })
}

function editDetail(row) {
  if (!canEditReimbursement(row)) {
    showToast('只有草稿状态的报销单可以编辑', 'warning')
    return
  }
  router.push({ name: 'reimbursement-edit', params: { id: row.reimbursementId } })
}

function newBill() {
  router.push({ name: 'reimbursement-create' })
}

function showMoreMenu(reimbursementNo) {
  if (moreMenuTimer.value) {
    clearTimeout(moreMenuTimer.value)
    moreMenuTimer.value = null
  }
  moreMenuOpen.value = reimbursementNo
}

function hideMoreMenu() {
  moreMenuTimer.value = setTimeout(() => {
    moreMenuOpen.value = null
    moreMenuTimer.value = null
  }, 200)
}

function confirmDelete(row) {
  if (!canEditReimbursement(row)) {
    showToast('只有草稿状态的报销单可以删除', 'warning')
    moreMenuOpen.value = null
    return
  }
  deleteBill(row)
}

async function deleteBill(row) {
  if (!(await confirmDialog('确认删除？'))) {
    moreMenuOpen.value = null
    return
  }
  await deleteReimbursement(row.reimbursementId)
  showToast('删除成功', 'success')
  moreMenuOpen.value = null
  await loadList()
}

async function manualPush(row) {
  if (!(await confirmDialog('确认手工推送该报销单并提交审批？'))) {
    moreMenuOpen.value = null
    return
  }
  try {
    await submitReimbursement(row.reimbursementId)
    showToast(`报销单 ${row.reimbursementNo} 已提交审批`, 'success')
    await loadList()
  } catch (error) {
    showToast(`手工推送失败：${error.message}`, 'error')
  } finally {
    moreMenuOpen.value = null
  }
}

async function copyBill(row) {
  try {
    await navigator.clipboard.writeText(row.reimbursementNo)
    showToast(`已复制单号：${row.reimbursementNo}`, 'success')
  } catch (error) {
    showToast('复制失败', 'error')
  } finally {
    moreMenuOpen.value = null
  }
}

function canModify(row) {
  return canEditReimbursement(row)
}

function changePage(nextPage) {
  const nextPageNum = Number(nextPage) || 1
  const pageNo = Math.min(Math.max(nextPageNum, 1), totalPages.value)
  if (pageNo !== (Number(page.pageNo) || 1)) {
    page.pageNo = pageNo
    loadList()
  }
}

function changePageSize(size) {
  page.pageSize = Math.max(1, Number(size) || 10)
  page.pageNo = 1
  loadList()
}

function jumpToPage() {
  changePage(jumpPage.value)
}

onMounted(() => {
  loadList()
})

</script>
