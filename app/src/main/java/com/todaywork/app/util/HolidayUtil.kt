package com.todaywork.app.util

import java.time.LocalDate

/**
 * 대한민국 공휴일 유틸리티
 * 고정 공휴일 + 이동 공휴일(음력 기반) 지원
 * 음력 날짜는 LunarUtil을 통해 양력으로 변환
 */
object HolidayUtil {

    data class Holiday(val name: String, val isPublic: Boolean = true)

    /**
     * 특정 연도의 모든 공휴일 반환 (Map<LocalDate, Holiday>)
     */
    fun getHolidays(year: Int): Map<LocalDate, Holiday> {
        val holidays = mutableMapOf<LocalDate, Holiday>()

        // ── 고정 공휴일 ──────────────────────────────────────────
        holidays[LocalDate.of(year, 1, 1)]  = Holiday("신정")
        holidays[LocalDate.of(year, 3, 1)]  = Holiday("삼일절")
        holidays[LocalDate.of(year, 5, 1)]  = Holiday("근로자의 날")
        holidays[LocalDate.of(year, 5, 5)]  = Holiday("어린이날")
        holidays[LocalDate.of(year, 6, 6)]  = Holiday("현충일")
        holidays[LocalDate.of(year, 8, 15)] = Holiday("광복절")
        holidays[LocalDate.of(year, 10, 3)] = Holiday("개천절")
        holidays[LocalDate.of(year, 10, 9)] = Holiday("한글날")
        holidays[LocalDate.of(year, 12, 25)] = Holiday("크리스마스")

        // ── 이동 공휴일 (음력 → 양력 변환) ───────────────────────
        // 설날: 음력 1월 1일 전·당일·다음날
        LunarUtil.lunarToSolar(year, 1, 1, false)?.let { seollal ->
            holidays[seollal.minusDays(1)] = Holiday("설날 연휴")
            holidays[seollal]              = Holiday("설날")
            holidays[seollal.plusDays(1)]  = Holiday("설날 연휴")
        }

        // 부처님 오신 날: 음력 4월 8일
        LunarUtil.lunarToSolar(year, 4, 8, false)?.let {
            holidays[it] = Holiday("부처님 오신 날")
        }

        // 추석: 음력 8월 15일 전·당일·다음날
        LunarUtil.lunarToSolar(year, 8, 15, false)?.let { chuseok ->
            holidays[chuseok.minusDays(1)] = Holiday("추석 연휴")
            holidays[chuseok]              = Holiday("추석")
            holidays[chuseok.plusDays(1)]  = Holiday("추석 연휴")
        }

        // 대체공휴일 적용 (일요일 겹침 시 다음 평일)
        applySubstituteHolidays(holidays, year)

        return holidays
    }

    /**
     * 대체공휴일 처리
     * 공휴일이 일요일이면 다음 평일(월요일)을 대체공휴일로 지정
     */
    private fun applySubstituteHolidays(
        holidays: MutableMap<LocalDate, Holiday>,
        year: Int
    ) {
        val substitutes = mutableMapOf<LocalDate, Holiday>()
        holidays.forEach { (date, holiday) ->
            if (date.dayOfWeek == java.time.DayOfWeek.SUNDAY && holiday.isPublic) {
                var substituteDate = date.plusDays(1)
                // 이미 공휴일이거나 대체공휴일인 날 건너뜀
                while (holidays.containsKey(substituteDate) || substitutes.containsKey(substituteDate)) {
                    substituteDate = substituteDate.plusDays(1)
                }
                substitutes[substituteDate] = Holiday("대체공휴일(${holiday.name})")
            }
        }
        holidays.putAll(substitutes)
    }

    fun isHoliday(date: LocalDate, holidayMap: Map<LocalDate, Holiday>): Boolean =
        holidayMap.containsKey(date)

    fun getHolidayName(date: LocalDate, holidayMap: Map<LocalDate, Holiday>): String =
        holidayMap[date]?.name ?: ""
}
