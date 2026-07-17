package com.coldchain.modules.transport.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.facility.dto.TrackPointVO;
import com.coldchain.modules.transport.dto.TransportOrderVO;
import com.coldchain.modules.transport.dto.TransportRealtimeVO;
import com.coldchain.modules.transport.service.TransportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "运输监控")
@RestController
@RequestMapping("/api/transport")
@RequiredArgsConstructor
public class TransportController {

    private final TransportService transportService;

    @Operation(summary = "在途订单")
    @GetMapping("/ongoing")
    public R<List<TransportOrderVO>> ongoing() {
        return R.ok(transportService.ongoing());
    }

    @Operation(summary = "全部订单")
    @GetMapping("/list")
    public R<List<TransportOrderVO>> list(@RequestParam(required = false) String status) {
        return R.ok(transportService.all(status));
    }

    @Operation(summary = "实时状态")
    @GetMapping("/{id}/realtime")
    public R<TransportRealtimeVO> realtime(@PathVariable Long id) {
        return R.ok(transportService.realtime(id));
    }

    @Operation(summary = "轨迹回放")
    @GetMapping("/{id}/track")
    public R<List<TrackPointVO>> track(@PathVariable Long id) {
        return R.ok(transportService.track(id));
    }
}
