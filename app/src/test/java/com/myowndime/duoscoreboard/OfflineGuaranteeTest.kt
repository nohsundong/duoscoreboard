package com.myowndime.duoscoreboard

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Locks in the core promise of the app: the merged manifest that ships to the store
 * must not request a single permission. This test fails the build if any dependency
 * ever merges one in.
 */
@RunWith(AndroidJUnit4::class)
class OfflineGuaranteeTest {

    @Test
    fun `merged manifest declares no permissions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS,
        )

        val requested = info.requestedPermissions?.toList().orEmpty()
        assertEquals(emptyList<String>(), requested)
    }

    @Test
    fun `app cannot reach the internet`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val granted = context.checkSelfPermission(android.Manifest.permission.INTERNET)

        assertTrue(
            "INTERNET must not be granted",
            granted == PackageManager.PERMISSION_DENIED,
        )
    }
}
