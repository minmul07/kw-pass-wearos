# 1. DataStore 및 Protobuf
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.** { *; }
-keep class androidx.datastore.preferences.protobuf.** { *; }

# 2. Shared 모듈 및 데이터 모델 보호
-keep class minmul.kwpass.shared.** { *; }
-keep class minmul.kwpass.service.** { *; }

# 3. Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature, InnerClasses

-keepclassmembers class ** {
    *** Companion;
    *** $serializer;
}
-keep @kotlinx.serialization.Serializable class * { *; }

# 4. Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }