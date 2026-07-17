package com.coldchain.modules.facility.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.common.exception.BizException;
import com.coldchain.modules.facility.dto.FacilityDetailVO;
import com.coldchain.modules.facility.dto.FacilityItemVO;
import com.coldchain.modules.facility.dto.FacilityListVO;
import com.coldchain.modules.facility.dto.TrackPointVO;
import com.coldchain.modules.facility.entity.Facility;
import com.coldchain.modules.facility.entity.SensorDevice;
import com.coldchain.modules.facility.mapper.FacilityMapper;
import com.coldchain.modules.facility.mapper.SensorDeviceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 设施监控业务（地图 DT-001）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityService {

    private static final String TYPE_STORAGE = "COLD_STORAGE";
    private static final String TYPE_VEHICLE = "REFRIGERATED_VEHICLE";

    private final FacilityMapper facilityMapper;
    private final SensorDeviceMapper sensorDeviceMapper;

    public FacilityListVO list(String type, String status) {
        LambdaQueryWrapper<Facility> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            qw.eq(Facility::getStatus, status);
        }
        if ("storage".equalsIgnoreCase(type) || TYPE_STORAGE.equalsIgnoreCase(type)) {
            qw.eq(Facility::getType, TYPE_STORAGE);
        } else if ("vehicle".equalsIgnoreCase(type) || TYPE_VEHICLE.equalsIgnoreCase(type)) {
            qw.eq(Facility::getType, TYPE_VEHICLE);
        }
        List<Facility> all = facilityMapper.selectList(qw);
        List<FacilityItemVO> storages = all.stream()
                .filter(f -> TYPE_STORAGE.equals(f.getType()))
                .map(this::toItem)
                .collect(Collectors.toList());
        List<FacilityItemVO> vehicles = all.stream()
                .filter(f -> TYPE_VEHICLE.equals(f.getType()))
                .map(this::toItem)
                .collect(Collectors.toList());
        return FacilityListVO.builder()
                .coldStorages(storages)
                .vehicles(vehicles)
                .total(Map.of("storages", storages.size(), "vehicles", vehicles.size()))
                .build();
    }

    public FacilityDetailVO detail(Long id) {
        Facility facility = facilityMapper.selectById(id);
        if (facility == null) {
            throw new BizException("设施不存在");
        }
        List<SensorDevice> sensors = sensorDeviceMapper.selectList(new LambdaQueryWrapper<SensorDevice>()
                .eq(SensorDevice::getFacilityId, id)
                .orderByAsc(SensorDevice::getSensorId));
        return FacilityDetailVO.builder()
                .facility(toItem(facility))
                .sensors(sensors)
                .build();
    }

    /**
     * 车辆轨迹：优先读库；无数据时按起终点生成模拟轨迹点（验收演示用）
     */
    public List<TrackPointVO> vehicleTrack(Long vehicleId, String start, String end) {
        Facility vehicle = facilityMapper.selectById(vehicleId);
        if (vehicle == null || !TYPE_VEHICLE.equals(vehicle.getType())) {
            throw new BizException("车辆不存在");
        }
        LocalDateTime endTime = parseOrDefault(end, LocalDateTime.now());
        LocalDateTime startTime = parseOrDefault(start, endTime.minusHours(2));
        return buildMockTrack(vehicle, startTime, endTime);
    }

    /** 轻微抖动坐标，模拟 30 秒刷新的位置变化 */
    public void jitterOnlineVehicles() {
        List<Facility> vehicles = facilityMapper.selectList(new LambdaQueryWrapper<Facility>()
                .eq(Facility::getType, TYPE_VEHICLE)
                .eq(Facility::getStatus, "ONLINE"));
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (Facility v : vehicles) {
            v.setLng(round6(v.getLng() + (r.nextDouble() - 0.5) * 0.008));
            v.setLat(round6(v.getLat() + (r.nextDouble() - 0.5) * 0.008));
            if (v.getCurrentTemp() != null) {
                v.setCurrentTemp(round1(v.getCurrentTemp() + (r.nextDouble() - 0.5) * 0.2));
            }
            v.setUpdateTime(LocalDateTime.now());
            facilityMapper.updateById(v);
        }
        log.debug("已抖动 {} 辆在线车辆位置", vehicles.size());
    }

    private List<TrackPointVO> buildMockTrack(Facility vehicle, LocalDateTime start, LocalDateTime end) {
        List<TrackPointVO> points = new ArrayList<>();
        int steps = 24;
        double startLng = vehicle.getLng() - 0.12;
        double startLat = vehicle.getLat() - 0.08;
        long seconds = Math.max(60, java.time.Duration.between(start, end).getSeconds());
        for (int i = 0; i <= steps; i++) {
            double t = i * 1.0 / steps;
            double lng = startLng + (vehicle.getLng() - startLng) * t;
            double lat = startLat + (vehicle.getLat() - startLat) * t;
            LocalDateTime time = start.plusSeconds((long) (seconds * t));
            points.add(new TrackPointVO(round6(lng), round6(lat), 40.0 + i, time));
        }
        return points;
    }

    private FacilityItemVO toItem(Facility f) {
        boolean vehicle = TYPE_VEHICLE.equals(f.getType());
        return FacilityItemVO.builder()
                .id(f.getFacilityId())
                .name(f.getName())
                .type(f.getType())
                .lng(f.getLng())
                .lat(f.getLat())
                .status(f.getStatus())
                .currentTemp(f.getCurrentTemp())
                .currentHumidity(f.getCurrentHumidity())
                .capacity(f.getCapacity())
                .usedCapacity(f.getUsedCapacity())
                .loadRate(f.getLoadRate())
                .address(f.getAddress())
                .plateNo(vehicle ? f.getName() : null)
                .driverName(vehicle ? "司机" + (f.getFacilityId() % 20) : null)
                .driverPhone(vehicle ? "138****" + String.format("%04d", f.getFacilityId() % 10000) : null)
                .speed(vehicle && "ONLINE".equals(f.getStatus()) ? 55.0 + (f.getFacilityId() % 20) : 0.0)
                .updateTime(f.getUpdateTime())
                .build();
    }

    private LocalDateTime parseOrDefault(String text, LocalDateTime def) {
        if (!StringUtils.hasText(text)) {
            return def;
        }
        return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private double round6(double v) {
        return Math.round(v * 1_000_000d) / 1_000_000d;
    }

    private double round1(double v) {
        return Math.round(v * 10d) / 10d;
    }
}
