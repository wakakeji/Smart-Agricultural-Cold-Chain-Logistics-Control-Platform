package com.coldchain.modules.monitor.service;

import com.coldchain.modules.facility.entity.Facility;
import com.coldchain.modules.facility.entity.SensorDevice;
import com.coldchain.modules.facility.mapper.FacilityMapper;
import com.coldchain.modules.facility.mapper.SensorDeviceMapper;
import com.coldchain.modules.monitor.dto.SensorListVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * 实时监控业务单元测试
 */
@ExtendWith(MockitoExtension.class)
class MonitorServiceTest {

    @Mock
    private SensorDeviceMapper sensorDeviceMapper;
    @Mock
    private FacilityMapper facilityMapper;
    @InjectMocks
    private MonitorService monitorService;

    @Test
    void listSensorsMapsStatusAndValues() {
        SensorDevice d = new SensorDevice();
        d.setSensorId(1L);
        d.setFacilityId(10L);
        d.setName("传感器1");
        d.setType("TEMP");
        d.setStatus(1);

        Facility f = new Facility();
        f.setFacilityId(10L);
        f.setName("冷库A");
        f.setCurrentTemp(2.5);
        f.setCurrentHumidity(80.0);

        when(sensorDeviceMapper.selectList(any())).thenReturn(List.of(d));
        when(facilityMapper.selectBatchIds(anyList())).thenReturn(List.of(f));

        SensorListVO vo = monitorService.listSensors(null, null, null, false);
        assertEquals(1, vo.getTotal());
        assertEquals("ONLINE", vo.getRecords().get(0).getStatus());
        assertEquals("冷库A", vo.getRecords().get(0).getFacilityName());
        assertNotNull(vo.getRecords().get(0).getTemperature());
    }
}
