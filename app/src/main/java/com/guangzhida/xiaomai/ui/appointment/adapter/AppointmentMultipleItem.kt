package com.guangzhida.xiaomai.ui.appointment.adapter

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.guangzhida.xiaomai.model.AppointmentModel

class AppointmentMultipleItem constructor(val item: AppointmentModel) : MultiItemEntity {
    override val itemType: Int
        get() = getItemTypeByMessage()


    private fun getItemTypeByMessage(): Int {
        return when(item.type){
            1->{
                APPOINTMENT_PLAY
            }
            2->{
                APPOINTMENT_CAR
            }
            3->{
                APPOINTMENT_WORK
            }
            else -> APPOINTMENT_PLAY
        }
    }

    companion object {
        const val APPOINTMENT_CAR = 0x01
        const val APPOINTMENT_PLAY = 0x02
        const val APPOINTMENT_WORK = 0x03
    }
}