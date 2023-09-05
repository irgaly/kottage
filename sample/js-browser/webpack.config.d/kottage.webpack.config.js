config.resolve.fallback = {
    ...config.resolve.fallback,
    fs: false,
    os: false
}
config.externals = {
    ...config.externals,
    "better-sqlite3": "better-sqlite3"
}
