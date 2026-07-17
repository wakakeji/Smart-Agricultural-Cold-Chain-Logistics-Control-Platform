package com.coldchain.modules.facility.service;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 地图演示数据种子：128 冷库 + 12 冷藏车（广西区域坐标）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FacilitySeedService {

    private static final int TARGET_STORAGE = 128;
    private static final int TARGET_VEHICLE = 12;
    private static final String TYPE_STORAGE = "COLD_STORAGE";
    private static final String TYPE_VEHICLE = "REFRIGERATED_VEHICLE";

    private final FacilityMapper facilityMapper;
    private final SensorDeviceMapper sensorDeviceMapper;

    @Transactional
    public void ensureSeedData() {
        long storageCnt = facilityMapper.selectCount(new LambdaQueryWrapper<Facility>()
                .eq(Facility::getType, TYPE_STORAGE));
        long vehicleCnt = facilityMapper.selectCount(new LambdaQueryWrapper<Facility>()
                .eq(Facility::getType, TYPE_VEHICLE));
        if (storageCnt >= TARGET_STORAGE && vehicleCnt >= TARGET_VEHICLE) {
            log.info("设施种子数据已就绪: 冷库={}, 车辆={}", storageCnt, vehicleCnt);
            return;
        }
        // 清空后重建，保证验收数量准确
        sensorDeviceMapper.delete(new LambdaQueryWrapper<>());
        facilityMapper.delete(new LambdaQueryWrapper<>());
        List<Facility> storages = buildStorages();
        List<Facility> vehicles = buildVehicles();
        for (Facility f : storages) {
            facilityMapper.insert(f);
            insertDefaultSensor(f);
        }
        for (Facility f : vehicles) {
            facilityMapper.insert(f);
            insertDefaultSensor(f);
        }
        log.info("已生成设施种子: 冷库={}, 车辆={}", storages.size(), vehicles.size());
    }

    private List<Facility> buildStorages() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        String[] cities = {"南宁", "柳州", "桂林", "北海", "玉林", "百色", "钦州", "梧州"};
        List<Facility> list = new ArrayList<>(TARGET_STORAGE);
        for (int i = 1; i <= TARGET_STORAGE; i++) {
            String city = cities[(i - 1) % cities.length];
            Facility f = new Facility();
            f.setName(city + "冷库" + String.format("%03d", i) + "区");
            f.setType(TYPE_STORAGE);
            // 广西大致范围：经度 104.5~112.0，纬度 21.5~26.5
            f.setLng(round6(104.5 + r.nextDouble() * 7.5));
            f.setLat(round6(21.5 + r.nextDouble() * 5.0));
            f.setStatus(pickStatus(r, 0.85, 0.08));
            f.setCapacity(1000.0 + r.nextInt(5000));
            f.setUsedCapacity(f.getCapacity() * (0.3 + r.nextDouble() * 0.6));
            f.setAddress("广西壮族自治区" + city + "市冷链园区");
            f.setCurrentTemp(round1(-2 + r.nextDouble() * 8));
            f.setCurrentHumidity(round1(70 + r.nextDouble() * 25));
            if ("ALARM".equals(f.getStatus())) {
                f.setCurrentTemp(round1(6 + r.nextDouble() * 4));
            }
            f.setCreateTime(LocalDateTime.now());
            f.setUpdateTime(LocalDateTime.now());
            list.add(f);
        }
        return list;
    }

    private List<Facility> buildVehicles() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        String[] plates = {"桂A", "桂B", "桂C", "桂E", "桂N", "桂R"};
        List<Facility> list = new ArrayList<>(TARGET_VEHICLE);
        for (int i = 1; i <= TARGET_VEHICLE; i++) {
            Facility f = new Facility();
            f.setName(plates[(i - 1) % plates.length] + "·D" + String.format("%04d", 1000 + i));
            f.setType(TYPE_VEHICLE);
            f.setLng(round6(108.0 + r.nextDouble() * 2.0));
            f.setLat(round6(22.5 + r.nextDouble() * 2.0));
            f.setStatus(pickStatus(r, 0.75, 0.1));
            f.setCapacity(20.0);
            f.setUsedCapacity(round1(5 + r.nextDouble() * 15));
            f.setLoadRate(round2(f.getUsedCapacity() / f.getCapacity()));
            f.setCurrentTemp(round1(0 + r.nextDouble() * 5));
            f.setCurrentHumidity(round1(75 + r.nextDouble() * 15));
            f.setCreateTime(LocalDateTime.now());
            f.setUpdateTime(LocalDateTime.now());
            list.add(f);
        }
        return list;
    }

    private void insertDefaultSensor(Facility f) {
        SensorDevice s = new SensorDevice();
        s.setFacilityId(f.getFacilityId());
        s.setName(f.getName() + "-温湿度");
        s.setType("TEMP");
        s.setModel("SHT35");
        s.setStatus("ONLINE".equals(f.getStatus()) ? 1 : 0);
        s.setInstallDate(LocalDate.now().minusMonths(6));
        s.setCreateTime(LocalDateTime.now());
        sensorDeviceMapper.insert(s);
    }

    private String pickStatus(ThreadLocalRandom r, double onlineRate, double alarmRate) {
        double v = r.nextDouble();
        if (v < onlineRate) {
            return "ONLINE";
        }
        if (v < onlineRate + alarmRate) {
            return "ALARM";
        }
        return "OFFLINE";
    }

    private double round6(double v) {
        return Math.round(v * 1_000_000d) / 1_000_000d;
    }

    private double round1(double v) {
        return Math.round(v * 10d) / 10d;
    }

    private double round2(double v) {
        return Math.round(v * 100d) / 100d;
    }
}
