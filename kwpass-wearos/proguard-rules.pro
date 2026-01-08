# 1. Shared 모듈 및 데이터 모델 보호 (필수)
-keep class minmul.kwpass.shared.** { *; }

-keep class minmul.kwpass.main.** { *; }
-keep class minmul.kwpass.service.** { *; }

# 2. DataStore 및 Protobuf
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.** { *; }
-keep class androidx.datastore.preferences.protobuf.** { *; }

# 3. Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature, InnerClasses
-keepclassmembers class ** {
    *** Companion;
    *** $serializer;
}
-keep @kotlinx.serialization.Serializable class * { *; }

# 4. Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# 5. Tiles, Complications
-keep class androidx.wear.tiles.** { *; }
-keep public class * extends androidx.wear.watchface.complications.data.ComplicationData
-keep public class * extends androidx.wear.watchface.complications.datasource.ComplicationDataSourceService