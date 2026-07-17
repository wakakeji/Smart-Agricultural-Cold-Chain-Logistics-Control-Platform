package com.coldchain.modules.suggestion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.common.exception.BizException;
import com.coldchain.modules.suggestion.entity.DecisionSuggestion;
import com.coldchain.modules.suggestion.mapper.DecisionSuggestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SuggestionService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DecisionSuggestionMapper mapper;

    public List<Map<String, Object>> list(String status, String priority) {
        LambdaQueryWrapper<DecisionSuggestion> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            qw.eq(DecisionSuggestion::getStatus, status);
        }
        if (StringUtils.hasText(priority)) {
            qw.eq(DecisionSuggestion::getPriority, priority);
        }
        qw.orderByDesc(DecisionSuggestion::getCreateTime);
        return mapper.selectList(qw).stream().map(this::toMap).toList();
    }

    public void adopt(Long id) {
        updateStatus(id, "ADOPTED");
    }

    public void ignore(Long id) {
        updateStatus(id, "IGNORED");
    }

    public Map<String, Object> stats() {
        List<DecisionSuggestion> all = mapper.selectList(null);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", all.size());
        m.put("pending", all.stream().filter(s -> "PENDING".equals(s.getStatus())).count());
        m.put("adopted", all.stream().filter(s -> "ADOPTED".equals(s.getStatus())).count());
        m.put("ignored", all.stream().filter(s -> "IGNORED".equals(s.getStatus())).count());
        return m;
    }

    private void updateStatus(Long id, String status) {
        DecisionSuggestion s = mapper.selectById(id);
        if (s == null) {
            throw new BizException("建议不存在");
        }
        s.setStatus(status);
        mapper.updateById(s);
    }

    private Map<String, Object> toMap(DecisionSuggestion s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("sugId", s.getSugId());
        m.put("type", s.getType());
        m.put("title", s.getTitle());
        m.put("content", s.getContent());
        m.put("priority", s.getPriority());
        m.put("status", s.getStatus());
        m.put("relatedBatchId", s.getRelatedBatchId());
        m.put("expectedEffect", effectOf(s.getType()));
        m.put("supportData", supportOf(s.getType()));
        m.put("createTime", s.getCreateTime() == null ? null : s.getCreateTime().format(FMT));
        return m;
    }

    private String effectOf(String type) {
        return switch (type == null ? "" : type) {
            case "TEMP_CONTROL" -> "预计降低超温告警 60%";
            case "REROUTE" -> "预计缩短在途时间 25 分钟";
            case "PRIORITY_SALE" -> "预计减少临期损耗 2.5%";
            case "MAINTENANCE" -> "降低设备故障风险";
            default -> "提升冷链运营效率";
        };
    }

    private String supportOf(String type) {
        return switch (type == null ? "" : type) {
            case "TEMP_CONTROL" -> "近1小时车厢均温 5.8℃";
            case "REROUTE" -> "路况拥堵指数 1.8";
            case "PRIORITY_SALE" -> "剩余货架期 42h";
            default -> "基于历史告警与运输数据";
        };
    }
}
