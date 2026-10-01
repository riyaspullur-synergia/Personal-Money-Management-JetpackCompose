# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Keep line numbers so crash stack traces stay symbolicatable via mapping.txt,
# but replace the real Kotlin file name so it doesn't leak in decompiled output.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- Reverse-engineering hardening ----

# Merge every obfuscated class into a single, unnamed top-level package instead
# of preserving the original package structure, hiding app architecture from
# decompilers, and let R8 relax visibility to merge/inline more aggressively.
-repackageclasses ''
-allowaccessmodification

# Reuse short obfuscated names across unrelated classes/members instead of
# giving each a unique name.
-overloadaggressively

# Strip all logging from the release build so no internal state leaks via logcat.
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
