package com.coldchain.modules.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coldchain.common.exception.BizException;
import com.coldchain.common.page.PageQuery;
import com.coldchain.common.page.PageResult;
import com.coldchain.config.ColdChainProperties;
import com.coldchain.modules.trace.dto.BatchCreateRequest;
import com.coldchain.modules.trace.dto.BatchCreateResultVO;
import com.coldchain.modules.trace.dto.QrCodeVO;
import com.coldchain.modules.trace.entity.BlockchainTx;
import com.coldchain.modules.trace.entity.ProductBatch;
import com.coldchain.modules.trace.entity.TraceRecord;
import com.coldchain.modules.trace.mapper.ProductBatchMapper;
import com.coldchain.modules.trace.mapper.TraceRecordMapper;
import com.coldchain.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 批次赋码业务 QT-002
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private static final String H5_PATH = "/h5/trace?batchNo=";

    private final ProductBatchMapper productBatchMapper;
    private final TraceRecordMapper traceRecordMapper;
    private final MockBlockchainService mockBlockchainService;
    private final ColdChainProperties coldChainProperties;

    @Transactional
    public BatchCreateResultVO create(BatchCreateRequest req) {
        String batchNo = nextBatchNo();
        String qrUrl = buildQrUrl(batchNo);
        LoginUser user = currentUser();

        ProductBatch batch = new ProductBatch();
        batch.setBatchNo(batchNo);
        batch.setProductName(req.getProductName());
        batch.setOrigin(req.getOrigin());
        batch.setProducerId(user == null ? 0L : user.getUserId());
        batch.setProduceDate(req.getProduceDate().atStartOfDay());
        batch.setShelfLife(req.getShelfLife());
        batch.setQuantity(req.getQuantity());
        batch.setUnit(req.getUnit());
        batch.setQrCode(qrUrl);
        batch.setStatus(0);
        batch.setCreateTime(LocalDateTime.now());
        productBatchMapper.insert(batch);

        // 模拟上链（MySQL 存哈希）
        String payload = batchNo + "|" + req.getProductName() + "|" + req.getOrigin()
                + "|" + req.getProduceDate() + "|" + req.getQuantity() + req.getUnit();
        BlockchainTx tx = mockBlockchainService.chain("BATCH", batchNo, payload);
        batch.setTxHash(tx.getTxHash());
        batch.setStatus(1);
        productBatchMapper.updateById(batch);

        // 初始追溯节点：赋码/生产
        TraceRecord trace = new TraceRecord();
        trace.setBatchId(batch.getBatchId());
        trace.setOperation("PRODUCE");
        trace.setOperator(user == null ? "系统" : user.getUsername());
        trace.setLocation(req.getOrigin());
        trace.setOpTime(LocalDateTime.now());
        trace.setTxHash(tx.getTxHash());
        trace.setCreateTime(LocalDateTime.now());
        traceRecordMapper.insert(trace);

        log.info("批次赋码成功 batchNo={} txHash={}", batchNo, tx.getTxHash());
        return BatchCreateResultVO.builder()
                .batchId(batch.getBatchId())
                .batchNo(batchNo)
                .qrCodeUrl(qrUrl)
                .txHash(tx.getTxHash())
                .status("CHAINED")
                .build();
    }

    public PageResult<ProductBatch> page(PageQuery query, Integer status, String keyword) {
        LambdaQueryWrapper<ProductBatch> qw = new LambdaQueryWrapper<>();
        if (status != null) {
            qw.eq(ProductBatch::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(ProductBatch::getBatchNo, keyword)
                    .or().like(ProductBatch::getProductName, keyword)
                    .or().like(ProductBatch::getOrigin, keyword));
        }
        qw.orderByDesc(ProductBatch::getCreateTime);
        Page<ProductBatch> page = productBatchMapper.selectPage(new Page<>(query.getPage(), query.getSize()), qw);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public QrCodeVO qrCode(Long id) {
        ProductBatch batch = productBatchMapper.selectById(id);
        if (batch == null) {
            throw new BizException("批次不存在");
        }
        String content = toAbsoluteQrUrl(batch.getQrCode() != null ? batch.getQrCode() : buildQrUrl(batch.getBatchNo()),
                batch.getBatchNo());
        return QrCodeVO.builder()
                .batchId(batch.getBatchId())
                .batchNo(batch.getBatchNo())
                .qrContent(content)
                .qrCodeUrl(content)
                .txHash(batch.getTxHash())
                .build();
    }

    private String nextBatchNo() {
        String prefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Long count = productBatchMapper.selectCount(new LambdaQueryWrapper<ProductBatch>()
                .likeRight(ProductBatch::getBatchNo, prefix));
        long seq = (count == null ? 0 : count) + 1;
        return prefix + String.format("%06d", seq);
    }

    private String buildQrUrl(String batchNo) {
        return toAbsoluteQrUrl(H5_PATH + batchNo, batchNo);
    }

    private String toAbsoluteQrUrl(String raw, String batchNo) {
        if (StringUtils.hasText(raw) && raw.startsWith("http")) {
            // 旧数据若写死 localhost / trace.local，强制改写为公网基址
            if (raw.contains("localhost") || raw.contains("127.0.0.1") || raw.contains("trace.local")) {
                return publicBase() + H5_PATH + batchNo;
            }
            return raw;
        }
        String path = StringUtils.hasText(raw) ? raw : (H5_PATH + batchNo);
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return publicBase() + path;
    }

    private String publicBase() {
        String base = coldChainProperties.getPublicBaseUrl();
        if (!StringUtils.hasText(base)) {
            base = "http://192.168.1.3:5173";
        }
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base.trim();
    }

    private LoginUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser user) {
            return user;
        }
        return null;
    }
}
