package com.example.doannhom15.controller;

import com.example.doannhom15.model.Booking;
import com.example.doannhom15.model.User;
import com.example.doannhom15.model.Booking.BookingStatus;
import com.example.doannhom15.service.BookingService;
import com.example.doannhom15.service.MovieService;
import com.example.doannhom15.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {
    
    private final MovieService movieService;
    private final BookingService bookingService;
    private final UserService userService;

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        // Tổng quan
        model.addAttribute("totalMovies", movieService.count());
        model.addAttribute("totalBookings", bookingService.count());
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("totalRevenue", bookingService.getTotalRevenue());

        // Hôm nay
        model.addAttribute("todayBookings", bookingService.countTodayBookings(startOfDay));
        model.addAttribute("todayRevenue", bookingService.getTodayRevenue(startOfDay));
        model.addAttribute("todayCheckedIn", bookingService.countTodayCheckedIn(startOfDay));
        model.addAttribute("pendingBookings", bookingService.countPendingBookings());

        // Đơn hàng gần đây (10 cái)
        List<Booking> recentBookings = bookingService.getRecentBookings(10);
        model.addAttribute("recentBookings", recentBookings);

        // Tính tổng payment cho mỗi booking (để hiển thị)
        Map<Long, BigDecimal> totalPaymentMap = new HashMap<>();
        for (Booking b : recentBookings) {
            totalPaymentMap.put(b.getId(), b.getTotalPrice());
        }
        model.addAttribute("totalPaymentMap", totalPaymentMap);

        // Staff list
        List<User> staffList = userService.getAllStaffAndAdmin();
        model.addAttribute("staffList", staffList);

        // Thống kê booking theo ngày trong tuần
        Map<String, Long> weeklyStats = getWeeklyBookingStats();
        model.addAttribute("weeklyStats", weeklyStats);

        // Biểu đồ (tránh null / thiếu dữ liệu)
        List<Object[]> monthlyRows = bookingService.getMonthlyRevenue(Calendar.getInstance().get(Calendar.YEAR));
        Map<Integer, Double> revenueMap = new HashMap<>();
        for (Object[] row : monthlyRows) {
            revenueMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }
        model.addAttribute("monthlyRevenue", revenueMap);
        long paidChart = bookingService.countByStatus(BookingStatus.PAID)
                + bookingService.countByStatus(BookingStatus.CONFIRMED);
        model.addAttribute("paidBookings", paidChart);
        model.addAttribute("cancelledBookings", bookingService.countByStatus(BookingStatus.CANCELLED));

        return "admin/dashboard";
    }

    private Map<String, Long> getWeeklyBookingStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("CN", 0L);
        stats.put("T2", 0L);
        stats.put("T3", 0L);
        stats.put("T4", 0L);
        stats.put("T5", 0L);
        stats.put("T6", 0L);
        stats.put("T7", 0L);

        LocalDateTime startOfWeek = LocalDate.now().minusDays(7).atStartOfDay();
        List<Booking> weekBookings = bookingService.getTodayBookings(startOfWeek);

        for (Booking b : weekBookings) {
            String dayKey;
            switch (b.getBookingTime().getDayOfWeek().getValue()) {
                case 1: dayKey = "T2"; break;
                case 2: dayKey = "T3"; break;
                case 3: dayKey = "T4"; break;
                case 4: dayKey = "T5"; break;
                case 5: dayKey = "T6"; break;
                case 6: dayKey = "T7"; break;
                default: dayKey = "CN"; break;
            }
            stats.merge(dayKey, 1L, Long::sum);
        }
        return stats;
    }

    @GetMapping("/dashboard-data")
    @ResponseBody
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        
        long totalMovies = movieService.count();
        long totalBookings = bookingService.count();
        long totalUsers = userService.count();
        BigDecimal totalRevenue = bookingService.getTotalRevenue();
        
        data.put("totalMovies", totalMovies);
        data.put("totalBookings", totalBookings);
        data.put("totalUsers", totalUsers);
        data.put("totalRevenue", totalRevenue);
        
        List<Object[]> monthlyRevenueData = bookingService.getMonthlyRevenue(Calendar.getInstance().get(Calendar.YEAR));
        List<String> months = new ArrayList<>();
        List<Double> revenues = new ArrayList<>();
        
        for (int i = 1; i <= 12; i++) {
            months.add("Month " + i);
            final int month = i;
            double revenue = monthlyRevenueData.stream()
                    .filter(row -> ((Number) row[0]).intValue() == month)
                    .mapToDouble(row -> ((Number) row[1]).doubleValue())
                    .findFirst()
                    .orElse(0.0);
            revenues.add(revenue);
        }
        
        data.put("months", months);
        data.put("revenues", revenues);
        
        return data;
    }
    
    @GetMapping("/analytics")
    public String analytics(Model model) {
        List<Object[]> monthlyRevenueData = bookingService.getMonthlyRevenue(Calendar.getInstance().get(Calendar.YEAR));
        
        Map<Integer, Double> revenueMap = new HashMap<>();
        for (Object[] row : monthlyRevenueData) {
            revenueMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }
        
        model.addAttribute("monthlyRevenue", revenueMap);
        
        return "admin/analytics";
    }

    @GetMapping("/staff-monitoring")
    @ResponseBody
    public Map<String, Object> getStaffMonitoring() {
        Map<String, Object> data = new HashMap<>();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        List<User> staffList = userService.getAllStaffAndAdmin();
        List<Map<String, Object>> staffStats = new ArrayList<>();

        for (User staff : staffList) {
            Map<String, Object> s = new HashMap<>();
            s.put("id", staff.getId());
            s.put("username", staff.getUsername());
            s.put("role", staff.getRole());
            s.put("enabled", staff.isEnabled());
            staffStats.add(s);
        }

        data.put("staffList", staffStats);
        data.put("totalStaff", staffList.size());
        data.put("todayTotalBookings", bookingService.countTodayBookings(startOfDay));
        data.put("todayRevenue", bookingService.getTodayRevenue(startOfDay));
        data.put("todayCheckedIn", bookingService.countTodayCheckedIn(startOfDay));

        return data;
    }
}
