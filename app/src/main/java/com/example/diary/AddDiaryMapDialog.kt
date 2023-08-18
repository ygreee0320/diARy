package com.example.diary

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.time.LocalDateTime
import java.time.ZoneId

class AddDiaryMapDialog(val context: Context) {
    lateinit var onClickListener: ButtonClickListener

    //일정정보
    lateinit var dateS_d: Array<Int>
    lateinit var dateE_d: Array<Int>
    lateinit var timeS_d: Array<Int>
    lateinit var timeE_d: Array<Int>

    val dialog = Dialog(context)

    @RequiresApi(Build.VERSION_CODES.O)
    fun myDialog(dateS: Array<Int>?, dateE: Array<Int>?, timeS: Array<Int>?, timeE: Array<Int>?) {
        dialog.setContentView(R.layout.add_diary_map_dialog)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        //View id
        val date_s = dialog.findViewById<Button>(R.id.date_s)
        val date_e = dialog.findViewById<Button>(R.id.date_e)
        val time_s = dialog.findViewById<Button>(R.id.time_s)
        val time_e = dialog.findViewById<Button>(R.id.time_e)
        val cancelBtn = dialog.findViewById<TextView>(R.id.cancelBtn)
        val submitBtn = dialog.findViewById<TextView>(R.id.submitBtn)

        //일정 정보 - 초기값: 현재 날짜 및 시간
        if(dateS === null) {
            val now = LocalDateTime.now(ZoneId.of("Asia/Seoul"))

            dateS_d = arrayOf(now.year, now.monthValue, now.dayOfMonth)
            dateE_d = arrayOf(now.year, now.monthValue, now.dayOfMonth)
            timeS_d = arrayOf(now.hour, now.minute)
            timeE_d = arrayOf(now.hour, now.minute)
        } else {
            dateS_d = dateS
            dateE_d = dateE!!
            timeS_d = timeS!!
            timeE_d = timeE!!

            date_s.text = "${dateS_d[0]}-${dateS_d[1]}-${dateS_d[2]}"
            date_e.text = "${dateE_d[0]}-${dateE_d[1]}-${dateE_d[2]}"
            time_s.text = "${timeS_d[0]}:${timeS_d[1]}"
            time_e.text = "${timeE_d[0]}:${timeE_d[1]}"
        }

        date_s.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context, object: DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                    dateS_d[0] = year
                    dateS_d[1] = month+1
                    dateS_d[2] = dayOfMonth
                    date_s.text = "${year}-${month+1}-${dayOfMonth}"
                }
            }, dateS_d[0], dateS_d[1]-1, dateS_d[2])
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.primary))
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.primary))
        }

        date_e.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context, object: DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                    dateE_d[0] = year
                    dateE_d[1] = month+1
                    dateE_d[2] = dayOfMonth
                    date_e.text = "${year}-${month+1}-${dayOfMonth}"
                }
            }, dateE_d[0], dateE_d[1]-1, dateE_d[2])
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.primary))
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.primary))
        }

        time_s.setOnClickListener {
            val timePickerDialog = TimePickerDialog(context, object: TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                    timeS_d[0] = hourOfDay
                    timeS_d[1] = minute
                    time_s.text = "${hourOfDay}:${minute}"
                }
            }, timeS_d[0], timeS_d[1], true)
            timePickerDialog.show()
            timePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.primary))
            timePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.primary))
        }

        time_e.setOnClickListener {
            val timePickerDialog = TimePickerDialog(context, object: TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                    timeE_d[0] = hourOfDay
                    timeE_d[1] = minute
                    time_e.text = "${hourOfDay}:${minute}"
                }
            }, timeE_d[0], timeE_d[1], true)
            timePickerDialog.show()
            timePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.primary))
            timePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.primary))
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        submitBtn.setOnClickListener {
            onClickListener.onClicked(dateS_d, dateE_d, timeS_d, timeE_d)
            dialog.dismiss()
        }

        dialog.show()
    }

    interface ButtonClickListener {
        fun onClicked(dateS_d: Array<Int>, dateE_d: Array<Int>, timeS_d: Array<Int>, timeE_d: Array<Int>)
    }

    fun setOnClickedListener(listener: ButtonClickListener) {
        onClickListener = listener
    }
}