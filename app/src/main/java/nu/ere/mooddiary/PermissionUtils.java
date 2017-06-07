package nu.ere.mooddiary;

// https://github.com/hotchemi/PermissionsDispatcher/issues/51#issuecomment-161787596

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.util.SimpleArrayMap;

import static android.os.Build.VERSION_CODES;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;

public final class PermissionUtils {

    // Map of dangerous permissions introduced in later framework versions.
    // Used to conditionally bypass permission-hold checks on older devices.
    private static final SimpleArrayMap<String, Integer> MIN_SDK_PERMISSIONS;
    static {
        MIN_SDK_PERMISSIONS = new SimpleArrayMap<>(6);
        MIN_SDK_PERMISSIONS.put("com.android.voicemail.permission.ADD_VOICEMAIL", VERSION_CODES.ICE_CREAM_SANDWICH);
        MIN_SDK_PERMISSIONS.put("android.permission.BODY_SENSORS", VERSION_CODES.KITKAT_WATCH);
        MIN_SDK_PERMISSIONS.put("android.permission.READ_CALL_LOG", VERSION_CODES.JELLY_BEAN);
        MIN_SDK_PERMISSIONS.put("android.permission.READ_EXTERNAL_STORAGE", VERSION_CODES.JELLY_BEAN);
        MIN_SDK_PERMISSIONS.put("android.permission.USE_SIP", VERSION_CODES.GINGERBREAD);
        MIN_SDK_PERMISSIONS.put("android.permission.WRITE_CALL_LOG", VERSION_CODES.JELLY_BEAN);
    }

    // ...

    private static boolean permissionExists(String permission) {
        // Check if the permission could potentially be missing on this device
        Integer minVersion = MIN_SDK_PERMISSIONS.get(permission);

        // If null was returned from the above call, there is no need for a device API level check for the permission;
        // otherwise, we check if its minimum API level requirement is met
        return minVersion == null || Build.VERSION.SDK_INT >= minVersion;
    }

    /**
     * Returns true if the Activity or Fragment has access to all given permissions.
     *
     * @param context     context
     * @param permissions permission list
     * @return returns true if the Activity or Fragment has access to all given permissions.
     */
    public static boolean hasSelfPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (permissionExists(permission) && checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}