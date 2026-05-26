<template>
  <section class="detail-page">
    <header class="detail-header">
      <h1>差旅费用报销单</h1>
      <div>提单日期&nbsp;&nbsp;{{ headerForm.billDate }}</div>
    </header>

    <div class="detail-canvas">
      <SectionPanel title="基础信息">
        <div class="basic-grid">
          <label class="wide-field">
            <span>报销标题</span>
            <input v-model.trim="headerForm.title" maxlength="500" placeholder="请输入" :disabled="isReadonly" />
          </label>
          <label>
            <span>报销人</span>
            <select v-model="headerForm.reimburserId" :disabled="isReadonly">
              <option value="">请选择</option>
              <option v-for="item in options.employees" :key="item.reimburserId" :value="item.reimburserId">
                {{ item.reimburserName }}
              </option>
            </select>
          </label>
          <label>
            <span>报销部门</span>
            <select v-model="headerForm.reimDepartmentId" :disabled="isReadonly">
              <option value="">请选择</option>
              <option v-for="item in options.departments" :key="item.reimDepartmentId" :value="item.reimDepartmentId">
                {{ item.reimDepartmentName }}
              </option>
            </select>
          </label>
          <label>
            <span>费用归属公司</span>
            <select v-model="headerForm.reimCompanyId" :disabled="isReadonly">
              <option value="">请选择</option>
              <option v-for="item in options.companies" :key="item.reimCompanyId" :value="item.reimCompanyId">
                {{ item.reimCompanyName }}
              </option>
            </select>
          </label>
          <label>
            <span>业务类型</span>
            <select v-model="headerForm.businessTypeId" :disabled="isReadonly">
              <option value="">请选择</option>
              <option v-for="item in leafBusinessTypes" :key="item.businessTypeId" :value="item.businessTypeId">
                {{ item.businessTypeName }}
              </option>
            </select>
          </label>
          <label class="wide-field">
            <span>出差事由</span>
            <textarea v-model.trim="headerForm.reason" maxlength="500" placeholder="请输入" :disabled="isReadonly"></textarea>
          </label>
        </div>
      </SectionPanel>

      <SectionPanel title="补录行程" :action-text="isReadonly ? '' : '补录行程'" @action="openTripModal('create')">
        <table class="section-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>出行人员</th>
              <th>出差日期</th>
              <th>行程</th>
              <th>行程说明</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="trips.length === 0">
              <td colspan="6" class="empty-cell">暂无补录行程</td>
            </tr>
            <tr v-for="(trip, index) in trips" :key="trip.tripId">
              <td>{{ index + 1 }}</td>
              <td>{{ trip.travelerName }}</td>
              <td>{{ formatRange(trip.departureDate, trip.arrivalDate) }}</td>
              <td>{{ trip.route }}</td>
              <td>{{ trip.description }}</td>
              <td>
                <div v-if="!isReadonly" class="row-actions table-actions">
                  <button title="删除" @click="deleteTrip(trip)"><Trash2 :size="14" /></button>
                  <button title="编辑" @click="openTripModal('edit', trip)"><Pencil :size="14" /></button>
                  <button title="复制" @click="openTripModal('copy', trip)"><Copy :size="14" /></button>
                </div>
                <span v-else class="muted-cell">-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </SectionPanel>

      <SectionPanel title="补助信息" :subtitle="allowanceSubtitle">
        <div class="notice">
          <Info :size="15" />
          请根据实际出差日期选择补助，出差期间当日有用餐安排的请自行核减当日餐补，出差期间当日有用车的请自行核减当日交补
        </div>
        <table class="section-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>出行人</th>
              <th>出差日期</th>
              <th>补助天数</th>
              <th>行程</th>
              <th>补助城市</th>
              <th>申请金额</th>
              <th>补助金额</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="allowances.length === 0">
              <td colspan="9" class="empty-cell">暂无补助信息</td>
            </tr>
            <tr v-for="(item, index) in allowances" :key="item.allowanceId">
              <td>{{ index + 1 }}</td>
              <td>{{ item.travelerName }}</td>
              <td>{{ item.travelDateRange }}</td>
              <td>{{ item.allowanceDays }}</td>
              <td>{{ item.route }}</td>
              <td>{{ item.allowanceCityName }}</td>
              <td>{{ formatMoney(item.applyAmount) }}</td>
              <td>{{ formatMoney(item.allowanceAmount) }}</td>
              <td>
                <button class="icon-link" :title="isReadonly ? '查看补助' : '编辑补助'" @click="openAllowanceModal(item)">
                  <Pencil :size="14" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </SectionPanel>

      <ExpenseTotalsSection :totals="totals" />

      <ExpenseSplitSection
        :totals="totals"
        :rows="splitRows"
        :options="options"
        :is-readonly="isReadonly"
        :first-ratio="firstSplitRatio"
        :ratio-total="splitRatioTotal"
        :amount-total="splitAmountTotal"
        :split-amount="splitAmount"
        @add="addSplitRow"
        @delete="deleteSplitRow"
        @average="averageSplitRows"
        @ratio-input="changeSplitRatio"
        @ratio-blur="normalizeSplitRatio"
      />

      <SectionPanel title="备注信息" :action-text="isReadonly ? '' : '删除备注'" @action="headerForm.remark = ''">
        <textarea
          v-model.trim="headerForm.remark"
          class="remark-box"
          maxlength="1000"
          placeholder="请输入"
          :disabled="isReadonly"
        ></textarea>
      </SectionPanel>
    </div>

    <footer class="detail-footer">
      <button class="btn ghost" @click="returnToList">关闭</button>
      <button v-if="!isReadonly" class="btn ghost" @click="saveBill">保存</button>
      <button v-if="!isReadonly" class="btn primary" @click="submitBill">提交</button>
      <button v-if="isApproving" class="btn ghost" @click="withdrawBill">撤回</button>
    </footer>

    <div v-if="tripModal.visible" class="modal-mask">
      <form class="trip-modal" @submit.prevent="saveTrip">
        <header class="modal-header">
          <h2>补录行程</h2>
          <button type="button" title="关闭" @click="closeTripModal"><X :size="18" /></button>
        </header>
        <div class="modal-body">
          <div class="warning">
            <Info :size="16" />
            <span>
              仅可补录未从申请单带入或未产生费用的行程信息<br />
              跨天跨城行程填写说明：出发城市-到达城市：武汉-北京；出发日期-到达日期：1号-5号；1号~5号补助按北京匹配。
            </span>
          </div>

          <label>
            <span>出行人<i>*</i></span>
            <select v-model="tripForm.travelerId">
              <option value="">请选择</option>
              <option v-for="item in options.employees" :key="item.reimburserId" :value="item.reimburserId">
                {{ item.reimburserName }}
              </option>
            </select>
          </label>
          <label>
            <span>出发城市<i>*</i></span>
            <select v-model="tripForm.departureCityNo">
              <option value="">请选择</option>
              <option v-for="item in options.cities" :key="item.cityNo" :value="item.cityNo">
                {{ item.cityName }}
              </option>
            </select>
          </label>
          <label>
            <span>到达城市<i>*</i></span>
            <select v-model="tripForm.arrivalCityNo">
              <option value="">请选择</option>
              <option v-for="item in options.cities" :key="item.cityNo" :value="item.cityNo">
                {{ item.cityName }}
              </option>
            </select>
          </label>
          <label>
            <span>出发到达日期<i>*</i></span>
            <div class="date-range">
              <input v-model="tripForm.departureDate" type="date" :max="today" />
              <em>-</em>
              <input v-model="tripForm.arrivalDate" type="date" :min="tripForm.departureDate" :max="today" />
            </div>
          </label>
          <label class="textarea-row">
            <span>行程说明<i>*</i></span>
            <textarea v-model.trim="tripForm.description" maxlength="500"></textarea>
          </label>
          <p v-if="formError" class="form-error">{{ formError }}</p>
        </div>
        <footer class="modal-footer">
          <button class="btn ghost" type="button" @click="closeTripModal">取消</button>
          <button class="btn primary" type="submit" :disabled="saving">{{ saving ? '保存中' : '保存' }}</button>
        </footer>
      </form>
    </div>

    <div v-if="allowanceModal.visible" class="allowance-modal-mask">
      <section class="allowance-dialog">
        <header class="allowance-header">
          <h2>补助日历</h2>
          <button type="button" title="关闭" @click="closeAllowanceModal"><X :size="16" /></button>
        </header>

        <div class="allowance-type-row">
          <span>出差类型</span>
          <b>{{ headerBusinessTypeName }}</b>
          <label>
            <input
              type="checkbox"
              :checked="isAllCalendarChecked"
              :disabled="isReadonly"
              @change="toggleAllCalendar($event.target.checked)"
            />
            全选
          </label>
        </div>

        <div class="allowance-body">
          <aside class="allowance-side">
            <div class="trip-card">
              <div class="trip-date-row">
                <span>开始日期</span>
                <b>{{ allowanceTrip?.departureDate || '-' }}</b>
              </div>
              <div class="trip-line">
                <span></span>
                <i></i>
                <em></em>
              </div>
              <div class="trip-route-row">
                <strong>{{ allowanceRouteStart }}</strong>
                <span>{{ selectedAllowance?.allowanceDays || 0 }}天</span>
                <strong>{{ allowanceRouteEnd }}</strong>
              </div>
              <div class="trip-date-row">
                <span>结束日期</span>
                <b>{{ allowanceTrip?.arrivalDate || '-' }}</b>
              </div>
            </div>

            <div class="amount-card">
              <div>
                <span>补助金额</span>
                <em>CNY</em>
                <b>{{ formatMoney(calendarAllowanceAmount) }}</b>
              </div>
              <div>
                <span>标准总额</span>
                <em>CNY</em>
                <b>{{ formatMoney(calendarStandardAmount) }}</b>
              </div>
              <div>
                <span>补助总额</span>
                <em>CNY</em>
                <b>{{ formatMoney(calendarAllowanceAmount) }}</b>
              </div>
            </div>
          </aside>

          <section class="calendar-panel">
            <div class="calendar-title">出差补助</div>
            <div v-if="loading" class="calendar-state">加载中...</div>
            <div v-else-if="calendarLoading" class="calendar-state">加载中...</div>
            <div v-else-if="calendarError" class="calendar-state error">{{ calendarError }}</div>
            <div v-else class="calendar-scroll">
              <table class="calendar-table">
              <thead>
                <tr>
                  <th>出差日期</th>
                  <th>补助城市</th>
                  <th>
                    <span>餐费补助</span>
                    <input
                      type="checkbox"
                      :checked="isColumnChecked('meal')"
                      :disabled="isReadonly"
                      @change="toggleColumn('meal', $event.target.checked)"
                    />
                  </th>
                  <th>
                    <span>交通补助</span>
                    <input
                      type="checkbox"
                      :checked="isColumnChecked('traffic')"
                      :disabled="isReadonly"
                      @change="toggleColumn('traffic', $event.target.checked)"
                    />
                  </th>
                  <th>
                    <span>通讯补助</span>
                    <input
                      type="checkbox"
                      :checked="isColumnChecked('communication')"
                      :disabled="isReadonly"
                      @change="toggleColumn('communication', $event.target.checked)"
                    />
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in calendarRows" :key="row.calendarId">
                  <td class="date-cell">
                    <div>{{ row.travelDate }}</div>
                    <span>{{ row.weekDay }}</span>
                    <input
                      type="checkbox"
                      :checked="isRowChecked(row)"
                      :disabled="isReadonly"
                      @change="toggleRow(row, $event.target.checked)"
                    />
                    <MapPin :size="13" />
                  </td>
                  <td>{{ row.allowanceCityName }}</td>
                  <td>
                    <div class="allowance-item-cell">
                      <span>CNY {{ formatMoney(row.meal.standardAmount) }} / 天</span>
                      <label>
                        <input
                          type="checkbox"
                          v-model="row.meal.checked"
                          :disabled="isReadonly"
                          @change="normalizeItem(row.meal)"
                        />
                        <input
                          v-model.number="row.meal.amount"
                          type="number"
                          min="0"
                          step="0.01"
                          :max="row.meal.standardAmount"
                          :disabled="isReadonly || !row.meal.checked"
                          @input="clampItem(row.meal)"
                        />
                      </label>
                    </div>
                  </td>
                  <td>
                    <div class="allowance-item-cell">
                      <span>CNY {{ formatMoney(row.traffic.standardAmount) }} / 天</span>
                      <label>
                        <input
                          type="checkbox"
                          v-model="row.traffic.checked"
                          :disabled="isReadonly"
                          @change="normalizeItem(row.traffic)"
                        />
                        <input
                          v-model.number="row.traffic.amount"
                          type="number"
                          min="0"
                          step="0.01"
                          :max="row.traffic.standardAmount"
                          :disabled="isReadonly || !row.traffic.checked"
                          @input="clampItem(row.traffic)"
                        />
                      </label>
                    </div>
                  </td>
                  <td>
                    <div class="allowance-item-cell">
                      <span>CNY {{ formatMoney(row.communication.standardAmount) }} / 天</span>
                      <label>
                        <input
                          type="checkbox"
                          v-model="row.communication.checked"
                          :disabled="isReadonly"
                          @change="normalizeItem(row.communication)"
                        />
                        <input
                          v-model.number="row.communication.amount"
                          type="number"
                          min="0"
                          step="0.01"
                          :max="row.communication.standardAmount"
                          :disabled="isReadonly || !row.communication.checked"
                          @input="clampItem(row.communication)"
                        />
                      </label>
                    </div>
                  </td>
                </tr>
              </tbody>
              </table>
            </div>
          </section>
        </div>

        <footer class="allowance-footer">
          <button class="btn ghost" type="button" @click="closeAllowanceModal">取消</button>
          <button v-if="!isReadonly" class="btn primary" type="button" :disabled="calendarSaving" @click="saveAllowanceCalendar">
            {{ calendarSaving ? '确认中' : '确认' }}
          </button>
        </footer>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Copy, Info, MapPin, Pencil, Trash2, X } from 'lucide-vue-next'
import {
  createReimbursement,
  createTrip,
  deleteTrip as deleteTripApi,
  submitReimbursement,
  withdrawReimbursement,
  updateReimbursement,
  updateTrip,
  getReimbursement
} from '../api/travelReimbursements'
import { useAllowanceCalendar } from '../composables/useAllowanceCalendar'
import { useExpenseSplit } from '../composables/useExpenseSplit'
import { formatMoney, formatRange } from '../utils/formatters'
import { createEmptyBill, createEmptyHeader } from '../utils/reimbursement'
import ExpenseSplitSection from './detail/ExpenseSplitSection.vue'
import ExpenseTotalsSection from './detail/ExpenseTotalsSection.vue'
import SectionPanel from './SectionPanel.vue'
import { useMasterData } from '../stores/masterData'
import { useFeedback } from '../composables/useFeedback'

const props = defineProps({
  mode: { type: String, default: 'edit' }
})

const router = useRouter()
const route = useRoute()
const { options } = useMasterData()
const { showToast, confirmDialog } = useFeedback()

const loading = ref(false)
const bill = ref(createEmptyBill())
const headerForm = reactive(createEmptyHeader())
const tripModal = reactive({ visible: false, mode: 'create', tripId: '' })
const tripForm = reactive({
  travelerId: '',
  departureCityNo: '',
  arrivalCityNo: '',
  departureDate: '',
  arrivalDate: '',
  description: ''
})
const formError = ref('')
const saving = ref(false)
const today = new Date().toISOString().slice(0, 10)

const trips = computed(() => bill.value.trips || [])
const allowances = computed(() => bill.value.allowances || [])
const savedSplits = computed(() => bill.value.splits || [])
const totals = computed(() => bill.value.totals || {})
const leafBusinessTypes = computed(() =>
  (options.businessTypes || []).filter((item) => !item.thereSubordinateNode || item.thereSubordinateNode === '0')
)
const allowanceSubtitle = computed(() => {
  const days = allowances.value.reduce((sum, item) => sum + Number(item.allowanceDays || 0), 0)
  return `${formatMoney(totals.value.totalAllowanceAmount)}（${days}天）`
})
const headerBusinessTypeName = computed(() => {
  const businessType = options.businessTypes.find((item) => item.businessTypeId === headerForm.businessTypeId)
  return businessType?.businessTypeName || headerForm.businessTypeName || '-'
})
const isDraft = computed(() => headerForm.status === '0' || headerForm.status === 0 || !headerForm.reimbursementId)
const isApproving = computed(() => headerForm.status === '1' || headerForm.status === 1)
const isReadonly = computed(() => {
  if (props.mode === 'view') return true
  if (props.mode === 'edit' && isDraft.value) return false
  return !isDraft.value
})

const {
  splitRows,
  firstSplitRatio,
  splitRatioTotal,
  splitAmountTotal,
  addSplitRow,
  deleteSplitRow,
  averageSplitRows,
  changeSplitRatio,
  normalizeSplitRatio,
  splitAmount,
  validateSplitRows,
  toSaveSplits
} = useExpenseSplit(headerForm, totals, savedSplits, isReadonly, showToast, confirmDialog)

const {
  allowanceModal,
  selectedAllowance,
  calendarRows,
  calendarLoading,
  calendarSaving,
  calendarError,
  allowanceTrip,
  allowanceRouteStart,
  allowanceRouteEnd,
  calendarStandardAmount,
  calendarAllowanceAmount,
  isAllCalendarChecked,
  openAllowanceModal,
  closeAllowanceModal,
  saveAllowanceCalendar,
  isRowChecked,
  isColumnChecked,
  toggleAllCalendar,
  toggleRow,
  toggleColumn,
  normalizeItem,
  clampItem
} = useAllowanceCalendar(headerForm, trips, isReadonly, { emit: () => {} })

watch(
  () => bill.value.header,
  (header) => {
    Object.assign(headerForm, createEmptyHeader(), header || {})
  },
  { immediate: true }
)

async function loadDetail(reimbursementId) {
  loading.value = true
  try {
    bill.value = await getReimbursement(reimbursementId)
  } catch (error) {
    showToast(`加载详情失败：${error.message}`, 'error')
  } finally {
    loading.value = false
  }
}

function openTripModal(mode, trip = null) {
  tripModal.visible = true
  tripModal.mode = mode
  tripModal.tripId = mode === 'edit' ? trip?.tripId || '' : ''
  formError.value = ''

  Object.assign(tripForm, {
    travelerId: trip?.travelerId || headerForm.reimburserId || '',
    departureCityNo: trip?.departureCityNo || '',
    arrivalCityNo: trip?.arrivalCityNo || '',
    departureDate: trip?.departureDate || '',
    arrivalDate: trip?.arrivalDate || '',
    description: trip?.description || ''
  })
}

function closeTripModal() {
  tripModal.visible = false
  formError.value = ''
}

async function saveBill() {
  try {
    const reimbursementId = await ensureBillSaved()
    showToast('保存成功', 'success')
    await loadDetail(reimbursementId)
  } catch (error) {
    showToast(error.message, 'error')
  }
}

async function saveTrip() {
  const validation = validateTripForm()
  if (validation) {
    formError.value = validation
    return
  }

  saving.value = true
  try {
    const reimbursementId = await ensureBillSaved()
    if (tripModal.mode === 'edit') {
      await updateTrip(reimbursementId, tripModal.tripId, { ...tripForm })
    } else {
      await createTrip(reimbursementId, { ...tripForm })
    }
    closeTripModal()
    await loadDetail(reimbursementId)
  } catch (error) {
    formError.value = error.message
  } finally {
    saving.value = false
  }
}

async function deleteTrip(trip) {
  if (!(await confirmDialog('确定删除当前补录行程吗？删除后将同步删除关联补助信息和补助日历。'))) {
    return
  }
  await deleteTripApi(headerForm.reimbursementId, trip.tripId)
  await loadDetail(headerForm.reimbursementId)
}

async function submitBill() {
  try {
    validateHeaderForm()
    validateSplitRows()
    const reimbursementId = await ensureBillSaved()
    await submitReimbursement(reimbursementId)
    showToast('提交成功', 'success')
    router.push({ name: 'reimbursement-list' })
  } catch (error) {
    showToast(error.message, 'error')
  }
}

async function withdrawBill() {
  try {
    if (!(await confirmDialog('确认撤回该报销单？撤回后将恢复为草稿状态'))) {
      return
    }
    await withdrawReimbursement(headerForm.reimbursementId)
    showToast('撤回成功', 'success')
    await loadDetail(headerForm.reimbursementId)
  } catch (error) {
    showToast(error.message, 'error')
  }
}

async function ensureBillSaved() {
  validateHeaderForm()
  const payload = {
    billDate: headerForm.billDate,
    title: headerForm.title,
    reason: headerForm.reason,
    reimburserId: headerForm.reimburserId,
    reimDepartmentId: headerForm.reimDepartmentId,
    reimCompanyId: headerForm.reimCompanyId,
    businessTypeId: headerForm.businessTypeId,
    remark: headerForm.remark || '',
    splits: toSaveSplits()
  }

  if (!headerForm.reimbursementId) {
    const created = await createReimbursement(payload)
    headerForm.reimbursementId = created.reimbursementId
    return created.reimbursementId
  }

  await updateReimbursement(headerForm.reimbursementId, payload)
  return headerForm.reimbursementId
}

function validateHeaderForm() {
  if (!headerForm.title) throw new Error('报销标题不能为空')
  if (!headerForm.reason) throw new Error('出差事由不能为空')
  if (!headerForm.reimburserId) throw new Error('报销人不能为空')
  if (!headerForm.reimDepartmentId) throw new Error('报销部门不能为空')
  if (!headerForm.reimCompanyId) throw new Error('费用归属公司不能为空')
  if (!headerForm.businessTypeId) throw new Error('业务类型不能为空')
}

function validateTripForm() {
  if (!tripForm.travelerId) return '出行人不能为空'
  if (!tripForm.departureCityNo) return '出发城市不能为空'
  if (!tripForm.arrivalCityNo) return '到达城市不能为空'
  if (!tripForm.departureDate) return '出发日期不能为空'
  if (!tripForm.arrivalDate) return '到达日期不能为空'
  if (!tripForm.description) return '行程说明不能为空'
  if (tripForm.description.length > 500) return '行程说明不能超过500字'
  if (tripForm.arrivalDate < tripForm.departureDate) return '到达日期不能早于出发日期'
  if (tripForm.arrivalDate > today) return '到达日期不能晚于当前日期'

  const duplicated = trips.value.some((trip) => {
    if (tripModal.mode === 'edit' && trip.tripId === tripModal.tripId) return false
    return (
      trip.travelerId === tripForm.travelerId &&
      rangesOverlap(trip.departureDate, trip.arrivalDate, tripForm.departureDate, tripForm.arrivalDate)
    )
  })
  return duplicated ? '同一出行人的行程日期范围不能重复或重叠' : ''
}

function rangesOverlap(startA, endA, startB, endB) {
  return endA >= startB && endB >= startA
}

function returnToList() {
  router.push({ name: 'reimbursement-list' })
}

onMounted(async () => {
  if (route.name === 'reimbursement-detail' && route.params.id) {
    await loadDetail(route.params.id)
  }
})
</script>
