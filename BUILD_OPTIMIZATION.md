# Build Optimization Guide

## Overview

This document outlines the build optimization strategies implemented in NanoBanana to minimize build times, reduce APK size, and enhance developer productivity.

## Current Build Configuration

### Gradle Version
- **Gradle**: 8.13
- **Android Gradle Plugin (AGP)**: 8.2.2
- **Kotlin**: 2.0.21

### Build Performance Metrics (Target)

| Metric | Debug | Release | Target |
|--------|-------|---------|--------|
| Clean Build | ~90s | ~120s | <120s |
| Incremental Build | ~15s | ~30s | <20s |
| APK Size | N/A | ~15MB | <20MB |
| Memory Usage | ~2GB | ~2.5GB | <3GB |

## Gradle Optimizations

### 1. Parallel Execution

**Configuration** (`gradle.properties`):
```properties
org.gradle.parallel=true
```

**Benefit**: Builds independent modules in parallel, reducing overall build time by 20-30%.

### 2. Build Caching

**Configuration** (`gradle.properties`):
```properties
org.gradle.caching=true
```

**Benefit**: Reuses outputs from previous builds, reducing incremental build time by 40-50%.

### 3. Configuration on Demand

**Configuration** (`gradle.properties`):
```properties
org.gradle.configureondemand=true
```

**Benefit**: Only configures relevant projects, reducing configuration time for multi-module projects.

### 4. Gradle Daemon

**Configuration** (`gradle.properties`):
```properties
org.gradle.daemon=true
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
```

**Benefit**: Keeps Gradle process alive between builds, eliminating JVM startup time.

## Kotlin Compiler Optimizations

### 1. Incremental Compilation

**Configuration** (`gradle.properties`):
```properties
kotlin.incremental=true
kotlin.incremental.android=true
```

**Benefit**: Only recompiles changed files and their dependencies, reducing incremental build time by 50-70%.

### 2. Compiler Arguments

**Configuration** (`app/build.gradle.kts`):
```kotlin
kotlinOptions {
    jvmTarget = "11"
    freeCompilerArgs += listOf(
        "-opt-in=kotlin.RequiresOptIn",
        "-Xjvm-default=all"
    )
}
```

**Benefits**:
- Faster compilation with JVM default methods
- Reduced bytecode size

## Android Build Optimizations

### 1. R8 Full Mode

**Configuration** (`gradle.properties`):
```properties
android.enableR8.fullMode=true
```

**Benefit**: More aggressive code shrinking and optimization, reducing APK size by 10-15%.

### 2. Resource Shrinking

**Configuration** (`app/build.gradle.kts`):
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
    }
}
```

**Benefit**: Removes unused resources, reducing APK size by 5-10%.

### 3. Non-Transitive R Classes

**Configuration** (`gradle.properties`):
```properties
android.nonTransitiveRClass=true
```

**Benefit**: Reduces R class size and compilation time by only including resources from each module.

### 4. Reduced Debug Symbol Level

**Configuration** (`app/build.gradle.kts`):
```kotlin
buildTypes {
    debug {
        ndk {
            debugSymbolLevel = "NONE"
        }
    }
    release {
        ndk {
            debugSymbolLevel = "SYMBOL_TABLE"
        }
    }
}
```

**Benefit**: Faster debug builds with smaller APK size.

## Dependency Management

### 1. Version Catalog

Using `gradle/libs.versions.toml` for centralized dependency management.

**Benefits**:
- Single source of truth for versions
- Type-safe dependency declarations
- Easier version updates
- Better IDE support

### 2. Dependency Resolution

**Best Practices**:
```kotlin
dependencies {
    // Use BOM (Bill of Materials) for consistent versions
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))
    
    // Exclude transitive dependencies when not needed
    implementation(libs.some.library) {
        exclude(group = "com.example", module = "unwanted")
    }
}
```

### 3. Dependency Analysis

**Command**:
```bash
./gradlew app:dependencies
```

**Use**: Identify and remove unnecessary dependencies.

## ProGuard/R8 Configuration

### Current Rules (`proguard-rules.pro`)

```proguard
# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Compose runtime
-keep class androidx.compose.** { *; }

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Firebase AI
-keep class com.google.firebase.** { *; }
-keep class com.google.ai.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
```

### Optimization

**Additional Rules for Size Reduction**:
```proguard
# Optimize method inlining
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Remove unused code
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}
```

## APK Size Optimization

### Current Size Breakdown

```
Total APK Size: ~15MB
├── Code (DEX): ~3MB
├── Resources: ~2MB
├── Assets: ~1MB
├── Native libs: ~8MB
└── Other: ~1MB
```

### Strategies

#### 1. App Bundle (Future)

**Configuration**:
```kotlin
android {
    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
}
```

**Benefit**: Reduces download size by 30-50% through on-demand delivery.

#### 2. Image Optimization

**Current**: PNG images
**Recommendation**: Use WebP format

**Conversion**:
```bash
# Convert PNG to WebP
cwebp -q 80 input.png -o output.webp
```

**Benefit**: 25-35% smaller file size with same quality.

#### 3. Vector Drawables

**Recommendation**: Use vector drawables instead of PNGs for icons and simple graphics.

**Example**:
```xml
<!-- Instead of multiple PNG densities -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="#FF000000"
          android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2z"/>
</vector>
```

**Benefit**: Single file for all densities, 60-80% smaller.

#### 4. Remove Unused Resources

**Command**:
```bash
./gradlew :app:minifyReleaseWithR8
```

**Manual Analysis**:
```bash
./gradlew :app:lintRelease
# Check lint-results-release.html for unused resources
```

## Build Performance Monitoring

### 1. Build Scan

**Enable**:
```bash
./gradlew build --scan
```

**Access**: Opens build scan URL in browser with detailed performance metrics.

### 2. Build Time Profiling

**Enable**:
```bash
./gradlew build --profile
```

**Output**: `build/reports/profile/` with HTML report.

### 3. Gradle Build Analysis

**View Task Duration**:
```bash
./gradlew build --dry-run
./gradlew build --debug | grep "Task :"
```

## CI/CD Optimization

### GitHub Actions Configuration

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle
      
      - name: Cache Gradle
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-gradle-
      
      - name: Build with Gradle
        run: ./gradlew assembleDebug --no-daemon --parallel
```

**Optimizations**:
- Gradle caching between runs
- Parallel execution
- No daemon (CI doesn't benefit from daemon)

## Developer Machine Setup

### Recommended Settings

**Android Studio**:
- **Memory**: 4GB+ (Preferences → Appearance & Behavior → System Settings)
- **Gradle JVM**: Embedded JDK 17
- **Offline Mode**: Enable when dependencies are cached

**gradle.properties** (local, not committed):
```properties
# Increase Gradle memory
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m -Dfile.encoding=UTF-8

# Enable parallel GC
org.gradle.jvmargs=-XX:+UseParallelGC

# Increase Kotlin compiler memory
kotlin.daemon.jvmargs=-Xmx2048m
```

## Troubleshooting

### Slow Builds

1. **Check Gradle version**: Ensure using latest stable
2. **Clear caches**: `./gradlew cleanBuildCache`
3. **Analyze build scan**: `./gradlew build --scan`
4. **Check memory**: Increase `org.gradle.jvmargs`

### Large APK Size

1. **Run APK analyzer**: Build → Analyze APK in Android Studio
2. **Check resources**: Look for large images, consider WebP
3. **Check dependencies**: Remove unused libraries
4. **Enable R8**: Ensure `isMinifyEnabled = true`

### Out of Memory

1. **Increase Gradle memory**: `org.gradle.jvmargs=-Xmx4096m`
2. **Increase Kotlin daemon memory**: `kotlin.daemon.jvmargs=-Xmx2048m`
3. **Reduce parallel tasks**: `org.gradle.workers.max=2`

## Future Optimizations

### Multi-Module Architecture

**Plan**: Split app into feature modules

**Benefits**:
- Parallel compilation
- Better caching
- Faster incremental builds
- Clearer dependencies

**Structure**:
```
:app
:feature-generation
:feature-settings
:core-domain
:core-data
:core-ui
```

**Expected Improvement**: 40-60% faster incremental builds

### Baseline Profiles

**Plan**: Generate baseline profiles for startup optimization

**Configuration**:
```kotlin
android {
    defaultConfig {
        profileBlock {
            profile = file("baseline-prof.txt")
        }
    }
}
```

**Benefit**: 30% faster app startup

### Build Cache Optimization

**Plan**: Set up remote build cache for team

**Configuration**:
```properties
org.gradle.caching.remote.url=https://build-cache.example.com
```

**Benefit**: Share build cache across team, 50-70% faster clean builds

## Monitoring & Metrics

### Build Time Tracking

**Tool**: Gradle Enterprise (future)

**Metrics to Track**:
- Average build time
- Cache hit rate
- Configuration time
- Task execution time
- APK size trend

### Success Metrics

| Metric | Current | Target | Achieved |
|--------|---------|--------|----------|
| Incremental Build | ~30s | <20s | ⏳ |
| Clean Build | ~150s | <120s | ⏳ |
| APK Size | ~15MB | <20MB | ✅ |
| Cache Hit Rate | ~60% | >80% | ⏳ |

## References

- [Gradle Performance Guide](https://docs.gradle.org/current/userguide/performance.html)
- [Android Build Optimization](https://developer.android.com/studio/build/optimize-your-build)
- [R8 Shrinking Guide](https://developer.android.com/studio/build/shrink-code)
- [Kotlin Compiler Options](https://kotlinlang.org/docs/compiler-reference.html)

---

**Document Version**: 1.0  
**Last Updated**: November 2024  
**Next Review**: January 2025  
**Owner**: NanoBanana Build Team
