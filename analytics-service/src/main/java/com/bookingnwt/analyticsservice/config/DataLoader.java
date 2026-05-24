package com.bookingnwt.analyticsservice.config;

import com.bookingnwt.analyticsservice.model.PropertyStatistics;
import com.bookingnwt.analyticsservice.model.RevenueReport;
import com.bookingnwt.analyticsservice.repository.PropertyStatisticsRepository;
import com.bookingnwt.analyticsservice.repository.RevenueReportRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initAnalyticsData(PropertyStatisticsRepository propStatsRepo,
                                        RevenueReportRepository revenueRepo) {
        return args -> {
            // Idempotent — preskoci ako podaci vec postoje (ddl-auto=update zadrzava)
            if (propStatsRepo.count() > 0) {
                System.out.println("=== Analytics Service: DB vec ima podatke, preskacem seed ===");
                return;
            }

            // ===== PropertyStatistics: Apartman "Sunce" (property 1, host 2) =====

            // Januar 2026
            propStatsRepo.save(new PropertyStatistics(1L, 2L, 2026, 1,
                    5, new BigDecimal("1250.00"),
                    new BigDecimal("4.80"), new BigDecimal("52.00"),
                    180, 0));

            // Februar 2026
            propStatsRepo.save(new PropertyStatistics(1L, 2L, 2026, 2,
                    6, new BigDecimal("1560.00"),
                    new BigDecimal("4.80"), new BigDecimal("58.00"),
                    210, 1));

            // Mart 2026
            propStatsRepo.save(new PropertyStatistics(1L, 2L, 2026, 3,
                    8, new BigDecimal("2100.00"),
                    new BigDecimal("4.85"), new BigDecimal("65.00"),
                    280, 0));

            // April 2026
            propStatsRepo.save(new PropertyStatistics(1L, 2L, 2026, 4,
                    7, new BigDecimal("1890.00"),
                    new BigDecimal("4.82"), new BigDecimal("62.00"),
                    250, 1));

            // Maj 2026
            propStatsRepo.save(new PropertyStatistics(1L, 2L, 2026, 5,
                    10, new BigDecimal("3200.00"),
                    new BigDecimal("4.85"), new BigDecimal("78.00"),
                    342, 0));

            // Juni 2026 (tekući mjesec)
            propStatsRepo.save(new PropertyStatistics(1L, 2L, 2026, 6,
                    8, new BigDecimal("3200.00"),
                    new BigDecimal("4.80"), new BigDecimal("78.00"),
                    342, 1));

            // ===== PropertyStatistics: Vila "Stari Most" (property 2, host 2) =====

            // Mart 2026
            propStatsRepo.save(new PropertyStatistics(2L, 2L, 2026, 3,
                    3, new BigDecimal("900.00"),
                    new BigDecimal("4.50"), new BigDecimal("40.00"),
                    120, 0));

            // April 2026
            propStatsRepo.save(new PropertyStatistics(2L, 2L, 2026, 4,
                    4, new BigDecimal("1200.00"),
                    new BigDecimal("4.55"), new BigDecimal("50.00"),
                    155, 0));

            // Maj 2026
            propStatsRepo.save(new PropertyStatistics(2L, 2L, 2026, 5,
                    5, new BigDecimal("1650.00"),
                    new BigDecimal("4.50"), new BigDecimal("65.00"),
                    218, 1));

            // Juni 2026
            propStatsRepo.save(new PropertyStatistics(2L, 2L, 2026, 6,
                    4, new BigDecimal("1650.00"),
                    new BigDecimal("4.50"), new BigDecimal("65.00"),
                    218, 0));

            // ===== PropertyStatistics: Hostel "Jezero" (property 3, host 3) =====

            // Maj 2026
            propStatsRepo.save(new PropertyStatistics(3L, 3L, 2026, 5,
                    6, new BigDecimal("720.00"),
                    new BigDecimal("3.90"), new BigDecimal("55.00"),
                    95, 2));

            // Juni 2026
            propStatsRepo.save(new PropertyStatistics(3L, 3L, 2026, 6,
                    4, new BigDecimal("480.00"),
                    new BigDecimal("3.90"), new BigDecimal("45.00"),
                    78, 0));

            System.out.println("=== Analytics Service: Učitano " + propStatsRepo.count()
                    + " property statistika ===");

            // ===== RevenueReport: Host 2 (Marko - vlasnik property 1 i 2) =====

            revenueRepo.save(new RevenueReport(2L, 2026, 1,
                    new BigDecimal("1250.00"), new BigDecimal("125.00"),
                    new BigDecimal("1125.00"),
                    5, 0, 2, new BigDecimal("52.00")));

            revenueRepo.save(new RevenueReport(2L, 2026, 2,
                    new BigDecimal("1560.00"), new BigDecimal("156.00"),
                    new BigDecimal("1404.00"),
                    6, 1, 2, new BigDecimal("58.00")));

            revenueRepo.save(new RevenueReport(2L, 2026, 3,
                    new BigDecimal("3000.00"), new BigDecimal("300.00"),
                    new BigDecimal("2700.00"),
                    11, 0, 2, new BigDecimal("52.50")));

            revenueRepo.save(new RevenueReport(2L, 2026, 4,
                    new BigDecimal("3090.00"), new BigDecimal("309.00"),
                    new BigDecimal("2781.00"),
                    11, 1, 2, new BigDecimal("56.00")));

            revenueRepo.save(new RevenueReport(2L, 2026, 5,
                    new BigDecimal("4850.00"), new BigDecimal("485.00"),
                    new BigDecimal("4365.00"),
                    15, 1, 2, new BigDecimal("71.50")));

            revenueRepo.save(new RevenueReport(2L, 2026, 6,
                    new BigDecimal("4850.00"), new BigDecimal("485.00"),
                    new BigDecimal("4365.00"),
                    12, 1, 2, new BigDecimal("71.50")));

            // ===== RevenueReport: Host 3 (vlasnik property 3) =====

            revenueRepo.save(new RevenueReport(3L, 2026, 5,
                    new BigDecimal("720.00"), new BigDecimal("72.00"),
                    new BigDecimal("648.00"),
                    6, 2, 1, new BigDecimal("55.00")));

            revenueRepo.save(new RevenueReport(3L, 2026, 6,
                    new BigDecimal("480.00"), new BigDecimal("48.00"),
                    new BigDecimal("432.00"),
                    4, 0, 1, new BigDecimal("45.00")));

            System.out.println("=== Analytics Service: Učitano " + revenueRepo.count()
                    + " revenue izvještaja ===");
        };
    }
}
