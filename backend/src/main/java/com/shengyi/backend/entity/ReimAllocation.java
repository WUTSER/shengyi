package com.shengyi.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("fk_reim_allocation")
public class ReimAllocation {

    @TableId(type = IdType.INPUT)
    private String id;

    private String mainId;
    private Integer rowIndex;
    private String reimCompanyId;
    private String reimCompanyNo;
    private String reimCompanyName;
    private String projectId;
    private String projectNo;
    private String projectName;
    private BigDecimal allocationRatio;
    private BigDecimal allocationAmount;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMainId() { return mainId; }
    public void setMainId(String mainId) { this.mainId = mainId; }

    public Integer getRowIndex() { return rowIndex; }
    public void setRowIndex(Integer rowIndex) { this.rowIndex = rowIndex; }

    public String getReimCompanyId() { return reimCompanyId; }
    public void setReimCompanyId(String reimCompanyId) { this.reimCompanyId = reimCompanyId; }

    public String getReimCompanyNo() { return reimCompanyNo; }
    public void setReimCompanyNo(String reimCompanyNo) { this.reimCompanyNo = reimCompanyNo; }

    public String getReimCompanyName() { return reimCompanyName; }
    public void setReimCompanyName(String reimCompanyName) { this.reimCompanyName = reimCompanyName; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getProjectNo() { return projectNo; }
    public void setProjectNo(String projectNo) { this.projectNo = projectNo; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public BigDecimal getAllocationRatio() { return allocationRatio; }
    public void setAllocationRatio(BigDecimal allocationRatio) { this.allocationRatio = allocationRatio; }

    public BigDecimal getAllocationAmount() { return allocationAmount; }
    public void setAllocationAmount(BigDecimal allocationAmount) { this.allocationAmount = allocationAmount; }
}
