package com.coldchain.modules.transport.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.common.exception.BizException;
import com.coldchain.modules.dashboard.service.DashboardSeedService;
import com.coldchain.modules.facility.dto.TrackPointVO;
import com.coldchain.modules.facility.entity.Facility;
import com.coldchain.modules.facility.mapper.FacilityMapper;
import com.coldchain.modules.facility.service.FacilityService;
import com.coldchain.modules.transport.dto.TransportOrderVO;
import com.coldchain.modules.transport.dto.TransportRealtimeVO;
import com.coldchain.modules.transport.entity.TransportOrder;
import com.coldchain.modules.transport.mapper.TransportOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 运输监控 RP-007
 */
@Service
@RequiredArgsConstructor
public class TransportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TransportOrderMapper transportOrderMapper;
    private final FacilityMapper facilityMapper;
    private final FacilityService facilityService;
    private final DashboardSeedService dashboardSeedService;

    public List<TransportOrderVO> ongoing() {
        dashboardSeedService.ensureSeedData();
        List<TransportOrder> list = transportOrderMapper.selectList(new LambdaQueryWrapper<TransportOrder>()
                .in(TransportOrder::getStatus, "TRANSPORTING", "PENDING")
                .orderByDesc(TransportOrder::getCreateTime));
        return list.stream().map(this::toVo).toList();
    }

    public List<TransportOrderVO> all(String status) {
        dashboardSeedService.ensureSeedData();
        LambdaQueryWrapper<TransportOrder> qw = new LambdaQueryWrapper<TransportOrder>()
                .orderByDesc(TransportOrder::getCreateTime);
        if (status != null && !status.isBlank()) {
            qw.eq(TransportOrder::getStatus, status);
        }
        return transportOrderMapper.selectList(qw).stream().map(this::toVo).toList();
    }

    public TransportRealtimeVO realtime(Long orderId) {
        TransportOrder order = transportOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("运输订单不存在");
        }
        List<TrackPointVO> track = List.of();
        if (order.getVehicleId() != null) {
            track = facilityService.vehicleTrack(order.getVehicleId(), null, null);
        }
        return TransportRealtimeVO.builder()
                .order(toVo(order))
                .track(track)
                .eta(LocalDateTime.now().plusHours(2).format(FMT))
                .lastUpdate(LocalDateTime.now().format(FMT))
                .build();
    }

    public List<TrackPointVO> track(Long orderId) {
        return realtime(orderId).getTrack();
    }

    private TransportOrderVO toVo(TransportOrder o) {
        Facility v = o.getVehicleId() == null ? null : facilityMapper.selectById(o.getVehicleId());
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int progress = "COMPLETED".equals(o.getStatus()) ? 100
                : "PENDING".equals(o.getStatus()) ? 0
                : 30 + (int) (o.getOrderId() % 50);
        return TransportOrderVO.builder()
                .orderId(o.getOrderId())
                .orderNo(o.getOrderNo())
                .batchId(o.getBatchId())
                .vehicleId(o.getVehicleId())
                .vehicleName(v == null ? "-" : v.getName())
                .route(o.getRoute())
                .status(o.getStatus())
                .startTime(o.getStartTime() == null ? null : o.getStartTime().format(FMT))
                .currentTemp(v == null || v.getCurrentTemp() == null ? 3.0 + r.nextDouble() : v.getCurrentTemp())
                .currentHumidity(v == null || v.getCurrentHumidity() == null ? 85.0 : v.getCurrentHumidity())
                .lng(v == null ? null : v.getLng())
                .lat(v == null ? null : v.getLat())
                .speed(v != null && "ONLINE".equals(v.getStatus()) ? 50.0 + o.getOrderId() % 20 : 0.0)
                .progress(progress)
                .build();
    }
}
