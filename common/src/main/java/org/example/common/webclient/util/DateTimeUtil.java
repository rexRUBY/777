package org.example.common.webclient.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    // "년-월-일" 형식으로 현재 날짜를 반환하는 메서드
    public static String getCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return now.format(dateFormatter);
    }

    // "시:분" 형식으로 현재 시간을 반환하는 메서드
    public static String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(1);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return now.format(timeFormatter);
    }

    public static void main(String[] args) {
        String currentDate = getCurrentDate();
        String currentTime = getCurrentTime();

        System.out.println("현재 날짜: " + currentDate);
        System.out.println("현재 시간: " + currentTime);
    }
}