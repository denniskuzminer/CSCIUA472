import fs from "fs";
import util from "util";

export function captureConsoleOutput() {
  let output = "";

  const originalLog = console.log;
  console.log = function (...args) {
    output +=
      args.map((arg) => util.inspect(arg, { showHidden: false, depth: null })) +
      "\n";
    originalLog.apply(console, args);
  };

  return {
    getOutput: () => output,
    writeToFile: (fileName) => {
      const writeStream = fs.createWriteStream(fileName);
      writeStream.write(output.replaceAll("\\n' +", ""));
      writeStream.end();
    },
  };
}
