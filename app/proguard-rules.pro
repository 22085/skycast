# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.skycast.weather.data.remote.dto.** {
    *** Companion;
    <fields>;
    <init>(...);
}
-keepclasseswithmembers class com.skycast.weather.data.remote.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Coroutines
-dontwarn kotlinx.coroutines.**
