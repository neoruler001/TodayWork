package com.todaywork.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.todaywork.app.data.model.DayInfo
import com.todaywork.app.data.model.ShiftType
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * ICS (iCalendar, RFC 5545) 파일 생성 및 삼성 캘린더 공유
 */
object IcsExporter {

    private val icsDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
    private val icsDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val stampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")

    /**
     * DayInfo 목록을 ICS 파일로 내보내고 공유 Intent 반환
     */
    fun exportAndShare(context: Context, days: List<DayInfo>, monthLabel: String): Intent? {
        val icsContent = buildIcsContent(days)
        val file = writeIcsFile(context, icsContent, monthLabel)
        return createShareIntent(context, file)
    }

    private fun buildIcsContent(days: List<DayInfo>): String {
        val sb = StringBuilder()
        val now = LocalDateTime.now(ZoneId.of("UTC")).format(stampFormatter)

        sb.append("BEGIN:VCALENDAR\r\n")
        sb.append("VERSION:2.0\r\n")
        sb.append("PRODID:-//TodayWork//TodayWork App//KO\r\n")
        sb.append("CALSCALE:GREGORIAN\r\n")
        sb.append("METHOD:PUBLISH\r\n")
        sb.append("X-WR-TIMEZONE:Asia/Seoul\r\n")
        sb.append("BEGIN:VTIMEZONE\r\n")
        sb.append("TZID:Asia/Seoul\r\n")
        sb.append("BEGIN:STANDARD\r\n")
        sb.append("DTSTART:19700101T000000\r\n")
        sb.append("TZOFFSETFROM:+0900\r\n")
        sb.append("TZOFFSETTO:+0900\r\n")
        sb.append("TZNAME:KST\r\n")
        sb.append("END:STANDARD\r\n")
        sb.append("END:VTIMEZONE\r\n")

        for (day in days) {
            val shift = day.shiftType ?: continue
            if (!shift.isWorkDay && shift != ShiftType.ANNUAL && shift != ShiftType.HALF_DAY_AM && shift != ShiftType.HALF_DAY_PM) continue

            val uid = "todaywork-${day.date.format(icsDateFormatter)}-${shift.name}@todaywork.app"
            val memoTitle = day.memos.firstOrNull()?.title ?: ""
            val summary = "[${shift.shortLabel}] ${shift.label}${if (memoTitle.isNotBlank()) " - $memoTitle" else ""}"

            sb.append("BEGIN:VEVENT\r\n")
            sb.append("UID:$uid\r\n")
            sb.append("DTSTAMP:$now\r\n")

            if (shift.isWorkDay && day.startTime.isNotBlank() && day.endTime.isNotBlank()) {
                // 시간 있는 근무: DTSTART/DTEND에 시간 포함
                val startDt = buildDateTime(day.date, shift, isStart = true)
                val endDt = buildDateTime(day.date, shift, isStart = false)
                sb.append("DTSTART;TZID=Asia/Seoul:${startDt.format(icsDateTimeFormatter)}\r\n")
                sb.append("DTEND;TZID=Asia/Seoul:${endDt.format(icsDateTimeFormatter)}\r\n")
            } else {
                // 하루 종일 이벤트 (휴무, 연차 등)
                sb.append("DTSTART;VALUE=DATE:${day.date.format(icsDateFormatter)}\r\n")
                sb.append("DTEND;VALUE=DATE:${day.date.plusDays(1).format(icsDateFormatter)}\r\n")
            }

            sb.append("SUMMARY:${foldLine(summary)}\r\n")
            sb.append("CATEGORIES:근무\r\n")
            val description = day.memos.joinToString("\n") { it.title }
            if (description.isNotBlank()) {
                sb.append("DESCRIPTION:${foldLine(description)}\r\n")
            }
            sb.append("END:VEVENT\r\n")
        }

        sb.append("END:VCALENDAR\r\n")
        return sb.toString()
    }

    private fun buildDateTime(date: LocalDate, shift: ShiftType, isStart: Boolean): LocalDateTime {
        val hour = if (isStart) shift.defaultStartHour else shift.defaultEndHour
        val min  = if (isStart) shift.defaultStartMin  else shift.defaultEndMin

        return if (!isStart && shift == ShiftType.NIGHT) {
            // 야간 근무는 익일 종료
            LocalDateTime.of(date.plusDays(1), LocalTime.of(hour, min))
        } else {
            LocalDateTime.of(date, LocalTime.of(hour, min))
        }
    }

    /** ICS 규격: 75 옥텟 이상이면 줄 접기 (CRLF + 공백) */
    private fun foldLine(text: String): String {
        if (text.length <= 74) return text
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            val end = minOf(i + 74, text.length)
            sb.append(text.substring(i, end))
            if (end < text.length) sb.append("\r\n ")
            i = end
        }
        return sb.toString()
    }

    private fun writeIcsFile(context: Context, content: String, label: String): File {
        val cacheDir = File(context.cacheDir, "ics").also { it.mkdirs() }
        val file = File(cacheDir, "todaywork_${label}.ics")
        file.writeText(content, Charsets.UTF_8)
        return file
    }

    private fun createShareIntent(context: Context, file: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/calendar")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
