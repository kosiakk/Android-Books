package com.kosenkov.androidbooks.collections

import org.junit.Assert


/**
 * Test utility function to ensure that exception is thrown
 */
inline fun <E : Exception> assertThrows(block: () -> Unit) {
    try {
        block()
        Assert.fail("Exception should have been thrown")
    } catch (expected: E) {
        // Expected behaviour.
        // Other exceptions classes will be re-thrown.
    }
}