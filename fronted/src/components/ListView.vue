<template>
  <section class="list-page">
    <form class="filter-bar list-filter-bar" @submit.prevent="$emit('search')">
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
      <div class="filter-actions">
        <button class="btn ghost" type="button" @click="$emit('new')">新增</button>
        <button class="btn ghost" type="button" @click="$emit('reset')">清除</button>
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
                <button title="查看" @click="$emit('view', row)"><FileText :size="14" /></button>
                <button title="编辑" :disabled="!canModify(row)" @click="$emit('edit', row)"><Pencil :size="14" /></button>
                <button title="删除" :disabled="!canModify(row)" @click="$emit('delete', row)"><Trash2 :size="14" /></button>
              </div>
            </td>
            <td><button class="link" @click="$emit('view', row)">{{ row.reimbursementNo }}</button></td>
            <td><span class="status-link">{{ row.statusName }}</span></td>
            <td>{{ row.billTypeName }}</td>
            <td>{{ row.reimburserName }}[{{ row.reimburserNo }}]</td>
            <td>[{{ row.reimDepartmentNo }}]{{ row.reimDepartmentName }}</td>
            <td class="ellipsis">{{ row.reimCompanyName }}</td>
            <td>{{ row.businessTypeName }}</td>
            <td><button class="link ellipsis inline-link" @click="$emit('view', row)">{{ row.title }}</button></td>
            <td class="ellipsis">{{ row.reason }}</td>
            <td class="money-cell">{{ formatMoney(row.allowanceAmount) }}</td>
            <td>{{ formatDate(row.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <footer class="pager">
      <span>共{{ total }}条</span>
      <select :value="page.pageSize" @change="changePageSize($event.target.value)">
        <option v-for="size in pageSizes" :key="size" :value="size">{{ size }}条/页</option>
      </select>
      <button :disabled="page.pageNo <= 1" @click="changePage(page.pageNo - 1)"><ChevronLeft :size="15" /></button>
      <template v-for="item in pagerItems" :key="item.key">
        <span v-if="item.ellipsis" class="pager-ellipsis">...</span>
        <button
          v-else
          class="page-number"
          :class="{ active: item.page === page.pageNo }"
          type="button"
          @click="changePage(item.page)"
        >
          {{ item.page }}
        </button>
      </template>
      <button :disabled="page.pageNo >= totalPages" @click="changePage(page.pageNo + 1)"><ChevronRight :size="15" /></button>
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
import { computed, ref, watch } from 'vue'
import {
  ChevronLeft,
  ChevronRight,
  FileText,
  Pencil,
  SlidersHorizontal,
  Trash2
} from 'lucide-vue-next'
import { formatDate, formatMoney } from '../utils/formatters'
import { canEditReimbursement } from '../utils/reimbursement'

const props = defineProps({
  rows: { type: Array, required: true },
  loading: { type: Boolean, required: true },
  filters: { type: Object, required: true },
  options: { type: Object, required: true },
  page: { type: Object, required: true },
  total: { type: Number, required: true }
})

const emit = defineEmits(['search', 'reset', 'view', 'edit', 'delete', 'new', 'page', 'pageSize'])

const pageSizes = [10, 20, 50]
const jumpPage = ref(props.page.pageNo)

const totalPages = computed(() => Math.max(1, Math.ceil(Number(props.total || 0) / Number(props.page.pageSize || 10))))
const pagerItems = computed(() => {
  const current = Number(props.page.pageNo || 1)
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
  () => props.page.pageNo,
  (value) => {
    jumpPage.value = value
  }
)

const leafBusinessTypes = computed(() =>
  (props.options.businessTypes || []).filter((item) => !item.thereSubordinateNode || item.thereSubordinateNode === '0')
)

function canModify(row) {
  return canEditReimbursement(row)
}

function changePage(nextPage) {
  const pageNo = Math.min(Math.max(Number(nextPage || 1), 1), totalPages.value)
  if (pageNo !== props.page.pageNo) emit('page', pageNo)
}

function changePageSize(size) {
  emit('pageSize', Number(size))
}

function jumpToPage() {
  changePage(jumpPage.value)
}

</script>
