package com.coldchain.modules.facility.service;

import com.coldchain.modules.facility.dto.FacilityItemVO;
import com.coldchain.modules.facility.dto.FacilityListVO;
import com.coldchain.modules.facility.entity.Facility;
import com.coldchain.modules.facility.mapper.FacilityMapper;
import com.coldchain.modules.facility.mapper.SensorDeviceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 设施列表业务单元测试
 */
@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

    @Mock
    private FacilityMapper facilityMapper;
    @Mock
    private SensorDeviceMapper sensorDeviceMapper;
    @InjectMocks
    private FacilityService facilityService;

    @Test
    void listSplitsStorageAndVehicle() {
        Facility s = new Facility();
        s.setFacilityId(1L);
        s.setName("冷库A");
        s.setType("COLD_STORAGE");
        s.setLng(108.3);
        s.setLat(22.8);
        s.setStatus("ONLINE");

        Facility v = new Facility();
        v.setFacilityId(2L);
        v.setName("桂A·D1001");
        v.setType("REFRIGERATED_VEHICLE");
        v.setLng(108.4);
        v.setLat(22.9);
        v.setStatus("ONLINE");

        when(facilityMapper.selectList(any())).thenReturn(List.of(s, v));

        FacilityListVO vo = facilityService.list("all", null);
        assertEquals(1, vo.getColdStorages().size());
        assertEquals(1, vo.getVehicles().size());
        assertEquals(1, vo.getTotal().get("storages"));
        FacilityItemVO vehicle = vo.getVehicles().get(0);
        assertEquals("桂A·D1001", vehicle.getPlateNo());
    }
}
