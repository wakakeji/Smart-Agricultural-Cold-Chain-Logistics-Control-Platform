package com.coldchain.modules.facility.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.facility.dto.TrackPointVO;
import com.coldchain.modules.facility.service.FacilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车辆轨迹接口
 */
@Tag(name = "车辆轨迹")
@RestController
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    private final FacilityService facilityService;

    @Operation(summary = "车辆历史轨迹")
    @GetMapping("/{id}/track")
    public R<List<TrackPointVO>> track(@PathVariable Long id,
                                       @RequestParam(required = false) String start,
                                       @RequestParam(required = false) String end) {
        return R.ok(facilityService.vehicleTrack(id, start, end));
    }
}
