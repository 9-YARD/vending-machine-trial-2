package com.example.vendingtest

/**
 * Mirror of the SDK protocol constants used when building command maps.
 * These values match the constants defined inside the AAR's service protocol.
 */
object YsAidlConstant {
    object MsgType {
        const val MSG_TYPE_VERSION_INFO  = 0
        const val MSG_TYPE_SHIP          = 1
        const val MSG_TYPE_SHIP_TEST     = 2
        const val MSG_TYPE_QUERY_STATUS  = 3
        const val MSG_TYPE_SET_PARAMS    = 4
        const val MSG_TYPE_QUERY_PARAMS  = 5
        const val MSG_TYPE_CLEAR_FAULTS  = 6
        const val MSG_TYPE_DO_ACTION     = 7
        const val MSG_TYPE_REFRIG_ON     = 8
        const val MSG_TYPE_REFRIG_OFF    = 9
        const val MSG_TYPE_DO_SOMETHING  = 20
    }
}
