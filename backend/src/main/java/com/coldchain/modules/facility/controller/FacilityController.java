package com.coldchain.modules.facility.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.facility.dto.FacilityDetailVO;
import com.coldchain.modules.facility.dto.FacilityListVO;
import com.coldchain.modules.facility.service.FacilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 设施地图接口 DT-001
 */
@Tag(name = "设施监控")
@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @Operation(summary = "设施列表（冷库+车辆）")
    @GetMapping("/list")
    public R<FacilityListVO> list(@RequestParam(defaultValue = "all") String type,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(defaultValue = "false") boolean refresh) {
        // refresh=true 时轻微抖动在线车辆，模拟实时位置刷新
        if (refresh) {
            facilityService.jitterOnlineVehicles();
        }
        return R.ok(facilityService.list(type, status));
    }

    @Operation(summary = "设施详情")
    @GetMapping("/{id}/detail")
    public R<FacilityDetailVO> detail(@PathVariable Long id) {
        return R.ok(facilityService.detail(id));
    }
}
