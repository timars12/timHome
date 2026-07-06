#!/usr/bin/env node
// Driver for the timHome Android app (com.timhome.modularizationtest).
// Builds the debug APK, boots the emulator, installs, launches, and drives
// the running app over adb (tap / text / key / screenshot / ui dump).
//
// Usage:  node .claude/skills/run-timhome/driver.mjs <command> [args]
//   up                  build + boot + install + launch + screenshot (full flow)
//   build               ./gradlew :app:assembleDebug
//   boot [avd]          start the emulator (default AVD: Medium_Phone_API_36.0) and wait for boot
//   install             adb install -r the debug APK
//   launch              launch MainActivity via monkey
//   screenshot [name]   pull a PNG into ./build/run-screenshots/<name>.png (default: shot)
//   tap <x> <y>         inject a tap
//   text "<str>"        type into the focused field (spaces -> %s automatically)
//   key <KEYCODE>       e.g. key 66 (ENTER), key 4 (BACK)
//   ui                  dump the view hierarchy XML to stdout (for finding coordinates)
//   logcat              dump recent app logcat (filtered to the app PID)
//   stop                kill the running emulator
//
// Paths are resolved relative to the repo root regardless of cwd.

import { execFileSync, spawn } from "node:child_process";
import { existsSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const REPO = resolve(dirname(fileURLToPath(import.meta.url)), "..", "..", "..");
const APK = join(REPO, "app", "build", "outputs", "apk", "debug", "app-debug.apk");
const PKG = "com.timhome.modularizationtest";
// Explicit component: the debug build also registers a LeakCanary LAUNCHER
// activity, so `monkey -c LAUNCHER` is non-deterministic. Start MainActivity
// directly. Class lives under the namespace (com.timhome.modularizationtest).
const ACTIVITY = `${PKG}/com.timhome.modularizationtest.ui.MainActivity`;
const DEFAULT_AVD = "Medium_Phone_API_36.0";
const SHOT_DIR = join(REPO, "build", "run-screenshots");
const isWin = process.platform === "win32";

function sdkDir() {
  // local.properties wins (that's what Gradle uses), then env.
  const lp = join(REPO, "local.properties");
  if (existsSync(lp)) {
    const m = readFileSync(lp, "utf8").match(/^\s*sdk\.dir\s*=\s*(.+)\s*$/m);
    if (m) return m[1].replace(/\\([:\\])/g, "$1").trim(); // unescape C\:\\... -> C:\...
  }
  const env = process.env.ANDROID_HOME || process.env.ANDROID_SDK_ROOT;
  if (env) return env;
  throw new Error("Cannot find SDK: set sdk.dir in local.properties or ANDROID_HOME.");
}
const SDK = sdkDir();
const ADB = join(SDK, "platform-tools", isWin ? "adb.exe" : "adb");
const EMULATOR = join(SDK, "emulator", isWin ? "emulator.exe" : "emulator");

const adb = (args, opts = {}) =>
  execFileSync(ADB, args, { encoding: "utf8", stdio: ["ignore", "pipe", "pipe"], ...opts });
const adbBuf = (args) => execFileSync(ADB, args, { maxBuffer: 64 * 1024 * 1024 });

function gradle(tasks) {
  const wrapper = isWin ? join(REPO, "gradlew.bat") : join(REPO, "gradlew");
  console.log(`> ${wrapper} ${tasks.join(" ")}`);
  execFileSync(wrapper, [...tasks, "--console=plain"], { cwd: REPO, stdio: "inherit", shell: isWin });
}

function hasDevice() {
  try {
    return adb(["devices"]).split("\n").some((l) => /\tdevice$/.test(l.trim()));
  } catch {
    return false;
  }
}

function build() {
  gradle([":app:assembleDebug"]);
  if (!existsSync(APK)) throw new Error(`Build finished but APK missing: ${APK}`);
  console.log(`APK: ${APK}`);
}

async function boot(avd = DEFAULT_AVD) {
  if (hasDevice()) {
    console.log("Device already online; skipping emulator launch.");
  } else {
    console.log(`Booting emulator ${avd} ...`);
    const child = spawn(EMULATOR, ["-avd", avd, "-no-snapshot-save", "-no-boot-anim"], {
      detached: true,
      stdio: "ignore",
    });
    child.unref();
    adb(["wait-for-device"]);
  }
  // Wait for full boot.
  for (let i = 0; i < 120; i++) {
    try {
      if (adb(["shell", "getprop", "sys.boot_completed"]).trim() === "1") {
        console.log("Boot complete.");
        adb(["shell", "input", "keyevent", "82"]); // dismiss keyguard
        return;
      }
    } catch {
      /* device not ready yet */
    }
    await new Promise((r) => setTimeout(r, 2000));
  }
  throw new Error("Emulator did not finish booting within ~4 min.");
}

function install() {
  if (!existsSync(APK)) throw new Error(`APK not found, run 'build' first: ${APK}`);
  console.log(adb(["install", "-r", APK]).trim());
}

function launch() {
  adb(["shell", "am", "force-stop", PKG]);
  console.log(adb(["shell", "am", "start", "-n", ACTIVITY]).trim());
}

function screenshot(name = "shot") {
  mkdirSync(SHOT_DIR, { recursive: true });
  const out = join(SHOT_DIR, `${name}.png`);
  writeFileSync(out, adbBuf(["exec-out", "screencap", "-p"]));
  console.log(`Screenshot: ${out}`);
}

function tap(x, y) {
  adb(["shell", "input", "tap", String(x), String(y)]);
}
function text(str) {
  adb(["shell", "input", "text", str.replace(/ /g, "%s")]);
}
function key(code) {
  adb(["shell", "input", "keyevent", String(code)]);
}
function ui() {
  adb(["shell", "uiautomator", "dump", "/sdcard/ui.xml"]);
  process.stdout.write(adb(["shell", "cat", "/sdcard/ui.xml"]));
}
function logcat() {
  const pid = adb(["shell", "pidof", PKG]).trim();
  if (!pid) {
    console.log("App not running.");
    return;
  }
  process.stdout.write(adb(["logcat", "-d", "--pid", pid]));
}
function stop() {
  try {
    adb(["emu", "kill"]);
    console.log("Emulator killed.");
  } catch {
    console.log("No emulator to kill.");
  }
}

const [cmd, ...rest] = process.argv.slice(2);
const run = async () => {
  switch (cmd) {
    case "up":
      build();
      await boot();
      install();
      launch();
      await new Promise((r) => setTimeout(r, 4000));
      screenshot("up");
      break;
    case "build": build(); break;
    case "boot": await boot(rest[0]); break;
    case "install": install(); break;
    case "launch": launch(); break;
    case "screenshot": screenshot(rest[0]); break;
    case "tap": tap(rest[0], rest[1]); break;
    case "text": text(rest.join(" ")); break;
    case "key": key(rest[0]); break;
    case "ui": ui(); break;
    case "logcat": logcat(); break;
    case "stop": stop(); break;
    default:
      console.error("Unknown command. See header of driver.mjs for usage.");
      process.exit(1);
  }
};
run().catch((e) => {
  console.error(e.message || e);
  process.exit(1);
});
