export function canEditReimbursement(row) {
  return row?.status === '0' || row?.status === 0 || row?.availableActions?.includes('EDIT')
}

export function createEmptyHeader() {
  return {
    reimbursementId: '',
    reimbursementNo: '',
    billDate: new Date().toISOString().slice(0, 10),
    status: '0',
    statusName: '草稿',
    title: '',
    reason: '',
    reimburserId: '',
    reimburserNo: '',
    reimburserName: '',
    reimDepartmentId: '',
    reimDepartmentNo: '',
    reimDepartmentName: '',
    reimCompanyId: '',
    reimCompanyNo: '',
    reimCompanyName: '',
    businessTypeId: '',
    businessTypeNo: '',
    businessTypeName: '',
    remark: ''
  }
}

export function createEmptyBill() {
  return {
    header: createEmptyHeader(),
    trips: [],
    allowances: [],
    splits: [],
    totals: {
      totalAllowanceAmount: 0,
      mealAllowanceAmount: 0,
      trafficAllowanceAmount: 0,
      communicationAllowanceAmount: 0
    },
    availableActions: ['EDIT', 'DELETE', 'SUBMIT', 'VOID']
  }
}

export const STATUS_MAP = {
  '0': '草稿',
  '1': '审批中',
  '2': '已完成',
  '3': '已作废'
}
