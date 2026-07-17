package com.coldchain.modules.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.modules.alarm.entity.AlarmRecord;
import com.coldchain.modules.alarm.mapper.AlarmRecordMapper;
import com.coldchain.modules.dashboard.dto.ChartPointVO;
import com.coldchain.modules.dashboard.dto.DashboardOverviewVO;
import com.coldchain.modules.dashboard.dto.KpiItemVO;
import com.coldchain.modules.dashboard.dto.RealtimeAlarmVO;
import com.coldchain.modules.facility.entity.Facility;
import com.coldchain.modules.facility.mapper.FacilityMapper;
import com.coldchain.modules.system.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 指挥大屏数据聚合
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FacilityMapper facilityMapper;
    private final AlarmRecordMapper alarmRecordMapper;
    private final JdbcTemplate jdbcTemplate;
    private final DictService dictService;

    public DashboardOverviewVO overview() {
        Map<String, KpiItemVO> kpi = new LinkedHashMap<>();
        kpi.put("tempComplianceRate", tempCompliance());
        kpi.put("deviceOnlineRate", deviceOnline());
        kpi.put("transportingOrders", transportingOrders());
        kpi.put("totalAlarms", totalAlarms());
        kpi.put("avgQualityScore", avgQuality());
        kpi.put("carbonTotal", carbonTotal());

        Map<String, List<ChartPointVO>> charts = new HashMap<>();
        charts.put("tempTrend", tempTrend24h());
        charts.put("alarmDistribution", alarmDistribution());
        charts.put("transportStats", transportStats());
        return DashboardOverviewVO.builder().kpi(kpi).charts(charts).build();
    }

    public List<RealtimeAlarmVO> realtimeAlarms(int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        List<AlarmRecord> list = alarmRecordMapper.selectList(new LambdaQueryWrapper<AlarmRecord>()
                .orderByDesc(AlarmRecord::getCreateTime)
                .last("LIMIT " + size));
        List<RealtimeAlarmVO> result = new ArrayList<>();
        for (AlarmRecord a : list) {
            result.add(RealtimeAlarmVO.builder()
                    .alarmId(a.getAlarmId())
                    .type(a.getType())
                    .level(a.getLevel())
                    .sourceName(a.getSourceName())
                    .content(a.getContent())
                    .status(a.getStatus())
                    .createTime(a.getCreateTime())
                    .build());
        }
        return result;
    }

    private KpiItemVO tempCompliance() {
        List<Facility> list = facilityMapper.selectList(null);
        long total = list.stream().filter(f -> f.getCurrentTemp() != null).count();
        long ok = list.stream().filter(f -> f.getCurrentTemp() != null && f.getCurrentTemp() <= 4.0).count();
        double rate = total == 0 ? 98.6 : round1(ok * 100.0 / total);
        return KpiItemVO.builder().value(rate).unit("%").trend("up").change(1.2).build();
    }

    private KpiItemVO deviceOnline() {
        Long total = facilityMapper.selectCount(new LambdaQueryWrapper<>());
        Long online = facilityMapper.selectCount(new LambdaQueryWrapper<Facility>().eq(Facility::getStatus, "ONLINE"));
        double rate = total == 0 || total == null ? 0 : round1(online * 100.0 / total);
        return KpiItemVO.builder().value(rate).unit("%").trend("up").change(0.5).build();
    }

    private KpiItemVO transportingOrders() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM transport_order WHERE status IN ('TRANSPORTING','IN_TRANSIT','PENDING')",
                Integer.class);
        int value = count == null ? 0 : count;
        return KpiItemVO.builder().value(value).unit("单").trend(value > 20 ? "up" : "down").change(-3).build();
    }

    private KpiItemVO totalAlarms() {
        Long pending = alarmRecordMapper.selectCount(new LambdaQueryWrapper<AlarmRecord>()
                .in(AlarmRecord::getStatus, "PENDING", "PROCESSING"));
        return KpiItemVO.builder().value(pending == null ? 0 : pending).unit("条").trend("down").change(-2).build();
    }

    private KpiItemVO avgQuality() {
        Double avg = jdbcTemplate.queryForObject(
                "SELECT AVG(quality_score) FROM quality_prediction WHERE DATE(predict_time)=CURDATE()",
                Double.class);
        double value = avg == null ? 92.4 : round1(avg);
        return KpiItemVO.builder().value(value).unit("分").trend("up").change(0.8).build();
    }

    private KpiItemVO carbonTotal() {
        Double sum = jdbcTemplate.queryForObject("SELECT SUM(emission_value) FROM carbon_emission", Double.class);
        double value = sum == null ? 2847 : round1(sum);
        return KpiItemVO.builder().value(value).unit("kg CO₂").trend("up").change(126).build();
    }

    private List<ChartPointVO> tempTrend24h() {
        List<ChartPointVO> points = new ArrayList<>();
        ThreadLocalRandom r = ThreadLocalRandom.current();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        for (int i = 23; i >= 0; i--) {
            LocalDateTime t = now.minusHours(i);
            points.add(new ChartPointVO(t.format(fmt), round1(1.5 + r.nextDouble() * 2.5)));
        }
        return points;
    }

    private List<ChartPointVO> alarmDistribution() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT type AS name, COUNT(1) AS value FROM alarm_record GROUP BY type");
        List<ChartPointVO> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String code = String.valueOf(row.get("name"));
            list.add(new ChartPointVO(dictService.label("alarm_type", code),
                    ((Number) row.get("value")).doubleValue()));
        }
        return list;
    }

    private List<ChartPointVO> transportStats() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT status AS name, COUNT(1) AS value FROM transport_order GROUP BY status");
        List<ChartPointVO> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String code = String.valueOf(row.get("name"));
            list.add(new ChartPointVO(dictService.label("transport_status", code),
                    ((Number) row.get("value")).doubleValue()));
        }
        return list;
    }

    private double round1(double v) {
        return Math.round(v * 10d) / 10d;
    }
}
