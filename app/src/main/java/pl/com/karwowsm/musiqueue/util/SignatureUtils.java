package pl.com.karwowsm.musiqueue.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import androidx.annotation.NonNull;

import java.security.MessageDigest;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class SignatureUtils {

    public static String getSignature(@NonNull PackageManager packageManager, @NonNull String packageName) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
                if (packageInfo == null
                    || packageInfo.signingInfo == null
                    || packageInfo.signingInfo.getApkContentsSigners() == null
                    || packageInfo.signingInfo.getApkContentsSigners().length == 0
                    || packageInfo.signingInfo.getApkContentsSigners()[0] == null) {
                    return null;
                }
                return signatureDigest(packageInfo.signingInfo.getApkContentsSigners()[0]);
            } else {
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                if (packageInfo == null
                    || packageInfo.signatures == null
                    || packageInfo.signatures.length == 0
                    || packageInfo.signatures[0] == null) {
                    return null;
                }
                return signatureDigest(packageInfo.signatures[0]);
            }
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @SneakyThrows
    private static String signatureDigest(Signature signature) {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        return bytesToHex(digest.digest(signature.toByteArray()));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
}
