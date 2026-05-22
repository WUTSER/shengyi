package com.shengyi.backend.controller;

import com.shengyi.backend.common.ApiResponse;
import com.shengyi.backend.model.Models.BusinessType;
import com.shengyi.backend.model.Models.City;
import com.shengyi.backend.model.Models.Employee;
import com.shengyi.backend.model.Models.Project;
import com.shengyi.backend.model.Models.ReimCompany;
import com.shengyi.backend.model.Models.ReimDepartment;
import com.shengyi.backend.service.MasterDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master-data")
public class MasterDataController {

    private final MasterDataService masterDataService;

    public MasterDataController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GetMapping("/reim-companies")
    public ApiResponse<List<ReimCompany>> companies() {
        return ApiResponse.success(masterDataService.companies());
    }

    @GetMapping("/reim-departments")
    public ApiResponse<List<ReimDepartment>> departments() {
        return ApiResponse.success(masterDataService.departments());
    }

    @GetMapping("/employees")
    public ApiResponse<List<Employee>> employees(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(masterDataService.employees(keyword));
    }

    @GetMapping("/business-types")
    public ApiResponse<List<BusinessType>> businessTypes() {
        return ApiResponse.success(masterDataService.businessTypes());
    }

    @GetMapping("/cities")
    public ApiResponse<List<City>> cities(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(masterDataService.cities(keyword));
    }

    @GetMapping("/projects")
    public ApiResponse<List<Project>> projects() {
        return ApiResponse.success(masterDataService.projects());
    }
}
