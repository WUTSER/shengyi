package com.shengyi.backend.service;

import com.shengyi.backend.common.BusinessException;
import com.shengyi.backend.model.Models.BusinessType;
import com.shengyi.backend.model.Models.City;
import com.shengyi.backend.model.Models.Employee;
import com.shengyi.backend.model.Models.Project;
import com.shengyi.backend.model.Models.ReimCompany;
import com.shengyi.backend.model.Models.ReimDepartment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class MasterDataService {

    private static final RowMapper<ReimCompany> COMPANY_ROW_MAPPER = (rs, rowNum) -> new ReimCompany(
            rs.getString("reim_company_id"),
            rs.getString("reim_company_no"),
            rs.getString("reim_company_name")
    );

    private static final RowMapper<ReimDepartment> DEPARTMENT_ROW_MAPPER = (rs, rowNum) -> new ReimDepartment(
            rs.getString("reim_department_id"),
            rs.getString("reim_department_no"),
            rs.getString("reim_department_name")
    );

    private static final RowMapper<Employee> EMPLOYEE_ROW_MAPPER = (rs, rowNum) -> new Employee(
            rs.getString("reimburser_id"),
            rs.getString("reimburser_no"),
            rs.getString("reimburser_name")
    );

    private static final RowMapper<BusinessType> BUSINESS_TYPE_ROW_MAPPER = (rs, rowNum) -> new BusinessType(
            rs.getString("business_type_id"),
            rs.getString("business_type_no"),
            rs.getString("business_type_name"),
            rs.getString("there_subordinate_node"),
            rs.getString("superior_id")
    );

    private static final RowMapper<City> CITY_ROW_MAPPER = (rs, rowNum) -> new City(
            rs.getString("city_no"),
            rs.getString("city_name"),
            rs.getString("city_type")
    );

    private static final RowMapper<Project> PROJECT_ROW_MAPPER = (rs, rowNum) -> new Project(
            rs.getString("project_id"),
            rs.getString("project_no"),
            rs.getString("project_name")
    );

    private final JdbcTemplate jdbcTemplate;

    public MasterDataService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReimCompany> companies() {
        return jdbcTemplate.query("""
                SELECT reim_company_id, reim_company_no, reim_company_name
                FROM reim_company
                ORDER BY reim_company_no
                """, COMPANY_ROW_MAPPER);
    }

    public List<ReimDepartment> departments() {
        return jdbcTemplate.query("""
                SELECT reim_department_id, reim_department_no, reim_department_name
                FROM reim_department
                ORDER BY reim_department_no
                """, DEPARTMENT_ROW_MAPPER);
    }

    public List<Employee> employees(String keyword) {
        List<Employee> employees = jdbcTemplate.query("""
                SELECT reimburser_id, reimburser_no, reimburser_name
                FROM employee
                ORDER BY reimburser_no
                """, EMPLOYEE_ROW_MAPPER);
        if (keyword == null || keyword.isBlank()) {
            return employees;
        }
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return employees.stream()
                .filter(employee -> employee.reimburserName().toLowerCase(Locale.ROOT).contains(lowerKeyword)
                        || employee.reimburserNo().toLowerCase(Locale.ROOT).contains(lowerKeyword))
                .toList();
    }

    public List<BusinessType> businessTypes() {
        return jdbcTemplate.query("""
                SELECT business_type_id, business_type_no, business_type_name, there_subordinate_node, superior_id
                FROM business_type
                ORDER BY business_type_no
                """, BUSINESS_TYPE_ROW_MAPPER);
    }

    public List<City> cities(String keyword) {
        List<City> cities = jdbcTemplate.query("""
                SELECT city_no, city_name, city_type
                FROM city
                ORDER BY city_no
                """, CITY_ROW_MAPPER);
        if (keyword == null || keyword.isBlank()) {
            return cities;
        }
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return cities.stream()
                .filter(city -> city.cityName().toLowerCase(Locale.ROOT).contains(lowerKeyword)
                        || city.cityNo().toLowerCase(Locale.ROOT).contains(lowerKeyword))
                .toList();
    }

    public List<Project> projects() {
        return jdbcTemplate.query("""
                SELECT project_id, project_no, project_name
                FROM project
                ORDER BY project_no
                """, PROJECT_ROW_MAPPER);
    }

    public Project requireProject(String id) {
        return jdbcTemplate.query("""
                        SELECT project_id, project_no, project_name
                        FROM project
                        WHERE project_id = ?
                        """, PROJECT_ROW_MAPPER, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> BusinessException.validation("项目不存在"));
    }

    public ReimCompany requireCompany(String id) {
        return jdbcTemplate.query("""
                        SELECT reim_company_id, reim_company_no, reim_company_name
                        FROM reim_company
                        WHERE reim_company_id = ?
                        """, COMPANY_ROW_MAPPER, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> BusinessException.validation("费用归属公司不存在"));
    }

    public ReimDepartment requireDepartment(String id) {
        return jdbcTemplate.query("""
                        SELECT reim_department_id, reim_department_no, reim_department_name
                        FROM reim_department
                        WHERE reim_department_id = ?
                        """, DEPARTMENT_ROW_MAPPER, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> BusinessException.validation("报销部门不存在"));
    }

    public Employee requireEmployee(String id) {
        return jdbcTemplate.query("""
                        SELECT reimburser_id, reimburser_no, reimburser_name
                        FROM employee
                        WHERE reimburser_id = ?
                        """, EMPLOYEE_ROW_MAPPER, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> BusinessException.validation("员工不存在"));
    }

    public BusinessType requireBusinessType(String id) {
        return jdbcTemplate.query("""
                        SELECT business_type_id, business_type_no, business_type_name, there_subordinate_node, superior_id
                        FROM business_type
                        WHERE business_type_id = ?
                        """, BUSINESS_TYPE_ROW_MAPPER, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> BusinessException.validation("业务类型不存在"));
    }

    public City requireCity(String cityNo) {
        return jdbcTemplate.query("""
                        SELECT city_no, city_name, city_type
                        FROM city
                        WHERE city_no = ?
                        """, CITY_ROW_MAPPER, cityNo)
                .stream()
                .findFirst()
                .orElseThrow(() -> BusinessException.validation("城市不存在"));
    }
}
