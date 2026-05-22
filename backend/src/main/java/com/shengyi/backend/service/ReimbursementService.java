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
import com.shengyi.backend.model.Models.ReimCompany;
import com.shengyi.backend.model.Models.ReimDepartment;
import com.shengyi.backend.model.Models.TravelAllowance;
import com.shengyi.backend.model.Models.TravelReimbursement;
import com.shengyi.backend.model.Models.TravelTrip;
import com.shengyi.backend.model.ReimbursementStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
public class ReimbursementService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal TRAFFIC_STANDARD = BigDecimal.valueOf(40);
    private static final BigDecimal COMMUNICATION_STANDARD = BigDecimal.valueOf(40);

    private final MasterDataService masterDataService;
    private final Map<String, TravelReimbursement> reimbursements = new LinkedHashMap<>();
    private final AtomicLong reimbursementSequence = new AtomicLong(1);
    private final AtomicLong tripSequence = new AtomicLong(1);
    private final AtomicLong allowanceSequence = new AtomicLong(1);
    private final AtomicLong calendarSequence = new AtomicLong(1);

    public ReimbursementService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    public synchronized PageResponse<ReimbursementListItem> list(ReimbursementQuery query) {
        int pageNo = query.pageNo() == null || query.pageNo() < 1 ? 1 : query.pageNo();
        int pageSize = query.pageSize() == null || query.pageSize() < 1 ? 20 : query.pageSize();

        List<ReimbursementListItem> filtered = reimbursements.values()
                .stream()
                .filter(item -> contains(item.getReimbursementNo(), query.reimbursementNo()))
                .filter(item -> contains(item.getTitle(), query.title()))
                .filter(item -> contains(item.getReason(), query.reason()))
                .filter(item -> equalsIfPresent(item.getReimCompanyId(), query.reimCompanyId()))
                .filter(item -> equalsIfPresent(item.getReimDepartmentId(), query.reimDepartmentId()))
                .filter(item -> equalsIfPresent(item.getReimburserId(), query.reimburserId()))
                .filter(item -> equalsIfPresent(item.getBusinessTypeId(), query.businessTypeId()))
                .filter(item -> query.status() == null || item.getStatus() == query.status())
                .sorted(Comparator.comparing(TravelReimbursement::getCreatedAt).reversed())
                .map(this::toListItem)
                .toList();

        int fromIndex = Math.min((pageNo - 1) * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        return new PageResponse<>(pageNo, pageSize, filtered.size(), filtered.subList(fromIndex, toIndex));
    }

    public synchronized ReimbursementCreatedResponse create(ReimbursementSaveRequest request) {
        validateMasterData(request);

        LocalDateTime now = LocalDateTime.now();
        long idValue = reimbursementSequence.getAndIncrement();

        TravelReimbursement reimbursement = new TravelReimbursement();
        reimbursement.setReimbursementId("RB%012d".formatted(idValue));
        reimbursement.setReimbursementNo("CLBX%s%04d".formatted(now.toLocalDate().toString().replace("-", ""), idValue));
        reimbursement.setStatus(ReimbursementStatus.DRAFT);
        reimbursement.setCreatedAt(now);
        reimbursement.setUpdatedAt(now);
        applyRequest(reimbursement, request);

        reimbursements.put(reimbursement.getReimbursementId(), reimbursement);
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
        return toDetail(reimbursement);
    }

    public synchronized void delete(String reimbursementId) {
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        requireDraft(reimbursement);
        reimbursements.remove(reimbursementId);
    }

    public synchronized ReimbursementDetailResponse submit(String reimbursementId) {
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        requireStatus(reimbursement, ReimbursementStatus.DRAFT, "仅未提交状态允许提交");
        validateBeforeSubmit(reimbursement);
        reimbursement.setStatus(ReimbursementStatus.APPROVING);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        return toDetail(reimbursement);
    }

    public synchronized ReimbursementDetailResponse withdraw(String reimbursementId) {
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        requireStatus(reimbursement, ReimbursementStatus.APPROVING, "仅审批中状态允许撤回");
        reimbursement.setStatus(ReimbursementStatus.DRAFT);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        return toDetail(reimbursement);
    }

    public synchronized ReimbursementDetailResponse voidBill(String reimbursementId, VoidRequest request) {
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        if (reimbursement.getStatus() == ReimbursementStatus.COMPLETED
                || reimbursement.getStatus() == ReimbursementStatus.VOIDED) {
            throw BusinessException.validation("当前状态不允许作废");
        }
        reimbursement.setStatus(ReimbursementStatus.VOIDED);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        return toDetail(reimbursement);
    }

    public synchronized ReimbursementDetailResponse approve(String reimbursementId) {
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        requireStatus(reimbursement, ReimbursementStatus.APPROVING, "仅审批中状态允许审批通过");
        reimbursement.setStatus(ReimbursementStatus.APPROVED);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        return toDetail(reimbursement);
    }

    public synchronized ReimbursementDetailResponse disburse(String reimbursementId, PaymentRequest request) {
        TravelReimbursement reimbursement = requireReimbursement(reimbursementId);
        requireStatus(reimbursement, ReimbursementStatus.APPROVED, "仅审批通过状态允许放款");
        reimbursement.setPaymentTime(request.paymentTime());
        reimbursement.setPaymentNo(request.paymentNo());
        reimbursement.setStatus(ReimbursementStatus.COMPLETED);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        return toDetail(reimbursement);
    }

    public synchronized TripResponse addTrip(String reimbursementId, TripRequest request) {
        TravelReimbursement reimbursement = requireEditableReimbursement(reimbursementId);
        validateTripRequest(request, null, reimbursement);

        TravelTrip trip = new TravelTrip();
        trip.setTripId("TRIP%012d".formatted(tripSequence.getAndIncrement()));
        trip.setReimbursementId(reimbursementId);
        applyTripRequest(trip, request);

        reimbursement.getTrips().add(trip);
        rebuildAllowanceForTrip(reimbursement, trip);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        return toTripResponse(trip);
    }

    public synchronized TripResponse updateTrip(String reimbursementId, String tripId, TripRequest request) {
        TravelReimbursement reimbursement = requireEditableReimbursement(reimbursementId);
        TravelTrip trip = requireTrip(reimbursement, tripId);
        validateTripRequest(request, tripId, reimbursement);

        applyTripRequest(trip, request);
        rebuildAllowanceForTrip(reimbursement, trip);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        return toTripResponse(trip);
    }

    public synchronized void deleteTrip(String reimbursementId, String tripId) {
        TravelReimbursement reimbursement = requireEditableReimbursement(reimbursementId);
        TravelTrip trip = requireTrip(reimbursement, tripId);
        reimbursement.getTrips().remove(trip);
        reimbursement.getAllowances().removeIf(allowance -> allowance.getTripId().equals(tripId));
        reimbursement.setUpdatedAt(LocalDateTime.now());
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
        reimbursement.setUpdatedAt(LocalDateTime.now());
        return allowance.getCalendars().stream().map(this::toCalendarResponse).toList();
    }

    public synchronized ExpenseTotalsResponse totals(String reimbursementId) {
        return calculateTotals(requireReimbursement(reimbursementId));
    }

    public synchronized ReimbursementDetailResponse updateRemark(String reimbursementId, RemarkRequest request) {
        TravelReimbursement reimbursement = requireEditableReimbursement(reimbursementId);
        reimbursement.setRemark(request.remark());
        reimbursement.setUpdatedAt(LocalDateTime.now());
        return toDetail(reimbursement);
    }

    public synchronized ReimbursementDetailResponse deleteRemark(String reimbursementId) {
        TravelReimbursement reimbursement = requireEditableReimbursement(reimbursementId);
        reimbursement.setRemark(null);
        reimbursement.setUpdatedAt(LocalDateTime.now());
        return toDetail(reimbursement);
    }

    private void validateMasterData(ReimbursementSaveRequest request) {
        masterDataService.requireEmployee(request.reimburserId());
        masterDataService.requireDepartment(request.reimDepartmentId());
        masterDataService.requireCompany(request.reimCompanyId());
        masterDataService.requireBusinessType(request.businessTypeId());
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
    }

    private void rebuildAllowanceForTrip(TravelReimbursement reimbursement, TravelTrip trip) {
        TravelAllowance allowance = reimbursement.getAllowances()
                .stream()
                .filter(item -> item.getTripId().equals(trip.getTripId()))
                .findFirst()
                .orElseGet(() -> {
                    TravelAllowance created = new TravelAllowance();
                    created.setAllowanceId("ALW%012d".formatted(allowanceSequence.getAndIncrement()));
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
            calendar.setCalendarId("CAL%012d".formatted(calendarSequence.getAndIncrement()));
            calendar.setTravelDate(current);
            calendar.setAllowanceCityNo(trip.getArrivalCityNo());
            applyStandards(calendar);
            allowance.getCalendars().add(calendar);
            current = current.plusDays(1);
        }
        recalculateAllowanceAmount(allowance);
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
            standardAmount = standardAmount
                    .add(calendar.getMeal().getStandardAmount())
                    .add(calendar.getTraffic().getStandardAmount())
                    .add(calendar.getCommunication().getStandardAmount());
            allowanceAmount = allowanceAmount
                    .add(calendar.getMeal().getAmount())
                    .add(calendar.getTraffic().getAmount())
                    .add(calendar.getCommunication().getAmount());
        }
        allowance.setStandardAmount(standardAmount);
        allowance.setApplyAmount(standardAmount);
        allowance.setAllowanceAmount(allowanceAmount);
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

    private TravelReimbursement requireReimbursement(String reimbursementId) {
        TravelReimbursement reimbursement = reimbursements.get(reimbursementId);
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
        requireStatus(reimbursement, ReimbursementStatus.DRAFT, "仅未提交状态允许编辑");
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
        return switch (reimbursement.getStatus()) {
            case DRAFT -> List.of("EDIT", "DELETE", "SUBMIT", "VOID");
            case APPROVING -> List.of("WITHDRAW", "APPROVE", "VOID");
            case APPROVED -> List.of("DISBURSE", "VOID");
            case COMPLETED, VOIDED -> List.of();
        };
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
