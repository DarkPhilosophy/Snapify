package ro.snapify.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationPermissionTest {
    @Test
    fun firstRunWithoutRationaleIsRequestable() {
        assertEquals(
            NotificationPermissionState.REQUESTABLE,
            classifyNotificationPermission(
                sdkInt = 33,
                permissionGranted = false,
                shouldShowRationale = false,
                requestAttempted = false,
            ),
        )
    }

    @Test
    fun grantedPermissionIsGranted() {
        assertEquals(
            NotificationPermissionState.GRANTED,
            classifyNotificationPermission(33, true, false, false),
        )
    }

    @Test
    fun olderAndroidIsGranted() {
        assertEquals(
            NotificationPermissionState.GRANTED,
            classifyNotificationPermission(32, false, false, false),
        )
    }

    @Test
    fun rationaleAfterRequestIsRequestable() {
        assertEquals(
            NotificationPermissionState.REQUESTABLE,
            classifyNotificationPermission(33, false, true, true),
        )
    }

    @Test
    fun denialWithoutRationaleAfterRequestIsPermanent() {
        assertEquals(
            NotificationPermissionState.PERMANENTLY_DENIED,
            classifyNotificationPermission(33, false, false, true),
        )
    }
}
