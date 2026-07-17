package com.coldchain.modules.monitor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.common.exception.BizException;
import com.coldchain.modules.facility.entity.Facility;
import com.coldchain.modules.facility.entity.SensorDevice;
import com.coldchain.modules.facility.mapper.FacilityMapper;
import com.coldchain.modules.facility.mapper.SensorDeviceMapper;
import com.coldchain.modules.monitor.dto.SensorCardVO;
import com.coldchain.modules.monitor.dto.SensorListVO;
import com.coldchain.modules.monitor.dto.TrendPointVO;
import lombok.RequiredArgsConstructor;
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
 * 实时环境监测业务 EM-001
 */
@Service
@RequiredArgsConstructor
public class MonitorService {

    private final SensorDeviceMapper sensorDeviceMapper;
    private final FacilityMapper facilityMapper;

    public SensorListVO listSensors(Long facilityId, String type, String status, boolean withTrend) {
        LambdaQueryWrapper<SensorDevice> qw = new LambdaQueryWrapper<>();
        if (facilityId != null) {
            qw.eq(SensorDevice::getFacilityId, facilityId);
        }
        if (StringUtils.hasText(type)) {
            qw.eq(SensorDevice::getType, type);
        }
        if (StringUtils.hasText(status)) {
            qw.eq(SensorDevice::getStatus, toDbStatus(status));
        }
        qw.orderByAsc(SensorDevice::getSensorId);
        List<SensorDevice> devices = sensorDeviceMapper.selectList(qw);
        Map<Long, Facility> facilityMap = loadFacilities(devices);
        List<SensorCardVO> cards = devices.stream()
                .map(d -> toCard(d, facilityMap.get(d.getFacilityId()), withTrend))
                .collect(Collectors.toList());
        return new SensorListVO(cards, cards.size());
    }

    public List<TrendPointVO> history(Long sensorId, int hours) {
        SensorDevice device = sensorDeviceMapper.selectById(sensorId);
        if (device == null) {
            throw new BizException("传感器不存在");
        }
        Facility facility = facilityMapper.selectById(device.getFacilityId());
        return buildTrend(device, facility, Math.min(Math.max(hours, 1), 72));
    }

    public List<Map<String, Object>> facilities() {
        List<Facility> list = facilityMapper.selectList(new LambdaQueryWrapper<Facility>()
                .orderByAsc(Facility::getFacilityId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Facility f : list) {
            result.add(Map.of(
                    "facilityId", f.getFacilityId(),
                    "name", f.getName(),
                    "type", f.getType(),
                    "status", f.getStatus() == null ? "ONLINE" : f.getStatus()));
        }
        return result;
    }

    private Map<Long, Facility> loadFacilities(List<SensorDevice> devices) {
        List<Long> ids = devices.stream().map(SensorDevice::getFacilityId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return facilityMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Facility::getFacilityId, f -> f, (a, b) -> a));
    }

    private SensorCardVO toCard(SensorDevice d, Facility f, boolean withTrend) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double baseTemp = f != null && f.getCurrentTemp() != null ? f.getCurrentTemp() : 2.0;
        double baseHum = f != null && f.getCurrentHumidity() != null ? f.getCurrentHumidity() : 80.0;
        // 轻微抖动，模拟实时刷新
        double temp = round2(baseTemp + (r.nextDouble() - 0.5) * 0.1);
        double humidity = round2(baseHum + (r.nextDouble() - 0.5) * 0.8);
        double co2 = round1(380 + (d.getSensorId() % 80) + r.nextDouble() * 20);
        int battery = (int) (60 + (d.getSensorId() % 40));
        return SensorCardVO.builder()
                .sensorId(d.getSensorId())
                .facilityId(d.getFacilityId())
                .facilityName(f == null ? "-" : f.getName())
                .name(d.getName())
                .type(d.getType())
                .temperature(temp)
                .humidity(humidity)
                .co2(co2)
                .battery(battery)
                .status(toApiStatus(d.getStatus()))
                .updateTime(LocalDateTime.now())
                .trendData(withTrend ? buildTrend(d, f, 24) : List.of())
                .build();
    }

    private List<TrendPointVO> buildTrend(SensorDevice d, Facility f, int hours) {
        List<TrendPointVO> points = new ArrayList<>();
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double base = f != null && f.getCurrentTemp() != null ? f.getCurrentTemp() : 2.0;
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        int steps = Math.min(hours * 2, 48);
        for (int i = steps - 1; i >= 0; i--) {
            LocalDateTime t = now.minusMinutes(i * 30L);
            double value = round2(base + Math.sin(i / 3.0 + d.getSensorId()) * 0.6 + (r.nextDouble() - 0.5) * 0.2);
            points.add(new TrendPointVO(t.format(fmt), value));
        }
        return points;
    }

    private Integer toDbStatus(String apiStatus) {
        return switch (apiStatus.toUpperCase()) {
            case "ONLINE" -> 1;
            case "ERROR" -> 2;
            default -> 0;
        };
    }

    private String toApiStatus(Integer dbStatus) {
        if (dbStatus == null) {
            return "OFFLINE";
        }
        return switch (dbStatus) {
            case 1 -> "ONLINE";
            case 2 -> "ERROR";
            default -> "OFFLINE";
        };
    }

    private double round2(double v) {
        return Math.round(v * 100d) / 100d;
    }

    private double round1(double v) {
        return Math.round(v * 10d) / 10d;
    }
}
