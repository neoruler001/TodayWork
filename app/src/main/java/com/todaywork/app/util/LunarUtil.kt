package com.todaywork.app.util

import java.time.LocalDate

/**
 * 음력 ↔ 양력 변환 유틸리티
 * 한국천문연구원 데이터 기반 룩업 테이블 방식 (2020~2035)
 *
 * lunarMonthData 구조: IntArray per year
 *   [0]: 1월 1일 양력 월/일 (MMDD 형식)
 *   [1~12]: 각 음력 월의 일수 (29 or 30)
 *   [13]: 윤달 위치 (0=없음, 1~12=윤달 위치)
 *   [14]: 윤달 일수 (29 or 30)
 */
object LunarUtil {

    // 음력 1월 1일 양력 날짜 + 각 월 일수 테이블 (2020~2035)
    // 형식: Pair(음력1월1일 양력날짜, IntArray[각월일수(12개), 윤달위치, 윤달일수])
    private val lunarData: Map<Int, Pair<LocalDate, IntArray>> = mapOf(
        2020 to Pair(LocalDate.of(2020, 1, 25), intArrayOf(30,29,30,29,30,30,29,30,29,30,29,30, 4, 29)),
        2021 to Pair(LocalDate.of(2021, 2, 12), intArrayOf(29,30,29,29,30,29,30,29,30,30,29,30, 0,  0)),
        2022 to Pair(LocalDate.of(2022, 2,  1), intArrayOf(30,29,30,29,29,30,29,30,29,30,30,29, 0,  0)),
        2023 to Pair(LocalDate.of(2023, 1, 22), intArrayOf(30,29,30,30,29,29,30,29,30,29,30,29, 2, 29)),
        2024 to Pair(LocalDate.of(2024, 2, 10), intArrayOf(30,30,29,30,29,30,29,29,30,29,30,29, 0,  0)),
        2025 to Pair(LocalDate.of(2025, 1, 29), intArrayOf(30,29,30,30,29,30,29,30,29,29,30,29, 6, 30)),
        2026 to Pair(LocalDate.of(2026, 2, 17), intArrayOf(30,29,30,29,30,30,29,30,29,30,29,29, 0,  0)),
        2027 to Pair(LocalDate.of(2027, 2,  6), intArrayOf(30,29,29,30,29,30,30,29,30,30,29,30, 0,  0)),
        2028 to Pair(LocalDate.of(2028, 1, 26), intArrayOf(29,30,29,29,30,29,30,29,30,30,29,30, 5, 29)),
        2029 to Pair(LocalDate.of(2029, 2, 13), intArrayOf(30,29,30,29,29,30,29,30,29,30,29,30, 0,  0)),
        2030 to Pair(LocalDate.of(2030, 2,  3), intArrayOf(30,29,30,30,29,29,30,29,30,29,30,29, 0,  0)),
        2031 to Pair(LocalDate.of(2031, 1, 23), intArrayOf(30,30,29,30,29,30,29,29,30,29,30,29, 3, 30)),
        2032 to Pair(LocalDate.of(2032, 2, 11), intArrayOf(30,30,29,30,30,29,30,29,29,30,29,30, 0,  0)),
        2033 to Pair(LocalDate.of(2033, 1, 31), intArrayOf(29,30,29,30,30,29,30,29,30,29,30,29, 11,29)),
        2034 to Pair(LocalDate.of(2034, 2, 19), intArrayOf(30,29,30,29,30,29,30,30,29,30,29,30, 0,  0)),
        2035 to Pair(LocalDate.of(2035, 2,  8), intArrayOf(29,30,29,29,30,29,30,30,29,30,30,29, 0,  0))
    )

    /**
     * 음력 → 양력 변환
     * @param year 음력 연도
     * @param month 음력 월 (1~12)
     * @param day 음력 일 (1~30)
     * @param isLeapMonth 윤달 여부
     * @return 양력 LocalDate, 변환 불가 시 null
     */
    fun lunarToSolar(year: Int, month: Int, day: Int, isLeapMonth: Boolean = false): LocalDate? {
        val data = lunarData[year] ?: return null
        val (baseDate, monthDays) = data
        val leapMonthPos = monthDays[12]
        val leapMonthDays = monthDays[13]

        var offset = 0
        for (m in 1 until month) {
            offset += monthDays[m - 1]
            // 윤달 처리: 해당 월 다음에 윤달이 있으면 더함
            if (leapMonthPos == m) {
                offset += leapMonthDays
            }
        }
        // 윤달 자체인 경우
        if (isLeapMonth && leapMonthPos == month) {
            offset += monthDays[month - 1]
        }
        offset += day - 1
        return baseDate.plusDays(offset.toLong())
    }

    /**
     * 양력 → 음력 변환
     * @return Triple(음력년, 음력월, 음력일, 윤달여부) 또는 null
     */
    fun solarToLunar(solarDate: LocalDate): LunarDate? {
        for ((year, data) in lunarData) {
            val (baseDate, monthDays) = data
            val leapMonthPos = monthDays[12]
            val leapMonthDays = monthDays[13]

            if (solarDate < baseDate) continue

            // 이 해의 총 음력 일수 계산
            val totalDaysInYear = monthDays.take(12).sum() +
                    (if (leapMonthPos > 0) leapMonthDays else 0)

            val nextYearBase = lunarData[year + 1]?.first ?: baseDate.plusDays(totalDaysInYear.toLong())
            if (solarDate >= nextYearBase) continue

            var remaining = (solarDate.toEpochDay() - baseDate.toEpochDay()).toInt()

            for (m in 1..12) {
                val regularDays = monthDays[m - 1]
                if (remaining < regularDays) {
                    return LunarDate(year, m, remaining + 1, false)
                }
                remaining -= regularDays

                // 윤달 체크
                if (leapMonthPos == m && leapMonthDays > 0) {
                    if (remaining < leapMonthDays) {
                        return LunarDate(year, m, remaining + 1, true)
                    }
                    remaining -= leapMonthDays
                }
            }
        }
        return null
    }

    data class LunarDate(
        val year: Int,
        val month: Int,
        val day: Int,
        val isLeapMonth: Boolean
    ) {
        fun toDisplayString(): String {
            val leapStr = if (isLeapMonth) "윤" else ""
            return "$leapStr${month}.${day}"
        }
    }
}
