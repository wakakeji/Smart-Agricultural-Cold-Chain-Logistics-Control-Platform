package com.coldchain.modules.route.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.route.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "路线规划")
@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping("/plan")
    @Operation(summary = "规划多方案路线")
    public R<Map<String, Object>> plan(@RequestParam(required = false) String origin,
                                       @RequestParam(required = false) String destination) {
        return R.ok(routeService.plan(origin, destination));
    }

    @GetMapping("/traffic")
    @Operation(summary = "路况摘要")
    public R<List<Map<String, Object>>> traffic() {
        return R.ok(routeService.traffic());
    }
}
