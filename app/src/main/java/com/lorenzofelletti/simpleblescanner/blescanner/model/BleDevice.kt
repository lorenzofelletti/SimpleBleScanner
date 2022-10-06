package com.lorenzofelletti.simpleblescanner.blescanner.model

data class BleDevice(val name: String) {
    companion object {
        fun createBleDevicesList(): MutableList<BleDevice> {
            return mutableListOf()
        }
    }
}
