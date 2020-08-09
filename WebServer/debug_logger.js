class Log {
  static verbose = false

  static ver(message, data, options) {
    if (Log.verbose) {
      if (data != undefined) {
        console.log(`\n${message} ...`)
        console.log(data)
      } else {
        console.log(`\n${message}`)
      }
    }
  }

  static log(message) {
    if (Log.verbose)
      console.log(`\n${message}`)
    else
      console.log(message)
  }
}

module.exports = { Log, }