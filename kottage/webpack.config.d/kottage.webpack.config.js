config.resolve.fallback = {
  ...config.resolve.fallback,
  path: false,
  util: false,
  fs: false,
  os: false
}
config.ignoreWarnings = [
   ...config.ignoreWarnings,
   {
     module: /better-sqlite3/,
     message: /Critical dependency: the request of a dependency is an expression/
   }
 ]
