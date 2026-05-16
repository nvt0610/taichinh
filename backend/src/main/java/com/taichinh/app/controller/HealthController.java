package com.taichinh.app.controller;

import com.taichinh.app.dto.common.ApiResponse;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {
  private final JdbcTemplate jdbcTemplate;

  public HealthController(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @GetMapping("/health")
  public ResponseEntity<ApiResponse<Map<String, String>>> health() {
    Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
    boolean dbOk = one != null && one == 1;
    Map<String, String> data =
        Map.of(
            "backend", "ok",
            "database", dbOk ? "ok" : "fail",
            "check", "fe->be->db");
    return ResponseEntity.ok(ApiResponse.success("Health check completed.", data));
  }
}
