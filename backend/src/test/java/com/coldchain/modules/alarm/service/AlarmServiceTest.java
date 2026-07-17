package com.coldchain.modules.alarm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coldchain.common.page.PageQuery;
import com.coldchain.modules.alarm.dto.AlarmHandleRequest;
import com.coldchain.modules.alarm.entity.AlarmRecord;
import com.coldchain.modules.alarm.mapper.AlarmRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 告警处理单元测试
 */
@ExtendWith(MockitoExtension.class)
class AlarmServiceTest {

    @Mock
    private AlarmRecordMapper alarmRecordMapper;
    @InjectMocks
    private AlarmService alarmService;

    @Test
    void handleUpdatesStatusAndHandler() {
        AlarmRecord record = new AlarmRecord();
        record.setAlarmId(1L);
        record.setStatus("PENDING");
        when(alarmRecordMapper.selectById(1L)).thenReturn(record);

        AlarmHandleRequest req = new AlarmHandleRequest();
        req.setStatus("RESOLVED");
        req.setHandleRemark("已降温");
        alarmService.handle(1L, req);

        ArgumentCaptor<AlarmRecord> captor = ArgumentCaptor.forClass(AlarmRecord.class);
        verify(alarmRecordMapper).updateById(captor.capture());
        assertEquals("RESOLVED", captor.getValue().getStatus());
        assertEquals("已降温", captor.getValue().getHandleRemark());
        assertNotNull(captor.getValue().getHandleTime());
    }

    @Test
    void pageDelegatesToMapper() {
        Page<AlarmRecord> page = new Page<>(1, 10);
        page.setRecords(List.of(new AlarmRecord()));
        page.setTotal(1);
        when(alarmRecordMapper.selectPage(any(Page.class), any())).thenReturn(page);

        var result = alarmService.page(new PageQuery(), null, "PENDING", null, null, null);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }
}
