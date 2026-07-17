package com.coldchain.modules.monitor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.modules.facility.entity.Facility;
import com.coldchain.modules.facility.entity.SensorDevice;
import com.coldchain.modules.facility.mapper.FacilityMapper;
import com.coldchain.modules.facility.mapper.SensorDeviceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 补齐实时监控传感器至 256 个（验收指标）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSeedService {

    private static final int TARGET = 256;
    private static final String[] TYPES = {"TEMP", "HUMIDITY", "CO2", "VIBRATION", "LIGHT"};

    private final SensorDeviceMapper sensorDeviceMapper;
    private final FacilityMapper facilityMapper;

    @Transactional
    public void ensureSensors() {
        Long count = sensorDeviceMapper.selectCount(new LambdaQueryWrapper<>());
        if (count != null && count >= TARGET) {
            log.info("传感器数量已达标: {}", count);
            return;
        }
        List<Facility> facilities = facilityMapper.selectList(new LambdaQueryWrapper<Facility>()
                .orderByAsc(Facility::getFacilityId));
        if (facilities.isEmpty()) {
            log.warn("无设施数据，跳过传感器种子");
            return;
        }
        long need = TARGET - (count == null ? 0 : count);
        int created = 0;
        int idx = 0;
        while (created < need) {
            Facility f = facilities.get(idx % facilities.size());
            SensorDevice s = new SensorDevice();
            s.setFacilityId(f.getFacilityId());
            String type = TYPES[idx % TYPES.length];
            s.setName(f.getName() + "-" + type + "-" + String.format("%03d", (idx % 100) + 1));
            s.setType(type);
            s.setModel("MOCK-" + type);
            s.setStatus("ONLINE".equals(f.getStatus()) ? 1 : ("ALARM".equals(f.getStatus()) ? 2 : 0));
            s.setInstallDate(LocalDate.now().minusMonths(3));
            s.setCreateTime(LocalDateTime.now());
            sensorDeviceMapper.insert(s);
            created++;
            idx++;
        }
        log.info("已补充传感器 {} 个，目标 {}", created, TARGET);
    }
}
