package com.agent.platform.mcp.entity;

import com.agent.platform.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 服务使用统计实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "service_usage",
       uniqueConstraints = @UniqueConstraint(columnNames = {"deployment_id", "user_id", "date"}))
public class ServiceUsage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deployment_id", nullable = false)
    private ServiceDeployment deployment;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "request_count")
    private Long requestCount = 0L;

    @Column(name = "response_time_avg", precision = 10, scale = 2)
    private BigDecimal responseTimeAvg;

    @Column(name = "error_count")
    private Long errorCount = 0L;

    @Column(name = "data_transfer_mb", precision = 15, scale = 2)
    private BigDecimal dataTransferMb = BigDecimal.ZERO;

    @Column(name = "cost", precision = 10, scale = 4)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * 计算错误率
     */
    public BigDecimal getErrorRate() {
        if (requestCount == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(errorCount)
                .divide(BigDecimal.valueOf(requestCount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 增加请求统计
     */
    public void addRequest(long responseTime, boolean isError, BigDecimal dataSize) {
        this.requestCount++;
        
        if (isError) {
            this.errorCount++;
        }
        
        // 更新平均响应时间
        if (this.responseTimeAvg == null) {
            this.responseTimeAvg = BigDecimal.valueOf(responseTime);
        } else {
            BigDecimal totalTime = this.responseTimeAvg.multiply(BigDecimal.valueOf(this.requestCount - 1));
            totalTime = totalTime.add(BigDecimal.valueOf(responseTime));
            this.responseTimeAvg = totalTime.divide(BigDecimal.valueOf(this.requestCount), 2, BigDecimal.ROUND_HALF_UP);
        }
        
        // 累加数据传输量
        if (dataSize != null) {
            this.dataTransferMb = this.dataTransferMb.add(dataSize);
        }
    }
}