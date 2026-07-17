package com.coldchain.modules.dashboard.service;

import com.coldchain.modules.alarm.entity.AlarmRecord;
import com.coldchain.modules.alarm.mapper.AlarmRecordMapper;
import com.coldchain.modules.facility.entity.Facility;
import com.coldchain.modules.facility.mapper.FacilityMapper;
import com.coldchain.modules.system.service.DictService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 大屏聚合单元测试
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private FacilityMapper facilityMapper;
    @Mock
    private AlarmRecordMapper alarmRecordMapper;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DictService dictService;
    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void overviewContainsSixKpis() {
        Facility online = new Facility();
        online.setStatus("ONLINE");
        online.setCurrentTemp(2.0);
        when(facilityMapper.selectList(null)).thenReturn(List.of(online));
        when(facilityMapper.selectCount(any())).thenReturn(1L);
        when(alarmRecordMapper.selectCount(any())).thenReturn(3L);
        when(jdbcTemplate.queryForObject(contains("transport_order"), eq(Integer.class))).thenReturn(5);
        when(jdbcTemplate.queryForObject(contains("quality_prediction"), eq(Double.class))).thenReturn(92.0);
        when(jdbcTemplate.queryForObject(contains("carbon_emission"), eq(Double.class))).thenReturn(1000.0);
        when(jdbcTemplate.queryForList(contains("alarm_record"))).thenReturn(List.of());
        when(jdbcTemplate.queryForList(contains("transport_order"))).thenReturn(List.of());

        var vo = dashboardService.overview();
        assertEquals(6, vo.getKpi().size());
        assertTrue(vo.getKpi().containsKey("tempComplianceRate"));
        assertEquals(3, vo.getCharts().size());
    }

    @Test
    void realtimeAlarmsMapsFields() {
        AlarmRecord a = new AlarmRecord();
        a.setAlarmId(1L);
        a.setType("TEMP_OVER");
        a.setLevel("CRITICAL");
        a.setSourceName("冷库A");
        a.setContent("超温");
        a.setStatus("PENDING");
        when(alarmRecordMapper.selectList(any())).thenReturn(List.of(a));

        var list = dashboardService.realtimeAlarms(10);
        assertEquals(1, list.size());
        assertEquals("CRITICAL", list.get(0).getLevel());
    }
}
