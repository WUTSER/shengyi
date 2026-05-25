# =====================================================================
# 差旅报销单 - 测试数据填充脚本
# 通过后端 REST API 创建多张不同状态、不同业务场景的报销单
# 运行：PowerShell> .\seed-data.ps1
# =====================================================================

$ErrorActionPreference = "Stop"
$BASE = "http://localhost:8081/api/v1/travel-reimbursements"

# UTF-8 中文输出
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

function Invoke-Api {
    param(
        [string]$Method = "GET",
        [string]$Url,
        [object]$Body = $null
    )
    $params = @{
        Uri             = $Url
        Method          = $Method
        UseBasicParsing = $true
        ContentType     = "application/json; charset=utf-8"
        TimeoutSec      = 30
    }
    if ($Body) {
        $json = $Body | ConvertTo-Json -Depth 10 -Compress
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
    }
    try {
        $resp = Invoke-WebRequest @params
        return ($resp.Content | ConvertFrom-Json)
    } catch {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errBody = $reader.ReadToEnd()
        Write-Host "  [API ERROR] $Method $Url -> $errBody" -ForegroundColor Red
        throw
    }
}

# 主数据引用（与 frontend/src/data 静态文件保持一致）
$Company_BJ  = @{ id="1C54557F1782E000"; code="0407"; name="胜意科技北京分公司" }
$Company_SH  = @{ id="19218A262C976000"; code="0408"; name="胜意科技上海分公司" }
$Company_WH  = @{ id="1C61686865DA8000"; code="0409"; name="胜意科技武汉分公司" }

$Dept_KH     = @{ id="13AB8D7B52A9B002"; code="072001"; name="客户成功事业部" }
$Dept_FK     = @{ id="14515BB4BFB92003"; code="072003"; name="企业费控事业部" }
$Dept_HL     = @{ id="19D32F9FE9647000"; code="072005"; name="航旅事业部" }

$Emp_Xu      = @{ id="13AB3A3F72409002"; code="74541"; name="徐年年" }
$Emp_Zheng   = @{ id="13AB4A56BB009002"; code="21552"; name="邹薇" }
$Emp_Wang    = @{ id="13AB591FE8009002"; code="80681"; name="王成军" }
$Emp_Pan     = @{ id="13AB77281A408001"; code="89899"; name="潘展飞" }

$Biz_XMCC    = @{ id="1B5FEB7DD4396000"; code="10010010101"; name="项目出差" }
$Biz_SCTZ    = @{ id="1A92E43082EFC000"; code="10010010102"; name="市场拓展出差" }
$Biz_SHWH    = @{ id="13AB3A4154008001"; code="10010010202"; name="售后维护出差" }

$Proj_HZ     = @{ id="1C811ABF96195000"; code="centralChina"; name="华中客户定制化项目" }
$Proj_HN     = @{ id="1C5931735AC4A000"; code="southChina";   name="华南客户定制化项目" }
$Proj_HB     = @{ id="1771EC45F2443000"; code="northChina";   name="华北客户定制化项目" }
$Proj_NoProj = @{ id="12BC248B25083001"; code="nonProjectRelated"; name="非项目类费用归集" }

# 城市（cityType: 1 一线  2 二线  3 三线）
$City_BJ = @{ cityNo="10119"; cityName="北京"; cityType="1" }
$City_SH = @{ cityNo="10621"; cityName="上海"; cityType="1" }
$City_WH = @{ cityNo="10458"; cityName="武汉"; cityType="2" }
$City_HZ = @{ cityNo="10216"; cityName="杭州"; cityType="2" }
$City_JZ = @{ cityNo="10455"; cityName="荆州"; cityType="3" }

# ---------------------------------------------------------------------
# 创建报销单（基本信息）
# ---------------------------------------------------------------------
function New-Reimbursement {
    param(
        [hashtable]$Company,
        [hashtable]$Department,
        [hashtable]$Employee,
        [hashtable]$BusinessType,
        [string]$Title,
        [string]$Reason,
        [string]$Remark = "",
        [array]$Allocations = @()
    )
    $body = @{
        title          = $Title
        reason         = $Reason
        remark         = $Remark
        reimCompany    = $Company
        reimDepartment = $Department
        reimburser     = $Employee
        businessType   = $BusinessType
        allocations    = $Allocations
    }
    $resp = Invoke-Api -Method POST -Url $BASE -Body $body
    return $resp.data
}

# ---------------------------------------------------------------------
# 行程补录
# ---------------------------------------------------------------------
function Add-Trip {
    param(
        [string]$Id,
        [hashtable]$Traveler,
        [hashtable]$From,
        [hashtable]$To,
        [string]$DepartureDate,
        [string]$ArrivalDate,
        [string]$Description = ""
    )
    $body = @{
        traveler      = $Traveler
        departureCity = $From
        arrivalCity   = $To
        departureDate = $DepartureDate
        arrivalDate   = $ArrivalDate
        description   = $Description
    }
    $resp = Invoke-Api -Method POST -Url "$BASE/$Id/trips" -Body $body
    return $resp.data
}

# ---------------------------------------------------------------------
# 状态流转
# ---------------------------------------------------------------------
function Submit-Bill   { param([string]$Id) Invoke-Api -Method POST -Url "$BASE/$Id/submit"  | Out-Null }
function Approve-Bill  { param([string]$Id) Invoke-Api -Method POST -Url "$BASE/$Id/approve" | Out-Null }
function Disburse-Bill { param([string]$Id, [string]$PaymentNo)
    Invoke-Api -Method POST -Url "$BASE/$Id/disburse" -Body @{ paymentNo = $PaymentNo } | Out-Null
}
function Void-Bill     { param([string]$Id, [string]$Reason)
    Invoke-Api -Method POST -Url "$BASE/$Id/void" -Body @{ reason = $Reason } | Out-Null
}

# =====================================================================
# 数据 1：DRAFT 草稿 - 含一段京杭行程 + 双公司分摊
# =====================================================================
Write-Host ""
Write-Host "[1/5] 创建草稿单 - 北京-杭州出差..." -ForegroundColor Cyan
$bill1 = New-Reimbursement `
    -Company $Company_BJ -Department $Dept_KH -Employee $Emp_Xu `
    -BusinessType $Biz_XMCC `
    -Title  "北京-杭州客户实施" `
    -Reason "杭州客户上线交付 + 培训" `
    -Remark "包含 2 天上线、1 天培训" `
    -Allocations @(
        @{ rowIndex=0; reimCompany=$Company_BJ; project=$Proj_HZ;     allocationRatio=0.7; allocationAmount=$null },
        @{ rowIndex=1; reimCompany=$Company_SH; project=$Proj_NoProj; allocationRatio=0.3; allocationAmount=$null }
    )
Add-Trip -Id $bill1.reimbursementId -Traveler $Emp_Xu -From $City_BJ -To $City_HZ `
    -DepartureDate "2026-05-08" -ArrivalDate "2026-05-10" -Description "客户系统上线" | Out-Null
Write-Host "  -> $($bill1.reimbursementNo) [DRAFT]" -ForegroundColor Green

# =====================================================================
# 数据 2：APPROVING 审批中 - 武汉-上海多日出差
# =====================================================================
Write-Host ""
Write-Host "[2/5] 创建并提交单 - 武汉-上海..." -ForegroundColor Cyan
$bill2 = New-Reimbursement `
    -Company $Company_WH -Department $Dept_FK -Employee $Emp_Zheng `
    -BusinessType $Biz_SCTZ `
    -Title  "武汉-上海市场拓展" `
    -Reason "拜访华东客户 ABC，参加 5/15 行业峰会" `
    -Allocations @(
        @{ rowIndex=0; reimCompany=$Company_WH; project=$Proj_HN; allocationRatio=1.0; allocationAmount=$null }
    )
Add-Trip -Id $bill2.reimbursementId -Traveler $Emp_Zheng -From $City_WH -To $City_SH `
    -DepartureDate "2026-05-13" -ArrivalDate "2026-05-16" -Description "拜访客户+峰会" | Out-Null
Submit-Bill $bill2.reimbursementId
Write-Host "  -> $($bill2.reimbursementNo) [APPROVING]" -ForegroundColor Green

# =====================================================================
# 数据 3：APPROVED 已审批 - 北京售后维护
# =====================================================================
Write-Host ""
Write-Host "[3/5] 创建并审批 - 北京售后..." -ForegroundColor Cyan
$bill3 = New-Reimbursement `
    -Company $Company_BJ -Department $Dept_HL -Employee $Emp_Wang `
    -BusinessType $Biz_SHWH `
    -Title  "北京客户售后维护" `
    -Reason "现场处理生产故障" `
    -Allocations @(
        @{ rowIndex=0; reimCompany=$Company_BJ; project=$Proj_HB; allocationRatio=1.0; allocationAmount=$null }
    )
Add-Trip -Id $bill3.reimbursementId -Traveler $Emp_Wang -From $City_SH -To $City_BJ `
    -DepartureDate "2026-04-22" -ArrivalDate "2026-04-23" -Description "现场支持" | Out-Null
Submit-Bill  $bill3.reimbursementId
Approve-Bill $bill3.reimbursementId
Write-Host "  -> $($bill3.reimbursementNo) [APPROVED]" -ForegroundColor Green

# =====================================================================
# 数据 4：COMPLETED 已放款 - 杭州-荆州  跨越多日
# =====================================================================
Write-Host ""
Write-Host "[4/5] 创建并放款 - 杭州-荆州（已完成）..." -ForegroundColor Cyan
$bill4 = New-Reimbursement `
    -Company $Company_SH -Department $Dept_KH -Employee $Emp_Pan `
    -BusinessType $Biz_XMCC `
    -Title  "杭州-荆州项目巡检" `
    -Reason "Q1 项目交付巡检" `
    -Allocations @(
        @{ rowIndex=0; reimCompany=$Company_SH; project=$Proj_HZ; allocationRatio=0.5; allocationAmount=$null },
        @{ rowIndex=1; reimCompany=$Company_BJ; project=$Proj_HZ; allocationRatio=0.5; allocationAmount=$null }
    )
Add-Trip -Id $bill4.reimbursementId -Traveler $Emp_Pan -From $City_HZ -To $City_JZ `
    -DepartureDate "2026-03-18" -ArrivalDate "2026-03-21" -Description "项目巡检" | Out-Null
Submit-Bill   $bill4.reimbursementId
Approve-Bill  $bill4.reimbursementId
Disburse-Bill $bill4.reimbursementId "PAY202603220001"
Write-Host "  -> $($bill4.reimbursementNo) [COMPLETED]" -ForegroundColor Green

# =====================================================================
# 数据 5：VOIDED 已作废 - 出差取消
# =====================================================================
Write-Host ""
Write-Host "[5/5] 创建并作废 - 行程取消..." -ForegroundColor Cyan
$bill5 = New-Reimbursement `
    -Company $Company_BJ -Department $Dept_KH -Employee $Emp_Zheng `
    -BusinessType $Biz_SCTZ `
    -Title  "上海行业沙龙" `
    -Reason "临时取消" `
    -Allocations @()
Void-Bill $bill5.reimbursementId "客户调整行程，本次差旅取消"
Write-Host "  -> $($bill5.reimbursementNo) [VOIDED]" -ForegroundColor Green

# ---------------------------------------------------------------------
# 汇总
# ---------------------------------------------------------------------
Write-Host ""
Write-Host "数据填充完成，当前列表：" -ForegroundColor Yellow
$list = Invoke-Api -Method GET -Url "$($BASE)?pageNo=1&pageSize=20"
$list.data.records | ForEach-Object {
    "{0,-22} {1,-12} {2,-10} {3,-30} 补助={4}" -f $_.reimbursementNo, $_.status, $_.reimburserName, $_.title, $_.allowanceAmount
}
Write-Host ""
Write-Host "前端访问：http://localhost:5173/" -ForegroundColor Yellow
