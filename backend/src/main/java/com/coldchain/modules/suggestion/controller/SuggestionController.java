package com.coldchain.modules.suggestion.controller;

import com.coldchain.common.api.R;
import com.coldchain.modules.suggestion.service.SuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "决策建议")
@RestController
@RequestMapping("/api/suggestion")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService suggestionService;

    @GetMapping("/list")
    @Operation(summary = "建议列表")
    public R<List<Map<String, Object>>> list(@RequestParam(required = false) String status,
                                             @RequestParam(required = false) String priority) {
        return R.ok(suggestionService.list(status, priority));
    }

    @GetMapping("/stats")
    @Operation(summary = "建议统计")
    public R<Map<String, Object>> stats() {
        return R.ok(suggestionService.stats());
    }

    @PutMapping("/{id}/adopt")
    @Operation(summary = "采纳建议")
    public R<Void> adopt(@PathVariable Long id) {
        suggestionService.adopt(id);
        return R.ok();
    }

    @PutMapping("/{id}/ignore")
    @Operation(summary = "忽略建议")
    public R<Void> ignore(@PathVariable Long id) {
        suggestionService.ignore(id);
        return R.ok();
    }
}
