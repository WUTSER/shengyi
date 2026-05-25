package com.shengyi.backend.service;

import com.shengyi.backend.common.BusinessException;
import com.shengyi.backend.common.PageResponse;
import com.shengyi.backend.dto.Dtos.AllowanceItemResponse;
import com.shengyi.backend.dto.Dtos.AllowanceItemSaveRequest;
import com.shengyi.backend.dto.Dtos.AllowanceResponse;
import com.shengyi.backend.dto.Dtos.CalendarResponse;
import com.shengyi.backend.dto.Dtos.CalendarSaveItem;
import com.shengyi.backend.dto.Dtos.CalendarSaveRequest;
import com.shengyi.backend.dto.Dtos.CopyTripRequest;
import com.shengyi.backend.dto.Dtos.ExpenseTotalsResponse;
import com.shengyi.backend.dto.Dtos.ExpenseSplitResponse;
import com.shengyi.backend.dto.Dtos.ExpenseSplitSaveRequest;
import com.shengyi.backend.dto.Dtos.PaymentRequest;
import com.shengyi.backend.dto.Dtos.ReimbursementCreatedResponse;
import com.shengyi.backend.dto.Dtos.ReimbursementDetailResponse;
import com.shengyi.backend.dto.Dtos.ReimbursementHeader;
import com.shengyi.backend.dto.Dtos.ReimbursementListItem;
import com.shengyi.backend.dto.Dtos.ReimbursementQuery;
import com.shengyi.backend.dto.Dtos.ReimbursementSaveRequest;
import com.shengyi.backend.dto.Dtos.RemarkRequest;
import com.shengyi.backend.dto.Dtos.TripRequest;
import com.shengyi.backend.dto.Dtos.TripResponse;
import com.shengyi.backend.dto.Dtos.VoidRequest;
import com.shengyi.backend.model.Models.AllowanceCalendar;
import com.shengyi.backend.model.Models.AllowanceItem;
import com.shengyi.backend.model.Models.BusinessType;
import com.shengyi.backend.model.Models.City;
import com.shengyi.backend.model.Models.Employee;
import com.shengyi.backend.model.Models.ExpenseSplit;
import com.shengyi.backend.model.Models.Project;
import com.shengyi.backend.model.Models.ReimCompany;
import com.shengyi.backend.model.Models.ReimDepartment;
import com.shengyi.backend.model.Models.TravelAllowance;
import com.shengyi.backend.model.Models.TravelReimbursement;
import com.shengyi.backend.model.Models.TravelTrip;
import com.shengyi.backend.model.ReimbursementStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class ReimbursementService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal TRAFFIC_STANDARD = BigDecimal.valueOf(40);
    private static final BigDecimal COMMUNICATION_STANDARD = BigDecimal.valueOf(40);

    private final MasterDataService masterDataService;
    private final JdbcTemplate jdbcTemplate;
    private final Map<String, TravelReimbursement> reimbursements = new LinkedHashMap<>();
    private final AtomicLong reimbursementSequence = new AtomicLong(1);
    private final AtomicLong tripSequence = new AtomicLong(1);
    private final AtomicLong allowanceSequence = new AtomicLong(1);
    private final AtomicLong calendarSequence = new AtomicLong(1);
    private final AtomicLong splitSequence = new AtomicLong(1);

    public ReimbursementService(MasterDataService masterDataService, JdbcTemplate jdbcTemplate) {
        this.masterDataService = masterDataService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public synchronized PageResponse<ReimbursementListItem> list(ReimbursementQuery query) {
        int pageNo = query.pageNo() == null || query.pageNo() < 1 ? 1 : query.pageNo();
        int pageSize = query.pageSize() == null || query.pageSize() < 1 ? 20 : query.pageSize();
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> args = new ArrayList<>();

        appendLike(where, args, "r.reimbursement_no", query.reimbursementNo());
        appendLike(where, args, "r.title", query.title());
        appendLike(where, args, "r.reason", query.reason());
        appendEquals(where, args, "r.reim_company_id", query.reimCompanyId());
        appendEquals(where, args, "r.reim_department_id", query.reimDepartmentId());
        appendEquals(where, args, "r.reimburser_id", query.reimburserId());
        appendEquals(where, args, "r.business_type_id", query.businessTypeId());
        if (query.status() != null) {
            where.append(" AND r.status = ?");
            args.add(ReimbursementStatus.fromStorage(query.status()).getCode());
        }

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM travel_reimbursement r" + where,
                Long.class,
                args.toArray()
        );

        List<Object> listArgs = new ArrayList<>(args);
        listArgs.add((pageNo - 1) * pageSize);
        listArgs.add(pageSize);

        List<ReimbursementListItem> records = jdbcTemplate.query("""
                        SELECT
                            r.reimbursement_id,
                            r.reimbursement_no,
                            r.status,
                            r.reimburser_id,
                            e.reimburser_no,
                            e.reimburser_name,
                            r.reim_department_id,
                            d.reim_department_no,
                            d.reim_department_name,
                            r.reim_company_id,
                            c.reim_company_name,
                            r.business_type_id,
                            b.business_type_name,
                            r.title,
                            r.reason,
                            COALESCE(a.allowance_amount, 0) AS allowance_amount,
                            r.created_at
                        FROM travel_reimbursement r
                        JOIN employee e ON e.reimburser_id = r.reimburser_id
                        JOIN reim_department d ON d.reim_department_id = r.reim_department_id
                        JOIN reim_company c ON c.reim_company_id = r.reim_company_id
                        JOIN business_type b ON b.business_type_id = r.business_type_id
                        LEFT JOIN (
                            SELECT reimbursement_id, SUM(allowance_amount) AS allowance_amount
                            FROM travel_allowance
                            GROUP BY reimbursement_id
                        ) a ON a.reimbursement_id = r.reimbursement_id
                        """ + where + """

                        ORDER BY r.created_at DESC
                        LIMIT ?, ?
                        """,
                (rs, rowNum) -> {
                    ReimbursementStatus status = ReimbursementStatus.fromStorage(rs.getString("status"));
                    return new ReimbursementListItem(
                            rs.getString("reimbursement_id"),
                            rs.getString("reimbursement_no"),
                            status,
                            status.getText(),
                            "日常报销单",
                            rs.getString("reimburser_id"),
                            rs.getString("reimburser_no"),
                            rs.getString("reimburser_name"),
                            rs.getString("reim_department_id"),
                            rs.getString("reim_department_no"),
                            rs.getString("reim_department_name"),
                            rs.getString("reim_company_id"),
                            rs.getString("reim_company_name"),
                            rs.getString("business_type_id"),
                            rs.getString("business_type_name"),
                            rs.getString("title"),
                            rs.getString("reason"),
                            rs.getBigDecimal("allowance_amount"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            availableActions(status)
                    );
                },
                listArgs.toArray()
        );

        return new PageResponse<>(pageNo, pageSize, total == null ? 0 : total, records);
    }

    public synchronized ReimbursementCreatedResponse create(ReimbursementSaveRequest request) {
        validateMasterData(request);

        LocalDateTime now = LocalDateTime.now();
        long idValue = reimbursementSequence.getAndIncrement();

        TravelReimbursement reimbursement = new TravelReimbursement();
        reimbursement.setReimbursementId("RB%s%04d".formatted(System.currentTimeMillis(), idValue));
        reimbursement.setReimbursementNo("CLBX%s%04d".formatted(String.valueOf(System.currentTimeMillis()), idValue));
        reimbursement.setStatus(ReimbursementStatus.DRAFT);
        reimbursement.setCreatedAt(now);
        reimbursement.setUpdatedAt(now);
        applyRequest(reimbursement, request);

        reimbursements.put(reimbursement.getReimbursementId(), reimbursement);
        insertReimbursement(reimbursement);
        saveExpenseSplits(reimbursement, request.splits());
        return new ReimbursementCreatedResponse(
                reimbursement.getReimbursementId(),
                reimbursement.getReimbursementNo(),
                reimbursement.getStatus()
        );
    }

    public synchronized ReimbursementDetailResponse detail(String reimbursementId) {
        return toDetail(requireReimbursement(reimbursementId));
    }

    public synchronized ReimbursementDetailResponse update(String reimbursementId, ReimbursementSaveRequest request) {
        validateMasterData(request);
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        requireDraft(reimbursement);

        applyRequest(reimbursement, request);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        updateReimbursement(reimbursement);
        saveExpenseSplits(reimbursement, request.splits());
        return toDetail(reimbursement);
    }

    public synchronized void delete(String reimbursementId) {
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        requireDraft(reimbursement);
        deleteReimbursementRows(reimbursementId);
        reimbursements.remove(reimbursementId);
    }

    public synchronized ReimbursementDetailResponse submit(String reimbursementId) {
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        requireStatus(reimbursement, ReimbursementStatus.DRAFT, "仅草稿状态允许提交");
        validateBeforeSubmit(reimbursement);
        reimbursement.setStatus(ReimbursementStatus.APPROVING);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        updateReimbursement(reimbursement);
        return toDetail(reimbursement);
    }

    public synchronized ReimbursementDetailResponse withdraw(String reimbursementId) {
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        requireStatus(reimbursement, ReimbursementStatus.APPROVING, "仅审批中状态允许撤回");
        reimbursement.setStatus(ReimbursementStatus.DRAFT);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        updateReimbursement(reimbursement);
        return toDetail(reimbursement);
    }

    public synchronized ReimbursementDetailResponse voidBill(String reimbursementId, VoidRequest request) {
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        if (reimbursement.getStatus() == ReimbursementStatus.VOIDED) {
            throw BusinessException.validation("当前状态不允许作废");
        }
        reimbursement.setStatus(ReimbursementStatus.VOIDED);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        updateReimbursement(reimbursement);
        return toDetail(reimbursement);
    }

    public synchronized ReimbursementDetailResponse approve(String reimbursementId) {
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        requireStatus(reimbursement, ReimbursementStatus.APPROVING, "仅审批中状态允许审批");
        reimbursement.setStatus(ReimbursementStatus.COMPLETED);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        updateReimbursement(reimbursement);
        return toDetail(reimbursement);
    }

    public synchronized ReimbursementDetailResponse disburse(String reimbursementId, PaymentRequest request) {
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        requireStatus(reimbursement, ReimbursementStatus.COMPLETED, "仅已完成状态允许记录放款信息");
        reimbursement.setPaymentTime(request.paymentTime());
        reimbursement.setPaymentNo(request.paymentNo());
        reimbursement.setStatus(ReimbursementStatus.COMPLETED);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        updateReimbursement(reimbursement);
        return toDetail(reimbursement);
    }

    public synchronized TripResponse addTrip(String reimbursementId, TripRequest request) {
        TravelReimbursement reimbursement = requireEditableReimbursement(reimbursementId);
        validateTripRequest(request, null, reimbursement);

        TravelTrip trip = new TravelTrip();
        trip.setTripId("TRIP%s%04d".formatted(System.currentTimeMillis(), tripSequence.getAndIncrement()));
        trip.setReimbursementId(reimbursementId);
        applyTripRequest(trip, request);

        reimbursement.getTrips().add(trip);
        insertTrip(trip);
        rebuildAllowanceForTrip(reimbursement, trip);
        recalculateExpenseSplitAmounts(reimbursement);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        touchReimbursement(reimbursement);
        return toTripResponse(trip);
    }

    public synchronized TripResponse updateTrip(String reimbursementId, String tripId, TripRequest request) {
        TravelReimbursement reimbursement = requireEditableReimbursement(reimbursementId);
        TravelTrip trip = requireTrip(reimbursement, tripId);
        validateTripRequest(request, tripId, reimbursement);

        applyTripRequest(trip, request);
        updateTripRow(trip);
        rebuildAllowanceForTrip(reimbursement, trip);
        recalculateExpenseSplitAmounts(reimbursement);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        touchReimbursement(reimbursement);
        return toTripResponse(trip);
    }

    public synchronized void deleteTrip(String reimbursementId, String tripId) {
        TravelReimbursement reimbursement = requireEditableReimbursement(reimbursementId);
        TravelTrip trip = requireTrip(reimbursement, tripId);
        reimbursement.getTrips().remove(trip);
        reimbursement.getAllowances().removeIf(allowance -> allowance.getTripId().equals(tripId));
        deleteAllowanceRowsForTrip(tripId);
        jdbcTemplate.update("DELETE FROM travel_trip WHERE trip_id = ?", tripId);
        recalculateExpenseSplitAmounts(reimbursement);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        touchReimbursement(reimbursement);
    }

    public synchronized TripResponse copyTrip(String reimbursementId, String tripId, CopyTripRequest request) {
        TravelReimbursement reimbursement = requireEditableReimbursement(reimbursementId);
        TravelTrip source = requireTrip(reimbursement, tripId);
        TripRequest copied = new TripRequest(
                source.getTravelerId(),
                source.getDepartureCityNo(),
                source.getArrivalCityNo(),
                request.departureDate(),
                request.arrivalDate(),
                source.getDescription()
        );
        return addTrip(reimbursementId, copied);
    }

    public synchronized List<AllowanceResponse> allowances(String reimbursementId) {
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        return reimbursement.getAllowances().stream().map(this::toAllowanceResponse).toList();
    }

    public synchronized List<CalendarResponse> calendars(String reimbursementId, String allowanceId) {
        TravelAllowance allowance = requireAllowance(requireReimbursement(reimbursementId), allowanceId);
        return allowance.getCalendars().stream().map(this::toCalendarResponse).toList();
    }

    public synchronized List<CalendarResponse> saveCalendar(
            String reimbursementId,
            String allowanceId,
            CalendarSaveRequest request
    ) {
        TravelReimbursement reimbursement = requireEditableReimbursement(reimbursementId);
        TravelAllowance allowance = requireAllowance(reimbursement, allowanceId);

        for (CalendarSaveItem item : request.items()) {
            AllowanceCalendar calendar = allowance.getCalendars()
                    .stream()
                    .filter(value -> value.getCalendarId().equals(item.calendarId()))
                    .findFirst()
                    .orElseThrow(() -> BusinessException.notFound("补助日历不存在: " + item.calendarId()));
            updateItem(calendar.getMeal(), item.meal(), "餐费补助");
            updateItem(calendar.getTraffic(), item.traffic(), "交通补助");
            updateItem(calendar.getCommunication(), item.communication(), "通讯补助");
        }

        recalculateAllowanceAmount(allowance);
        upsertAllowance(allowance);
        jdbcTemplate.update("DELETE FROM allowance_calendar WHERE allowance_id = ?", allowance.getAllowanceId());
        for (AllowanceCalendar calendar : allowance.getCalendars()) {
            insertCalendar(allowance.getAllowanceId(), calendar);
        }
        recalculateExpenseSplitAmounts(reimbursement);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        touchReimbursement(reimbursement);
        return allowance.getCalendars().stream().map(this::toCalendarResponse).toList();
    }

    public synchronized ExpenseTotalsResponse totals(String reimbursementId) {
        return calculateTotals(requireReimbursement(reimbursementId));
    }

    public synchronized ReimbursementDetailResponse updateRemark(String reimbursementId, RemarkRequest request) {
        TravelReimbursement reimbursement = requireEditableReimbursement(reimbursementId);
        reimbursement.setRemark(request.remark());
        reimbursement.setUpdatedAt(LocalDateTime.now());
        updateReimbursement(reimbursement);
        return toDetail(reimbursement);
    }

    public synchronized ReimbursementDetailResponse deleteRemark(String reimbursementId) {
        TravelReimbursement reimbursement = requireEditableReimbursement(reimbursementId);
        reimbursement.setRemark(null);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        updateReimbursement(reimbursement);
        return toDetail(reimbursement);
    }

    private void validateMasterData(ReimbursementSaveRequest request) {
        masterDataService.requireEmployee(request.reimburserId());
        masterDataService.requireDepartment(request.reimDepartmentId());
        masterDataService.requireCompany(request.reimCompanyId());
        masterDataService.requireBusinessType(request.businessTypeId());
        if (request.splits() != null) {
            for (ExpenseSplitSaveRequest split : request.splits()) {
                masterDataService.requireCompany(split.reimCompanyId());
                if (!isBlank(split.projectId())) {
                    masterDataService.requireProject(split.projectId());
                }
            }
        }
    }

    private void applyRequest(TravelReimbursement reimbursement, ReimbursementSaveRequest request) {
        reimbursement.setBillDate(request.billDate());
        reimbursement.setTitle(request.title());
        reimbursement.setReason(request.reason());
        reimbursement.setReimburserId(request.reimburserId());
        reimbursement.setReimDepartmentId(request.reimDepartmentId());
        reimbursement.setReimCompanyId(request.reimCompanyId());
        reimbursement.setBusinessTypeId(request.businessTypeId());
        reimbursement.setRemark(request.remark());
    }

    private void applyTripRequest(TravelTrip trip, TripRequest request) {
        trip.setTravelerId(request.travelerId());
        trip.setDepartureCityNo(request.departureCityNo());
        trip.setArrivalCityNo(request.arrivalCityNo());
        trip.setDepartureDate(request.departureDate());
        trip.setArrivalDate(request.arrivalDate());
        trip.setDescription(request.description());
    }

    private void validateTripRequest(TripRequest request, String currentTripId, TravelReimbursement reimbursement) {
        masterDataService.requireEmployee(request.travelerId());
        masterDataService.requireCity(request.departureCityNo());
        masterDataService.requireCity(request.arrivalCityNo());

        if (request.arrivalDate().isBefore(request.departureDate())) {
            throw BusinessException.validation("到达日期不能早于出发日期");
        }
        if (request.arrivalDate().isAfter(LocalDate.now())) {
            throw BusinessException.validation("到达日期不能晚于当前日期");
        }

        for (TravelTrip existing : reimbursement.getTrips()) {
            if (Objects.equals(existing.getTripId(), currentTripId)) {
                continue;
            }
            if (existing.getTravelerId().equals(request.travelerId())
                    && rangesOverlap(existing.getDepartureDate(), existing.getArrivalDate(),
                    request.departureDate(), request.arrivalDate())) {
                throw BusinessException.conflict("同一出行人的行程日期范围不能重复或重叠");
            }
        }
    }

    private boolean rangesOverlap(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB) {
        return !endA.isBefore(startB) && !endB.isBefore(startA);
    }

    private void validateBeforeSubmit(TravelReimbursement reimbursement) {
        if (isBlank(reimbursement.getTitle())
                || isBlank(reimbursement.getReason())
                || isBlank(reimbursement.getReimburserId())
                || isBlank(reimbursement.getReimDepartmentId())
                || isBlank(reimbursement.getReimCompanyId())
                || isBlank(reimbursement.getBusinessTypeId())) {
            throw BusinessException.validation("基本信息必填项不能为空");
        }
        if (reimbursement.getTrips().isEmpty()) {
            throw BusinessException.validation("至少需要一条补录行程");
        }

        for (TravelTrip trip : reimbursement.getTrips()) {
            TripRequest request = new TripRequest(
                    trip.getTravelerId(),
                    trip.getDepartureCityNo(),
                    trip.getArrivalCityNo(),
                    trip.getDepartureDate(),
                    trip.getArrivalDate(),
                    trip.getDescription()
            );
            validateTripRequest(request, trip.getTripId(), reimbursement);
        }

        for (TravelAllowance allowance : reimbursement.getAllowances()) {
            for (AllowanceCalendar calendar : allowance.getCalendars()) {
                validateItem(calendar.getMeal(), "餐费补助");
                validateItem(calendar.getTraffic(), "交通补助");
                validateItem(calendar.getCommunication(), "通讯补助");
            }
        }
        validateExpenseSplits(reimbursement.getExpenseSplits());
    }

    private void saveExpenseSplits(TravelReimbursement reimbursement, List<ExpenseSplitSaveRequest> requests) {
        List<ExpenseSplitSaveRequest> source = requests == null || requests.isEmpty()
                ? List.of(new ExpenseSplitSaveRequest(reimbursement.getReimCompanyId(), null, BigDecimal.ONE))
                : requests;
        validateExpenseSplitRequests(source);

        BigDecimal totalAmount = calculateTotals(reimbursement).totalAllowanceAmount();
        List<ExpenseSplit> nextSplits = new ArrayList<>();
        BigDecimal usedAmount = ZERO;
        for (int index = 0; index < source.size(); index++) {
            ExpenseSplitSaveRequest item = source.get(index);
            ExpenseSplit split = new ExpenseSplit();
            split.setSplitId("SPL%s%04d".formatted(System.currentTimeMillis(), splitSequence.getAndIncrement()));
            split.setReimbursementId(reimbursement.getReimbursementId());
            split.setSequenceNo(index + 1);
            split.setReimCompanyId(item.reimCompanyId());
            split.setProjectId(isBlank(item.projectId()) ? null : item.projectId());
            split.setRatio(item.ratio().setScale(4, RoundingMode.HALF_UP));
            if (index == source.size() - 1) {
                split.setAmount(totalAmount.subtract(usedAmount).max(ZERO));
            } else {
                BigDecimal amount = totalAmount.multiply(split.getRatio()).setScale(2, RoundingMode.HALF_UP);
                split.setAmount(amount);
                usedAmount = usedAmount.add(amount);
            }
            nextSplits.add(split);
        }

        reimbursement.getExpenseSplits().clear();
        reimbursement.getExpenseSplits().addAll(nextSplits);
        replaceExpenseSplitRows(reimbursement.getReimbursementId(), nextSplits);
    }

    private void validateExpenseSplitRequests(List<ExpenseSplitSaveRequest> splits) {
        if (splits.isEmpty()) {
            throw BusinessException.validation("至少保留一条分摊信息");
        }
        BigDecimal ratioTotal = ZERO;
        for (ExpenseSplitSaveRequest split : splits) {
            if (isBlank(split.reimCompanyId())) {
                throw BusinessException.validation("费用归属不能为空");
            }
            if (split.ratio() == null
                    || split.ratio().compareTo(ZERO) < 0
                    || split.ratio().compareTo(BigDecimal.ONE) > 0) {
                throw BusinessException.validation("分摊比例必须在0到100%之间");
            }
            ratioTotal = ratioTotal.add(split.ratio());
        }
        if (ratioTotal.compareTo(BigDecimal.ONE) != 0) {
            throw BusinessException.validation("分摊比例合计必须为100%");
        }
    }

    private void validateExpenseSplits(List<ExpenseSplit> splits) {
        if (splits.isEmpty()) {
            throw BusinessException.validation("至少保留一条分摊信息");
        }
        BigDecimal ratioTotal = ZERO;
        for (ExpenseSplit split : splits) {
            ratioTotal = ratioTotal.add(split.getRatio());
        }
        if (ratioTotal.compareTo(BigDecimal.ONE.setScale(4, RoundingMode.HALF_UP)) != 0) {
            throw BusinessException.validation("分摊比例合计必须为100%");
        }
    }

    private void recalculateExpenseSplitAmounts(TravelReimbursement reimbursement) {
        if (reimbursement.getExpenseSplits().isEmpty()) {
            return;
        }
        BigDecimal totalAmount = calculateTotals(reimbursement).totalAllowanceAmount();
        BigDecimal usedAmount = ZERO;
        List<ExpenseSplit> splits = reimbursement.getExpenseSplits();
        for (int index = 0; index < splits.size(); index++) {
            ExpenseSplit split = splits.get(index);
            if (index == splits.size() - 1) {
                split.setAmount(totalAmount.subtract(usedAmount).max(ZERO));
            } else {
                BigDecimal amount = totalAmount.multiply(split.getRatio()).setScale(2, RoundingMode.HALF_UP);
                split.setAmount(amount);
                usedAmount = usedAmount.add(amount);
            }
        }
        replaceExpenseSplitRows(reimbursement.getReimbursementId(), splits);
    }

    private void rebuildAllowanceForTrip(TravelReimbursement reimbursement, TravelTrip trip) {
        TravelAllowance allowance = reimbursement.getAllowances()
                .stream()
                .filter(item -> item.getTripId().equals(trip.getTripId()))
                .findFirst()
                .orElseGet(() -> {
                    TravelAllowance created = new TravelAllowance();
                    created.setAllowanceId("ALW%s%04d".formatted(System.currentTimeMillis(), allowanceSequence.getAndIncrement()));
                    created.setReimbursementId(reimbursement.getReimbursementId());
                    created.setTripId(trip.getTripId());
                    reimbursement.getAllowances().add(created);
                    return created;
                });

        allowance.setTravelerId(trip.getTravelerId());
        allowance.setAllowanceCityNo(trip.getArrivalCityNo());
        allowance.getCalendars().clear();

        LocalDate current = trip.getDepartureDate();
        while (!current.isAfter(trip.getArrivalDate())) {
            AllowanceCalendar calendar = new AllowanceCalendar();
            calendar.setCalendarId("CAL%s%04d".formatted(System.currentTimeMillis(), calendarSequence.getAndIncrement()));
            calendar.setTravelDate(current);
            calendar.setAllowanceCityNo(trip.getArrivalCityNo());
            applyStandards(calendar);
            allowance.getCalendars().add(calendar);
            current = current.plusDays(1);
        }
        recalculateAllowanceAmount(allowance);
        upsertAllowance(allowance);
        jdbcTemplate.update("DELETE FROM allowance_calendar WHERE allowance_id = ?", allowance.getAllowanceId());
        for (AllowanceCalendar calendar : allowance.getCalendars()) {
            insertCalendar(allowance.getAllowanceId(), calendar);
        }
    }

    private void applyStandards(AllowanceCalendar calendar) {
        City city = masterDataService.requireCity(calendar.getAllowanceCityNo());
        BigDecimal mealStandard = switch (city.cityType()) {
            case "1" -> BigDecimal.valueOf(100);
            case "2" -> BigDecimal.valueOf(80);
            default -> BigDecimal.valueOf(50);
        };
        applyDefault(calendar.getMeal(), mealStandard);
        applyDefault(calendar.getTraffic(), TRAFFIC_STANDARD);
        applyDefault(calendar.getCommunication(), COMMUNICATION_STANDARD);
    }

    private void applyDefault(AllowanceItem item, BigDecimal standardAmount) {
        item.setChecked(true);
        item.setStandardAmount(standardAmount);
        item.setAmount(standardAmount);
    }

    private void updateItem(AllowanceItem target, AllowanceItemSaveRequest source, String label) {
        if (source.checked()) {
            target.setChecked(true);
            target.setAmount(source.amount());
        } else {
            target.setChecked(false);
            target.setAmount(source.amount() == null ? ZERO : source.amount());
        }
        validateItem(target, label);
    }

    private void validateItem(AllowanceItem item, String label) {
        BigDecimal amount = item.getAmount() == null ? ZERO : item.getAmount();
        if (!item.isChecked()) {
            if (amount.compareTo(ZERO) != 0) {
                throw BusinessException.validation(label + "未选中时金额必须为0");
            }
            return;
        }
        if (amount.compareTo(ZERO) <= 0) {
            throw BusinessException.validation(label + "金额必须为正数");
        }
        if (amount.compareTo(item.getStandardAmount()) > 0) {
            throw BusinessException.validation(label + "金额不能大于标准金额");
        }
    }

    private void recalculateAllowanceAmount(TravelAllowance allowance) {
        BigDecimal standardAmount = ZERO;
        BigDecimal allowanceAmount = ZERO;
        for (AllowanceCalendar calendar : allowance.getCalendars()) {
            standardAmount = standardAmount.add(checkedStandardAmount(calendar.getMeal()))
                    .add(checkedStandardAmount(calendar.getTraffic()))
                    .add(checkedStandardAmount(calendar.getCommunication()));
            allowanceAmount = allowanceAmount.add(checkedAmount(calendar.getMeal()))
                    .add(checkedAmount(calendar.getTraffic()))
                    .add(checkedAmount(calendar.getCommunication()));
        }
        allowance.setStandardAmount(standardAmount);
        allowance.setApplyAmount(standardAmount);
        allowance.setAllowanceAmount(allowanceAmount);
    }

    private BigDecimal checkedStandardAmount(AllowanceItem item) {
        return item.isChecked() ? item.getStandardAmount() : ZERO;
    }

    private BigDecimal checkedAmount(AllowanceItem item) {
        return item.isChecked() ? item.getAmount() : ZERO;
    }

    private ExpenseTotalsResponse calculateTotals(TravelReimbursement reimbursement) {
        BigDecimal meal = ZERO;
        BigDecimal traffic = ZERO;
        BigDecimal communication = ZERO;
        for (TravelAllowance allowance : reimbursement.getAllowances()) {
            for (AllowanceCalendar calendar : allowance.getCalendars()) {
                meal = meal.add(calendar.getMeal().getAmount());
                traffic = traffic.add(calendar.getTraffic().getAmount());
                communication = communication.add(calendar.getCommunication().getAmount());
            }
        }
        return new ExpenseTotalsResponse(meal.add(traffic).add(communication), meal, traffic, communication);
    }

    private ReimbursementDetailResponse toDetail(TravelReimbursement reimbursement) {
        return new ReimbursementDetailResponse(
                toHeader(reimbursement),
                reimbursement.getTrips().stream().map(this::toTripResponse).toList(),
                reimbursement.getAllowances().stream().map(this::toAllowanceResponse).toList(),
                reimbursement.getExpenseSplits().stream().map(this::toExpenseSplitResponse).toList(),
                calculateTotals(reimbursement),
                availableActions(reimbursement)
        );
    }

    private ReimbursementHeader toHeader(TravelReimbursement reimbursement) {
        Employee employee = masterDataService.requireEmployee(reimbursement.getReimburserId());
        ReimDepartment department = masterDataService.requireDepartment(reimbursement.getReimDepartmentId());
        ReimCompany company = masterDataService.requireCompany(reimbursement.getReimCompanyId());
        BusinessType businessType = masterDataService.requireBusinessType(reimbursement.getBusinessTypeId());
        return new ReimbursementHeader(
                reimbursement.getReimbursementId(),
                reimbursement.getReimbursementNo(),
                reimbursement.getBillDate(),
                reimbursement.getStatus(),
                reimbursement.getStatus().getText(),
                reimbursement.getTitle(),
                reimbursement.getReason(),
                employee.reimburserId(),
                employee.reimburserNo(),
                employee.reimburserName(),
                department.reimDepartmentId(),
                department.reimDepartmentNo(),
                department.reimDepartmentName(),
                company.reimCompanyId(),
                company.reimCompanyNo(),
                company.reimCompanyName(),
                businessType.businessTypeId(),
                businessType.businessTypeNo(),
                businessType.businessTypeName(),
                reimbursement.getRemark(),
                reimbursement.getCreatedAt(),
                reimbursement.getUpdatedAt()
        );
    }

    private ReimbursementListItem toListItem(TravelReimbursement reimbursement) {
        Employee employee = masterDataService.requireEmployee(reimbursement.getReimburserId());
        ReimDepartment department = masterDataService.requireDepartment(reimbursement.getReimDepartmentId());
        ReimCompany company = masterDataService.requireCompany(reimbursement.getReimCompanyId());
        BusinessType businessType = masterDataService.requireBusinessType(reimbursement.getBusinessTypeId());
        return new ReimbursementListItem(
                reimbursement.getReimbursementId(),
                reimbursement.getReimbursementNo(),
                reimbursement.getStatus(),
                reimbursement.getStatus().getText(),
                "日常报销单",
                employee.reimburserId(),
                employee.reimburserNo(),
                employee.reimburserName(),
                department.reimDepartmentId(),
                department.reimDepartmentNo(),
                department.reimDepartmentName(),
                company.reimCompanyId(),
                company.reimCompanyName(),
                businessType.businessTypeId(),
                businessType.businessTypeName(),
                reimbursement.getTitle(),
                reimbursement.getReason(),
                calculateTotals(reimbursement).totalAllowanceAmount(),
                reimbursement.getCreatedAt(),
                availableActions(reimbursement)
        );
    }

    private TripResponse toTripResponse(TravelTrip trip) {
        Employee employee = masterDataService.requireEmployee(trip.getTravelerId());
        City departureCity = masterDataService.requireCity(trip.getDepartureCityNo());
        City arrivalCity = masterDataService.requireCity(trip.getArrivalCityNo());
        return new TripResponse(
                trip.getTripId(),
                trip.getReimbursementId(),
                employee.reimburserId(),
                employee.reimburserNo(),
                employee.reimburserName(),
                departureCity.cityNo(),
                departureCity.cityName(),
                arrivalCity.cityNo(),
                arrivalCity.cityName(),
                trip.getDepartureDate(),
                trip.getArrivalDate(),
                daysInclusive(trip.getDepartureDate(), trip.getArrivalDate()),
                departureCity.cityName() + "-" + arrivalCity.cityName(),
                trip.getDescription()
        );
    }

    private AllowanceResponse toAllowanceResponse(TravelAllowance allowance) {
        TravelTrip trip = requireTrip(requireReimbursement(allowance.getReimbursementId()), allowance.getTripId());
        Employee employee = masterDataService.requireEmployee(allowance.getTravelerId());
        City city = masterDataService.requireCity(allowance.getAllowanceCityNo());
        City departureCity = masterDataService.requireCity(trip.getDepartureCityNo());
        return new AllowanceResponse(
                allowance.getAllowanceId(),
                allowance.getReimbursementId(),
                allowance.getTripId(),
                employee.reimburserId(),
                employee.reimburserName(),
                trip.getDepartureDate() + " ~ " + trip.getArrivalDate(),
                daysInclusive(trip.getDepartureDate(), trip.getArrivalDate()),
                departureCity.cityName() + "-" + city.cityName(),
                city.cityNo(),
                city.cityName(),
                allowance.getStandardAmount(),
                allowance.getApplyAmount(),
                allowance.getAllowanceAmount()
        );
    }

    private ExpenseSplitResponse toExpenseSplitResponse(ExpenseSplit split) {
        ReimCompany company = masterDataService.requireCompany(split.getReimCompanyId());
        Project project = isBlank(split.getProjectId()) ? null : masterDataService.requireProject(split.getProjectId());
        return new ExpenseSplitResponse(
                split.getSplitId(),
                split.getReimbursementId(),
                split.getSequenceNo(),
                company.reimCompanyId(),
                company.reimCompanyName(),
                project == null ? null : project.projectId(),
                project == null ? null : project.projectName(),
                split.getRatio(),
                split.getAmount()
        );
    }

    private CalendarResponse toCalendarResponse(AllowanceCalendar calendar) {
        City city = masterDataService.requireCity(calendar.getAllowanceCityNo());
        return new CalendarResponse(
                calendar.getCalendarId(),
                calendar.getTravelDate(),
                weekDayText(calendar.getTravelDate().getDayOfWeek()),
                city.cityNo(),
                city.cityName(),
                toAllowanceItemResponse(calendar.getMeal()),
                toAllowanceItemResponse(calendar.getTraffic()),
                toAllowanceItemResponse(calendar.getCommunication())
        );
    }

    private AllowanceItemResponse toAllowanceItemResponse(AllowanceItem item) {
        return new AllowanceItemResponse(item.isChecked(), item.getStandardAmount(), item.getAmount());
    }

    private TravelReimbursement loadReimbursementFromDb(String reimbursementId) {
        List<TravelReimbursement> results = jdbcTemplate.query("""
                        SELECT reimbursement_id, reimbursement_no, bill_date, status, title, reason,
                               reimburser_id, reim_department_id, reim_company_id, business_type_id,
                               remark, payment_time, payment_no, created_at, updated_at
                        FROM travel_reimbursement
                        WHERE reimbursement_id = ?
                        """,
                (rs, rowNum) -> {
                    TravelReimbursement reimbursement = new TravelReimbursement();
                    reimbursement.setReimbursementId(rs.getString("reimbursement_id"));
                    reimbursement.setReimbursementNo(rs.getString("reimbursement_no"));
                    reimbursement.setBillDate(rs.getDate("bill_date").toLocalDate());
                    reimbursement.setStatus(ReimbursementStatus.fromStorage(rs.getString("status")));
                    reimbursement.setTitle(rs.getString("title"));
                    reimbursement.setReason(rs.getString("reason"));
                    reimbursement.setReimburserId(rs.getString("reimburser_id"));
                    reimbursement.setReimDepartmentId(rs.getString("reim_department_id"));
                    reimbursement.setReimCompanyId(rs.getString("reim_company_id"));
                    reimbursement.setBusinessTypeId(rs.getString("business_type_id"));
                    reimbursement.setRemark(rs.getString("remark"));
                    if (rs.getTimestamp("payment_time") != null) {
                        reimbursement.setPaymentTime(rs.getTimestamp("payment_time").toLocalDateTime());
                    }
                    reimbursement.setPaymentNo(rs.getString("payment_no"));
                    reimbursement.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    reimbursement.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return reimbursement;
                },
                reimbursementId
        );
        if (results.isEmpty()) {
            throw BusinessException.notFound("报销单不存在");
        }

        TravelReimbursement reimbursement = results.get(0);
        loadTrips(reimbursement);
        loadAllowances(reimbursement);
        loadExpenseSplits(reimbursement);
        return reimbursement;
    }

    private void loadTrips(TravelReimbursement reimbursement) {
        reimbursement.getTrips().addAll(jdbcTemplate.query("""
                        SELECT trip_id, reimbursement_id, traveler_id, departure_city_no, arrival_city_no,
                               departure_date, arrival_date, description
                        FROM travel_trip
                        WHERE reimbursement_id = ?
                        ORDER BY departure_date, trip_id
                        """,
                (rs, rowNum) -> {
                    TravelTrip trip = new TravelTrip();
                    trip.setTripId(rs.getString("trip_id"));
                    trip.setReimbursementId(rs.getString("reimbursement_id"));
                    trip.setTravelerId(rs.getString("traveler_id"));
                    trip.setDepartureCityNo(rs.getString("departure_city_no"));
                    trip.setArrivalCityNo(rs.getString("arrival_city_no"));
                    trip.setDepartureDate(rs.getDate("departure_date").toLocalDate());
                    trip.setArrivalDate(rs.getDate("arrival_date").toLocalDate());
                    trip.setDescription(rs.getString("description"));
                    return trip;
                },
                reimbursement.getReimbursementId()
        ));
    }

    private void loadAllowances(TravelReimbursement reimbursement) {
        List<TravelAllowance> allowances = jdbcTemplate.query("""
                        SELECT allowance_id, reimbursement_id, trip_id, traveler_id, allowance_city_no,
                               standard_amount, apply_amount, allowance_amount
                        FROM travel_allowance
                        WHERE reimbursement_id = ?
                        ORDER BY allowance_id
                        """,
                (rs, rowNum) -> {
                    TravelAllowance allowance = new TravelAllowance();
                    allowance.setAllowanceId(rs.getString("allowance_id"));
                    allowance.setReimbursementId(rs.getString("reimbursement_id"));
                    allowance.setTripId(rs.getString("trip_id"));
                    allowance.setTravelerId(rs.getString("traveler_id"));
                    allowance.setAllowanceCityNo(rs.getString("allowance_city_no"));
                    allowance.setStandardAmount(rs.getBigDecimal("standard_amount"));
                    allowance.setApplyAmount(rs.getBigDecimal("apply_amount"));
                    allowance.setAllowanceAmount(rs.getBigDecimal("allowance_amount"));
                    return allowance;
                },
                reimbursement.getReimbursementId()
        );
        for (TravelAllowance allowance : allowances) {
            loadCalendars(allowance);
            reimbursement.getAllowances().add(allowance);
        }
    }

    private void loadCalendars(TravelAllowance allowance) {
        allowance.getCalendars().addAll(jdbcTemplate.query("""
                        SELECT calendar_id, travel_date, allowance_city_no,
                               meal_checked, meal_standard_amount, meal_amount,
                               traffic_checked, traffic_standard_amount, traffic_amount,
                               communication_checked, communication_standard_amount, communication_amount
                        FROM allowance_calendar
                        WHERE allowance_id = ?
                        ORDER BY travel_date, calendar_id
                        """,
                (rs, rowNum) -> {
                    AllowanceCalendar calendar = new AllowanceCalendar();
                    calendar.setCalendarId(rs.getString("calendar_id"));
                    calendar.setTravelDate(rs.getDate("travel_date").toLocalDate());
                    calendar.setAllowanceCityNo(rs.getString("allowance_city_no"));
                    calendar.getMeal().setChecked(rs.getBoolean("meal_checked"));
                    calendar.getMeal().setStandardAmount(rs.getBigDecimal("meal_standard_amount"));
                    calendar.getMeal().setAmount(rs.getBigDecimal("meal_amount"));
                    calendar.getTraffic().setChecked(rs.getBoolean("traffic_checked"));
                    calendar.getTraffic().setStandardAmount(rs.getBigDecimal("traffic_standard_amount"));
                    calendar.getTraffic().setAmount(rs.getBigDecimal("traffic_amount"));
                    calendar.getCommunication().setChecked(rs.getBoolean("communication_checked"));
                    calendar.getCommunication().setStandardAmount(rs.getBigDecimal("communication_standard_amount"));
                    calendar.getCommunication().setAmount(rs.getBigDecimal("communication_amount"));
                    return calendar;
                },
                allowance.getAllowanceId()
        ));
    }

    private void loadExpenseSplits(TravelReimbursement reimbursement) {
        reimbursement.getExpenseSplits().addAll(jdbcTemplate.query("""
                        SELECT split_id, reimbursement_id, sequence_no, reim_company_id, project_id, ratio, amount
                        FROM expense_split
                        WHERE reimbursement_id = ?
                        ORDER BY sequence_no, split_id
                        """,
                (rs, rowNum) -> {
                    ExpenseSplit split = new ExpenseSplit();
                    split.setSplitId(rs.getString("split_id"));
                    split.setReimbursementId(rs.getString("reimbursement_id"));
                    split.setSequenceNo(rs.getInt("sequence_no"));
                    split.setReimCompanyId(rs.getString("reim_company_id"));
                    split.setProjectId(rs.getString("project_id"));
                    split.setRatio(rs.getBigDecimal("ratio"));
                    split.setAmount(rs.getBigDecimal("amount"));
                    return split;
                },
                reimbursement.getReimbursementId()
        ));
    }

    private void insertReimbursement(TravelReimbursement reimbursement) {
        jdbcTemplate.update("""
                        INSERT INTO travel_reimbursement (
                            reimbursement_id, reimbursement_no, bill_date, status, title, reason,
                            reimburser_id, reim_department_id, reim_company_id, business_type_id,
                            remark, payment_time, payment_no, created_at, updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                reimbursement.getReimbursementId(),
                reimbursement.getReimbursementNo(),
                reimbursement.getBillDate(),
                reimbursement.getStatus().getCode(),
                reimbursement.getTitle(),
                reimbursement.getReason(),
                reimbursement.getReimburserId(),
                reimbursement.getReimDepartmentId(),
                reimbursement.getReimCompanyId(),
                reimbursement.getBusinessTypeId(),
                reimbursement.getRemark(),
                reimbursement.getPaymentTime(),
                reimbursement.getPaymentNo(),
                reimbursement.getCreatedAt(),
                reimbursement.getUpdatedAt()
        );
    }

    private void updateReimbursement(TravelReimbursement reimbursement) {
        jdbcTemplate.update("""
                        UPDATE travel_reimbursement
                        SET bill_date = ?, status = ?, title = ?, reason = ?, reimburser_id = ?,
                            reim_department_id = ?, reim_company_id = ?, business_type_id = ?,
                            remark = ?, payment_time = ?, payment_no = ?, updated_at = ?
                        WHERE reimbursement_id = ?
                        """,
                reimbursement.getBillDate(),
                reimbursement.getStatus().getCode(),
                reimbursement.getTitle(),
                reimbursement.getReason(),
                reimbursement.getReimburserId(),
                reimbursement.getReimDepartmentId(),
                reimbursement.getReimCompanyId(),
                reimbursement.getBusinessTypeId(),
                reimbursement.getRemark(),
                reimbursement.getPaymentTime(),
                reimbursement.getPaymentNo(),
                reimbursement.getUpdatedAt(),
                reimbursement.getReimbursementId()
        );
    }

    private void touchReimbursement(TravelReimbursement reimbursement) {
        jdbcTemplate.update(
                "UPDATE travel_reimbursement SET updated_at = ? WHERE reimbursement_id = ?",
                reimbursement.getUpdatedAt(),
                reimbursement.getReimbursementId()
        );
    }

    private void insertTrip(TravelTrip trip) {
        jdbcTemplate.update("""
                        INSERT INTO travel_trip (
                            trip_id, reimbursement_id, traveler_id, departure_city_no,
                            arrival_city_no, departure_date, arrival_date, description
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                trip.getTripId(),
                trip.getReimbursementId(),
                trip.getTravelerId(),
                trip.getDepartureCityNo(),
                trip.getArrivalCityNo(),
                trip.getDepartureDate(),
                trip.getArrivalDate(),
                trip.getDescription()
        );
    }

    private void updateTripRow(TravelTrip trip) {
        jdbcTemplate.update("""
                        UPDATE travel_trip
                        SET traveler_id = ?, departure_city_no = ?, arrival_city_no = ?,
                            departure_date = ?, arrival_date = ?, description = ?
                        WHERE trip_id = ?
                        """,
                trip.getTravelerId(),
                trip.getDepartureCityNo(),
                trip.getArrivalCityNo(),
                trip.getDepartureDate(),
                trip.getArrivalDate(),
                trip.getDescription(),
                trip.getTripId()
        );
    }

    private void upsertAllowance(TravelAllowance allowance) {
        jdbcTemplate.update("""
                        INSERT INTO travel_allowance (
                            allowance_id, reimbursement_id, trip_id, traveler_id, allowance_city_no,
                            standard_amount, apply_amount, allowance_amount
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            traveler_id = VALUES(traveler_id),
                            allowance_city_no = VALUES(allowance_city_no),
                            standard_amount = VALUES(standard_amount),
                            apply_amount = VALUES(apply_amount),
                            allowance_amount = VALUES(allowance_amount)
                        """,
                allowance.getAllowanceId(),
                allowance.getReimbursementId(),
                allowance.getTripId(),
                allowance.getTravelerId(),
                allowance.getAllowanceCityNo(),
                allowance.getStandardAmount(),
                allowance.getApplyAmount(),
                allowance.getAllowanceAmount()
        );
    }

    private void insertCalendar(String allowanceId, AllowanceCalendar calendar) {
        jdbcTemplate.update("""
                        INSERT INTO allowance_calendar (
                            calendar_id, allowance_id, travel_date, allowance_city_no,
                            meal_checked, meal_standard_amount, meal_amount,
                            traffic_checked, traffic_standard_amount, traffic_amount,
                            communication_checked, communication_standard_amount, communication_amount
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                calendar.getCalendarId(),
                allowanceId,
                calendar.getTravelDate(),
                calendar.getAllowanceCityNo(),
                calendar.getMeal().isChecked(),
                calendar.getMeal().getStandardAmount(),
                calendar.getMeal().getAmount(),
                calendar.getTraffic().isChecked(),
                calendar.getTraffic().getStandardAmount(),
                calendar.getTraffic().getAmount(),
                calendar.getCommunication().isChecked(),
                calendar.getCommunication().getStandardAmount(),
                calendar.getCommunication().getAmount()
        );
    }

    private void replaceExpenseSplitRows(String reimbursementId, List<ExpenseSplit> splits) {
        jdbcTemplate.update("DELETE FROM expense_split WHERE reimbursement_id = ?", reimbursementId);
        for (ExpenseSplit split : splits) {
            jdbcTemplate.update("""
                            INSERT INTO expense_split (
                                split_id, reimbursement_id, sequence_no, reim_company_id, project_id, ratio, amount
                            ) VALUES (?, ?, ?, ?, ?, ?, ?)
                            """,
                    split.getSplitId(),
                    split.getReimbursementId(),
                    split.getSequenceNo(),
                    split.getReimCompanyId(),
                    split.getProjectId(),
                    split.getRatio(),
                    split.getAmount()
            );
        }
    }

    private void deleteAllowanceRowsForTrip(String tripId) {
        jdbcTemplate.update("""
                DELETE FROM allowance_calendar
                WHERE allowance_id IN (
                    SELECT allowance_id FROM travel_allowance WHERE trip_id = ?
                )
                """, tripId);
        jdbcTemplate.update("DELETE FROM travel_allowance WHERE trip_id = ?", tripId);
    }

    private void deleteReimbursementRows(String reimbursementId) {
        jdbcTemplate.update("DELETE FROM expense_split WHERE reimbursement_id = ?", reimbursementId);
        jdbcTemplate.update("""
                DELETE FROM allowance_calendar
                WHERE allowance_id IN (
                    SELECT allowance_id FROM travel_allowance WHERE reimbursement_id = ?
                )
                """, reimbursementId);
        jdbcTemplate.update("DELETE FROM travel_allowance WHERE reimbursement_id = ?", reimbursementId);
        jdbcTemplate.update("DELETE FROM travel_trip WHERE reimbursement_id = ?", reimbursementId);
        jdbcTemplate.update("DELETE FROM travel_reimbursement WHERE reimbursement_id = ?", reimbursementId);
    }

    private TravelReimbursement requireReimbursement(String reimbursementId) {
        TravelReimbursement reimbursement = reimbursements.get(reimbursementId);
        if (reimbursement == null) {
            reimbursement = loadReimbursementFromDb(reimbursementId);
            reimbursements.put(reimbursementId, reimbursement);
        }
        if (reimbursement == null) {
            throw BusinessException.notFound("报销单不存在");
        }
        return reimbursement;
    }

    private TravelReimbursement requireEditableReimbursement(String reimbursementId) {
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        requireDraft(reimbursement);
        return reimbursement;
    }

    private void requireDraft(TravelReimbursement reimbursement) {
        requireStatus(reimbursement, ReimbursementStatus.DRAFT, "仅草稿状态允许编辑");
    }

    private void requireStatus(TravelReimbursement reimbursement, ReimbursementStatus status, String message) {
        if (reimbursement.getStatus() != status) {
            throw BusinessException.validation(message);
        }
    }

    private TravelTrip requireTrip(TravelReimbursement reimbursement, String tripId) {
        return reimbursement.getTrips()
                .stream()
                .filter(trip -> trip.getTripId().equals(tripId))
                .findFirst()
                .orElseThrow(() -> BusinessException.notFound("补录行程不存在"));
    }

    private TravelAllowance requireAllowance(TravelReimbursement reimbursement, String allowanceId) {
        return reimbursement.getAllowances()
                .stream()
                .filter(allowance -> allowance.getAllowanceId().equals(allowanceId))
                .findFirst()
                .orElseThrow(() -> BusinessException.notFound("补助信息不存在"));
    }

    private List<String> availableActions(TravelReimbursement reimbursement) {
        return availableActions(reimbursement.getStatus());
    }

    private List<String> availableActions(ReimbursementStatus status) {
        return switch (status) {
            case DRAFT -> List.of("EDIT", "DELETE", "SUBMIT", "VOID");
            case APPROVING -> List.of("WITHDRAW", "APPROVE", "VOID");
            case COMPLETED -> List.of("VOID");
            case VOIDED -> List.of();
        };
    }

    private void appendLike(StringBuilder where, List<Object> args, String column, String value) {
        if (value != null && !value.isBlank()) {
            where.append(" AND ").append(column).append(" LIKE ?");
            args.add("%" + value.trim() + "%");
        }
    }

    private void appendEquals(StringBuilder where, List<Object> args, String column, String value) {
        if (value != null && !value.isBlank()) {
            where.append(" AND ").append(column).append(" = ?");
            args.add(value.trim());
        }
    }

    private boolean contains(String source, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        if (source == null) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private boolean equalsIfPresent(String source, String expected) {
        return expected == null || expected.isBlank() || Objects.equals(source, expected);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private long daysInclusive(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    private String weekDayText(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "星期一";
            case TUESDAY -> "星期二";
            case WEDNESDAY -> "星期三";
            case THURSDAY -> "星期四";
            case FRIDAY -> "星期五";
            case SATURDAY -> "星期六";
            case SUNDAY -> "星期日";
        };
    }
}
