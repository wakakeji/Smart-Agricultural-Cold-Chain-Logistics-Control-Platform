package com.coldchain.modules.transport.dto;

import com.coldchain.modules.facility.dto.TrackPointVO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TransportRealtimeVO {
    private TransportOrderVO order;
    private List<TrackPointVO> track;
    private String eta;
    private String lastUpdate;
}
