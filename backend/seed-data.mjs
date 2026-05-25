// 差旅报销单 - 测试数据填充脚本
// 运行：node seed-data.mjs

const BASE = 'http://localhost:8081/api/v1/travel-reimbursements';

async function api(method, path, body) {
  const url = path.startsWith('http') ? path : `${BASE}${path}`;
  const opts = { method, headers: { 'Content-Type': 'application/json; charset=utf-8' } };
  if (body !== undefined) opts.body = JSON.stringify(body);
  const resp = await fetch(url, opts);
  const json = await resp.json();
  if (json.code !== '0') throw new Error(`${method} ${url} => code=${json.code} msg=${json.message}`);
  return json.data;
}

// --------- 主数据（与 frontend/src/data 一致）---------
const Co_BJ = { id: '1C54557F1782E000', code: '0407', name: '胜意科技北京分公司' };
const Co_SH = { id: '19218A262C976000', code: '0408', name: '胜意科技上海分公司' };
const Co_WH = { id: '1C61686865DA8000', code: '0409', name: '胜意科技武汉分公司' };
const Co_HZ = { id: '1717271D1DA15000', code: '0410', name: '胜意科技杭州分公司' };

const Dp_KH = { id: '13AB8D7B52A9B002', code: '072001', name: '客户成功事业部' };
const Dp_FK = { id: '14515BB4BFB92003', code: '072003', name: '企业费控事业部' };
const Dp_HL = { id: '19D32F9FE9647000', code: '072005', name: '航旅事业部' };
const Dp_YX = { id: '14055D22BB808001', code: '072007', name: '营销事业部' };

const Em_Xu   = { id: '13AB3A3F72409002', code: '74541', name: '徐年年' };
const Em_Zou  = { id: '13AB4A56BB009002', code: '21552', name: '邹薇' };
const Em_Wang = { id: '13AB591FE8009002', code: '80681', name: '王成军' };
const Em_Pan  = { id: '13AB77281A408001', code: '89899', name: '潘展飞' };
const Em_Jia  = { id: '13AB7925EB808001', code: '10503', name: '姜林' };

const Biz_XMCC = { id: '1B5FEB7DD4396000', code: '10010010101', name: '项目出差' };
const Biz_SCTZ = { id: '1A92E43082EFC000', code: '10010010102', name: '市场拓展出差' };
const Biz_SHWH = { id: '13AB3A4154008001', code: '10010010202', name: '售后维护出差' };
const Biz_ZPH  = { id: '13AB3A41AC408001', code: '100100202',   name: '招聘会' };

const Pj_HZ = { id: '1C811ABF96195000', code: 'centralChina', name: '华中客户定制化项目' };
const Pj_HN = { id: '1C5931735AC4A000', code: 'southChina',   name: '华南客户定制化项目' };
const Pj_HB = { id: '1771EC45F2443000', code: 'northChina',   name: '华北客户定制化项目' };
const Pj_HD = { id: '1762792DB4E9A002', code: 'eastChina',    name: '华东客户定制化项目' };
const Pj_No = { id: '12BC248B25083001', code: 'nonProjectRelated', name: '非项目类费用归集' };

const Ci_BJ = { cityNo: '10119', cityName: '北京', cityType: '1' };
const Ci_SH = { cityNo: '10621', cityName: '上海', cityType: '1' };
const Ci_WH = { cityNo: '10458', cityName: '武汉', cityType: '2' };
const Ci_HZ = { cityNo: '10216', cityName: '杭州', cityType: '2' };
const Ci_JZ = { cityNo: '10455', cityName: '荆州', cityType: '3' };

async function main() {
  console.log('====== 差旅报销单 · 测试数据填充 ======\n');

  // ===== 清理旧数据 =====
  const old = await api('GET', '?pageNo=1&pageSize=100');
  for (const r of (old.records ?? [])) {
    try { await api('DELETE', `/${r.reimbursementId}`); }
    catch { /* 非草稿删不掉，跳过 */ }
  }
  console.log(`已清理 ${old.records?.length ?? 0} 条旧数据\n`);

  // ============================================================
  // 1. DRAFT 草稿 - 北京→杭州 项目出差（含行程 + 分摊）
  // ============================================================
  console.log('[1/5] 草稿：北京→杭州 客户实施');
  const b1 = await api('POST', '', {
    title: '北京-杭州客户实施', reason: '杭州客户上线交付 + 培训（2天上线+1天培训）',
    remark: '已与客户预约，预计5/8出发',
    reimCompany: Co_BJ, reimDepartment: Dp_KH, reimburser: Em_Xu, businessType: Biz_XMCC,
    allocations: []
  });
  await api('POST', `/${b1.reimbursementId}/trips`, {
    traveler: Em_Xu, departureCity: Ci_BJ, arrivalCity: Ci_HZ,
    departureDate: '2026-05-08', arrivalDate: '2026-05-10', description: '客户系统上线+培训'
  });
  // 查补助总额，设置分摊
  const tot1 = await api('GET', `/${b1.reimbursementId}/expense-totals`);
  const total1 = tot1.totalAllowanceAmount;
  await api('PUT', `/${b1.reimbursementId}`, {
    title: '北京-杭州客户实施', reason: '杭州客户上线交付 + 培训（2天上线+1天培训）',
    remark: '已与客户预约，预计5/8出发',
    reimCompany: Co_BJ, reimDepartment: Dp_KH, reimburser: Em_Xu, businessType: Biz_XMCC,
    allocations: [
      { rowIndex: 0, reimCompany: Co_BJ, project: Pj_HZ, allocationRatio: 0.7, allocationAmount: +((total1 * 0.7).toFixed(2)) },
      { rowIndex: 1, reimCompany: Co_SH, project: Pj_No, allocationRatio: 0.3, allocationAmount: +(total1 - +(total1 * 0.7).toFixed(2)).toFixed(2) }
    ]
  });
  console.log(`  ✓ ${b1.reimbursementNo} [DRAFT] 补助=${total1}\n`);

  // ============================================================
  // 2. APPROVING 审批中 - 武汉→上海 市场拓展（4天出差）
  // ============================================================
  console.log('[2/5] 审批中：武汉→上海 市场拓展');
  const b2 = await api('POST', '', {
    title: '武汉-上海市场拓展', reason: '拜访华东客户ABC，参加5/15行业峰会',
    reimCompany: Co_WH, reimDepartment: Dp_FK, reimburser: Em_Zou, businessType: Biz_SCTZ,
    allocations: []
  });
  await api('POST', `/${b2.reimbursementId}/trips`, {
    traveler: Em_Zou, departureCity: Ci_WH, arrivalCity: Ci_SH,
    departureDate: '2026-05-13', arrivalDate: '2026-05-16', description: '拜访客户+行业峰会'
  });
  await api('POST', `/${b2.reimbursementId}/submit`);
  console.log(`  ✓ ${b2.reimbursementNo} [APPROVING]\n`);

  // ============================================================
  // 3. APPROVED 已审批 - 上海→北京 售后维护
  // ============================================================
  console.log('[3/5] 已审批：上海→北京 售后维护');
  const b3 = await api('POST', '', {
    title: '北京客户售后维护', reason: '现场处理生产故障',
    reimCompany: Co_BJ, reimDepartment: Dp_HL, reimburser: Em_Wang, businessType: Biz_SHWH,
    allocations: []
  });
  await api('POST', `/${b3.reimbursementId}/trips`, {
    traveler: Em_Wang, departureCity: Ci_SH, arrivalCity: Ci_BJ,
    departureDate: '2026-04-22', arrivalDate: '2026-04-24', description: '现场支持，处理生产故障'
  });
  await api('POST', `/${b3.reimbursementId}/submit`);
  await api('POST', `/${b3.reimbursementId}/approve`);
  console.log(`  ✓ ${b3.reimbursementNo} [APPROVED]\n`);

  // ============================================================
  // 4. COMPLETED 已放款 - 杭州→荆州 项目巡检（多日 + 双分摊）
  // ============================================================
  console.log('[4/5] 已完成：杭州→荆州 项目巡检（已放款）');
  const b4 = await api('POST', '', {
    title: '杭州-荆州项目巡检', reason: 'Q1项目交付巡检',
    reimCompany: Co_SH, reimDepartment: Dp_KH, reimburser: Em_Pan, businessType: Biz_XMCC,
    allocations: []
  });
  await api('POST', `/${b4.reimbursementId}/trips`, {
    traveler: Em_Pan, departureCity: Ci_HZ, arrivalCity: Ci_JZ,
    departureDate: '2026-03-18', arrivalDate: '2026-03-22', description: '荆州现场巡检5天'
  });
  await api('POST', `/${b4.reimbursementId}/submit`);
  await api('POST', `/${b4.reimbursementId}/approve`);
  await api('POST', `/${b4.reimbursementId}/disburse`, { paymentNo: 'PAY202603250001', paymentTime: '2026-03-25T10:30:00' });
  console.log(`  ✓ ${b4.reimbursementNo} [COMPLETED]\n`);

  // ============================================================
  // 5. VOIDED 已作废 - 临时取消的出差
  // ============================================================
  console.log('[5/5] 已作废：上海行业沙龙（临时取消）');
  const b5 = await api('POST', '', {
    title: '上海行业沙龙', reason: '出席5月底行业沙龙',
    reimCompany: Co_BJ, reimDepartment: Dp_YX, reimburser: Em_Jia, businessType: Biz_SCTZ,
    allocations: []
  });
  await api('POST', `/${b5.reimbursementId}/void`, { reason: '客户调整行程，本次差旅取消' });
  console.log(`  ✓ ${b5.reimbursementNo} [VOIDED]\n`);

  // ============================================================
  // 汇总
  // ============================================================
  console.log('====== 当前列表汇总 ======');
  const list = await api('GET', '?pageNo=1&pageSize=20');
  for (const r of (list.records ?? [])) {
    const no = (r.reimbursementNo || '').padEnd(26);
    const st = (r.status || '').padEnd(12);
    const nm = (r.reimburserName || '').padEnd(6);
    const ti = r.title || '';
    const am = r.allowanceAmount ?? 0;
    console.log(`  ${no} ${st} ${nm} ${ti}  补助=${am}`);
  }
  console.log(`\n共 ${list.total} 条数据，前端访问：http://localhost:5173/`);
}

main().catch(err => { console.error('FAILED:', err.message); process.exit(1); });
