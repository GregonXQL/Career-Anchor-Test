package com.careeranchor.server.controller;

import com.careeranchor.server.config.AuthContext;
import com.careeranchor.server.dto.AdminInviteView;
import com.careeranchor.server.dto.AdminResultSummary;
import com.careeranchor.server.dto.AdminStatsResponse;
import com.careeranchor.server.dto.ApiResponse;
import com.careeranchor.server.dto.InviteCreateRequest;
import com.careeranchor.server.dto.PageResponse;
import com.careeranchor.server.dto.QrImageResponse;
import com.careeranchor.server.dto.QrInviteCreateRequest;
import com.careeranchor.server.dto.QrInviteResponse;
import com.careeranchor.server.dto.ReportResponse;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.enums.Role;
import com.careeranchor.server.exception.BizException;
import com.careeranchor.server.service.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ApiResponse<AdminStatsResponse> stats(
            @RequestAttribute(AuthContext.ATTRIBUTE) AuthContext.Principal principal) {
        requireAdmin(principal);
        return ApiResponse.ok(adminService.stats());
    }

    @GetMapping("/results")
    public ApiResponse<PageResponse<AdminResultSummary>> results(
            @RequestAttribute(AuthContext.ATTRIBUTE) AuthContext.Principal principal,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        requireAdmin(principal);
        if (from != null && to != null && from.isAfter(to)) throw new BizException(ErrorCode.VALIDATION_FAILED);
        return ApiResponse.ok(adminService.results(page, size, keyword, from, to));
    }

    @GetMapping("/results/{id}")
    public ApiResponse<ReportResponse> result(
            @RequestAttribute(AuthContext.ATTRIBUTE) AuthContext.Principal principal,
            @PathVariable @Min(1) long id) {
        requireAdmin(principal);
        return ApiResponse.ok(adminService.result(id));
    }

    @PostMapping("/invites")
    public ApiResponse<List<AdminInviteView>> createInvites(
            @RequestAttribute(AuthContext.ATTRIBUTE) AuthContext.Principal principal,
            @Valid @RequestBody InviteCreateRequest request) {
        requireAdmin(principal);
        return ApiResponse.ok(adminService.createManual(principal.userId(), request));
    }

    @PostMapping("/invites/qr")
    public ApiResponse<QrInviteResponse> createQrInvite(
            @RequestAttribute(AuthContext.ATTRIBUTE) AuthContext.Principal principal,
            @Valid @RequestBody QrInviteCreateRequest request) {
        requireAdmin(principal);
        return ApiResponse.ok(adminService.createQr(principal.userId(), request));
    }

    @GetMapping("/invites")
    public ApiResponse<PageResponse<AdminInviteView>> invites(
            @RequestAttribute(AuthContext.ATTRIBUTE) AuthContext.Principal principal,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(required = false) String status) {
        requireAdmin(principal);
        return ApiResponse.ok(adminService.invites(page, size, status));
    }

    @PatchMapping("/invites/{id}/disable")
    public ApiResponse<AdminInviteView> disable(
            @RequestAttribute(AuthContext.ATTRIBUTE) AuthContext.Principal principal,
            @PathVariable @Min(1) long id) {
        requireAdmin(principal);
        return ApiResponse.ok(adminService.disable(id));
    }

    @GetMapping("/invites/{id}/qrcode")
    public ApiResponse<QrImageResponse> qrcode(
            @RequestAttribute(AuthContext.ATTRIBUTE) AuthContext.Principal principal,
            @PathVariable @Min(1) long id) {
        requireAdmin(principal);
        return ApiResponse.ok(adminService.qrcode(id));
    }

    private void requireAdmin(AuthContext.Principal principal) {
        if (principal.role() != Role.ADMIN) throw new BizException(ErrorCode.FORBIDDEN);
    }
}
