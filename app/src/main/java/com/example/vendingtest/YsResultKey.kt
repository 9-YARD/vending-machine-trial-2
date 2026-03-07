package com.example.vendingtest

/**
 * Mirror of the SDK result-map keys returned by ship() and doSomething() callbacks.
 * These string values match the keys the AAR places in the result MutableMap.
 */
object YsResultKey {
    const val DRIVE_RESULT_CODE          = "ResultCode"
    const val DRIVE_RESULT_CMDTYPE       = "CmdType"
    const val DRIVE_RESULT_STATUS        = "Status"
    const val DRIVE_RESULT_ERRCODE       = "ErrCode"
    const val DRIVE_RESULT_ERRMSG        = "ErrMsg"
    const val DRIVE_RESULT_TRANSID       = "TransId"
    const val DRIVE_RESULT_SLOTNO        = "SlotNo"
    const val DRIVE_RESULT_ORDER         = "Order"
    const val DRIVE_RESULT_PAYMETHOD     = "PayMethod"
    const val DRIVE_RESULT_AMOUNT        = "Amount"
}
