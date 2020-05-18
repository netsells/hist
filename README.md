# Hist

[![](https://jitpack.io/v/netsells/hist.svg)](https://jitpack.io/#netsells/hist)

Hist is a "tree" for [Timber](https://github.com/JakeWharton/timber) which sends logs to Logstash.

## Usage

When you've added the `hist` package to your project, simply plant the `HistTree`:

```kotlin
Timber.plant(
    HistTree(
        "YOUR APP NAME",
        BuildConfig.VERSION_NAME,
        BuildConfig.VERSION_CODE,
        if (BuildConfig.DEBUG) "debug" else "production",
        "http://your.logstash.url",
        5001
    )
)
```

